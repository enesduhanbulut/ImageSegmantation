package com.example.starter.service

import ModelRepository
import base64ToImage
import com.example.starter.util.Compute
import com.example.starter.util.TensorImageUtils
import createScaledBufferedImage
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.*
import org.pytorch.IValue
import size
import toByteArray

class RestService : CoroutineVerticle() {

    override suspend fun start() {
        vertx.eventBus().consumer<JsonObject>("message").handler { message ->
            launch(Dispatchers.IO) {
                val image = message.body().getString("image")
                val model = message.body().getString("modelName")

                val module = ModelRepository.readModelFromResources(model)
                val imageSize = module.second!!.size(model)

                val baseImage = base64ToImage(image, imageSize, imageSize)

                val normMeanRGB = floatArrayOf(0.0f, 0.0f, 0.0f)
                val normStdRGB = floatArrayOf(1.0f, 1.0f, 1.0f)
                val input = TensorImageUtils.bitmapToFloat32Tensor(
                    baseImage, normMeanRGB, normStdRGB
                )

                val computeDeferred = async(Dispatchers.Default) {
                    val outTensors: Array<IValue> = module.second!!.forward(IValue.from(input)).toTuple()
                    Compute(listOf(*outTensors), module.second!!.size(model)).apply { init() }
                }
                val compute = computeDeferred.await()

                val transferredBitmaps = (0 until 8).parallelMap { index ->
                    when (index) {
                        in 0..3 -> createScaledBufferedImage(
                            compute.get_q_mask(index), baseImage.width, baseImage.height
                        )

                        else -> createScaledBufferedImage(
                            compute.get_q_depth(index - 4), baseImage.width, baseImage.height
                        )
                    }
                }

                val encodedBitmaps = transferredBitmaps.map { bitmap ->
                    bitmap.toByteArray()
                }

                message.reply(
                    jsonArrayOf(encodedBitmaps)
                )
            }
        }
    }

    suspend inline fun <T, R> Iterable<T>.parallelMap(
        crossinline transform: suspend (T) -> R
    ): List<R> = coroutineScope {
        map { async { transform(it) } }.awaitAll()
    }
}
