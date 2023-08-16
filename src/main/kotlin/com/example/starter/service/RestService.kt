package com.example.starter.service

import com.example.starter.util.*
import com.google.gson.Gson
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.pgclient.PgPool
import kotlinx.coroutines.*
import org.pytorch.IValue

class RestService() : CoroutineVerticle() {

  override suspend fun start() {
    val gson = Gson()
    vertx.eventBus().consumer<JsonObject>("message").handler { message ->
      launch{
        val image = message.body().getString("image")
        val model = message.body().getString("modelName")

        val module = ModelRepository.readModelFromResources(model)
        val imageSize = module.second!!.size(model)

        val baseImage = base64ToImage(image, imageSize, imageSize)

        val normMeanRGB = floatArrayOf(0.0f, 0.0f, 0.0f)
        val normStdRGB = floatArrayOf(1.0f, 1.0f, 1.0f)
        val input = TensorImageUtils.bitmapToFloat32Tensor(
          baseImage,
          normMeanRGB,
          normStdRGB
        )

        val outTensors: Array<IValue> = module.second!!.forward(IValue.from(input)).toTuple()
        val compute = Compute(listOf(*outTensors), module.second!!.size(model))

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

        message.reply(
          jsonArrayOf(encodedBitmaps)
        )
      }
    }
  }
}
