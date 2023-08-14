package com.example.demo

import com.google.gson.annotations.SerializedName

data class SegmentationRequest(
    @SerializedName("model_name")
    val modelName: String,
    @SerializedName("image")
    val image: String,
)
