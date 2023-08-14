package com.example.demo

import com.google.gson.annotations.SerializedName

data class SegmentationResponse(
    @SerializedName("result")
    val result: List<String>,
)
