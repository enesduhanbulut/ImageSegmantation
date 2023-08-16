package com.example.starter.controller

import com.example.starter.util.coroutineHandler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.json.schema.SchemaParser
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.await

suspend fun restRoute(vertx: Vertx, schemaParser: SchemaParser): Router {
  val router = Router.router(vertx)

  router
    .post("/classifier/predict")
    .produces("application/json")
    .coroutineHandler { ctx ->
      val body = ctx.bodyAsJson
      val modelName = body.getString("modelName")
      val image = body.getString("image")
      val result = vertx.eventBus().request<JsonArray>(
        "message",
        jsonObjectOf(
          "modelName" to modelName,
          "image" to image
        )
      ).await().body()
      ctx.json(
        jsonObjectOf(
          "statusCode" to 200,
          "msg" to null,
          "data" to result.toString(),
        )
      )
    }

  return router
}
