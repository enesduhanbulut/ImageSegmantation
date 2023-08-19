package com.example.starter

import com.example.starter.service.RestService
import com.example.starter.verticle.HTTPVerticle
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.DeploymentOptions
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlin.system.exitProcess

class MainVerticle : CoroutineVerticle() {
  private val logger = LoggerFactory.getLogger(this::class.java)

  override suspend fun start() {
    vertx.exceptionHandler { e ->
      logger.error("unhandledThrowable: ${e.message}", e.cause)
      exitProcess(1)
    }

    val configBuffer = vertx.fileSystem().readFileBlocking("config.json")
    val configOptions = ConfigStoreOptions().setType("json").setConfig(JsonObject(configBuffer))
    val retrieverOptions = ConfigRetrieverOptions().addStore(configOptions)
    val retriever = ConfigRetriever.create(vertx, retrieverOptions)

    val config = retriever.config.await()

    vertx.deployVerticle(HTTPVerticle(), DeploymentOptions().setConfig(config)).await()
    vertx.deployVerticle(RestService::class.java, DeploymentOptions().setConfig(config).setWorker(true).setInstances(4).setWorkerPoolSize(40)).await()


  }
}
