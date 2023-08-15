package com.example.demo

import com.google.gson.Gson
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.pytorch.IValue
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/classifier")
class ClassifierController {
    private val gson = Gson()

    @PostMapping("/predict")
    fun predict(@RequestBody segmentationRequest: SegmentationRequest): ResponseEntity<*> {
        require(segmentationRequest.modelName.isNotBlank()) { "Model name is required" }
        require(segmentationRequest.image.isNotBlank()) { "Image is required" }

        val module = ModelRepository.readModelFromResources(segmentationRequest.modelName)
        val imageSize = module.second!!.size(segmentationRequest.modelName)
        val baseImage = base64ToImage(segmentationRequest.image, imageSize, imageSize)
        require(baseImage != null) { "Image is not valid" }

        val normMeanRGB = floatArrayOf(0.0f, 0.0f, 0.0f)
        val normStdRGB = floatArrayOf(1.0f, 1.0f, 1.0f)
        val input = TensorImageUtils.bitmapToFloat32Tensor(
            baseImage,
            normMeanRGB,
            normStdRGB
        )

        val outTensors: Array<IValue> = module.second!!.forward(IValue.from(input)).toTuple()
        val compute = Compute(listOf(*outTensors), module.second!!.size(segmentationRequest.modelName))

        val transferredBitmaps = runBlocking {
            val transferJobs = (0 until 8).map { index ->
                async {
                    when (index) {
                        in 0..3 -> createScaledBufferedImage(
                            compute.get_q_mask(index),
                            baseImage.width,
                            baseImage.height
                        )

                        else -> createScaledBufferedImage(
                            compute.get_q_depth(index - 4),
                            baseImage.width,
                            baseImage.height
                        )
                    }
                }
            }
            transferJobs.awaitAll()
        }

        val encodedBitmaps = transferredBitmaps.map { bitmap ->
            bitmap.toByteArray()
        }

        val response = SegmentationResponse(encodedBitmaps)
        return ResponseEntity.ok<Any>(gson.toJson(response))
    }
}
