package com.example.demo

import org.pytorch.Module
import org.springframework.core.io.ClassPathResource
import java.nio.file.Paths


object ModelRepository {
    private val modelMap = mutableMapOf<String, Module>()
    fun readModelFromResources(modelName: String): Pair<String, Module?> {
        if (modelMap.containsKey(modelName)) {
            return Pair(modelName, modelMap[modelName])
        }
		System.out.println("User Dir============= " + System.getProperty("user.dir"))
            return Pair(modelName, Module.load(System.getProperty("user.dir")+ "/" + modelName)
                .apply {
                    modelMap[modelName] = this
                })

        throw Exception("Model not found")
    }

    fun modelExists(modelName: String): Boolean {
        return ClassPathResource(modelName).exists()
    }

}

fun Module.size(modelName: String): Int {
    var subName: String = modelName.substring(modelName.length - 7, modelName.length - 4)
    if (subName.startsWith("_")) {
        subName = subName.substring(1)
    }
    return subName.toInt()
}
