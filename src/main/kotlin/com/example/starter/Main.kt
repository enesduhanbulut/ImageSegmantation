package com.example.starter

import io.vertx.core.Vertx
class Main{
  companion object{
    @JvmStatic
    fun main() {
      Vertx.vertx().deployVerticle(MainVerticle::class.java.name)
    }

  }
}

