package com.example.demo

import org.pytorch.IValue
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.google.gson.Gson
import java.awt.image.BufferedImage
import java.util.Base64

@RestController
@RequestMapping("/api/classifier")
class ClassifierController {
	val gson = Gson()
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
        val compute =
            Compute(listOf(*outTensors), module.second!!.size(segmentationRequest.modelName))
        val pre1: BufferedImage = createScaledBufferedImage(
            compute.getmask(2),
            baseImage.width,
            baseImage.height,
        )
        val pre2 = createScaledBufferedImage(
            compute.getdepth(2),
            baseImage.width,
            baseImage.height,
        )
        val transferredBitmap1 = createScaledBufferedImage(
            compute.get_q_mask(0),
            baseImage.width,
            baseImage.height,
        )
        val transferredBitmap2 = createScaledBufferedImage(
            compute.get_q_mask(1),
            baseImage.width,
            baseImage.height,
        )
        val transferredBitmap3 = createScaledBufferedImage(
            compute.get_q_mask(2),
            baseImage.width,
            baseImage.height,
        )
        val transferredBitmap4 = createScaledBufferedImage(
            compute.get_q_mask(3),
            baseImage.width,
            baseImage.height,
        )
        val transferredBitmap5 = createScaledBufferedImage(
            compute.get_q_depth(0),
            baseImage.width,
            baseImage.height,
        )
        val transferredBitmap6 = createScaledBufferedImage(
            compute.get_q_depth(1),
            baseImage.width,
            baseImage.height,
        )
        val transferredBitmap7 = createScaledBufferedImage(
            compute.get_q_depth(2),
            baseImage.width,
            baseImage.height,
        )
        val transferredBitmap8 = createScaledBufferedImage(
            compute.get_q_depth(3),
            baseImage.width,
            baseImage.height,
        )
        val encoder = Base64.getEncoder()
        // Return the prediction as a JSON string
        return ResponseEntity.ok<Any>(
            gson.toJson(SegmentationResponse(
                listOf(
                    transferredBitmap1.toByteArray(),
                    transferredBitmap2.toByteArray(),
                    transferredBitmap3.toByteArray(),
                    transferredBitmap4.toByteArray(),
                    transferredBitmap5.toByteArray(),
                    transferredBitmap6.toByteArray(),
                    transferredBitmap7.toByteArray(),
                    transferredBitmap8.toByteArray(),
                )
            ))
        )
    }


}
