package com.happymax.realtimebuscnwear

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BusInfo(
    @SerialName("lines")
    val lines: String,

    @SerialName("price")
    val price: String,

    @SerialName("endSn")
    val endSn: String,

    @SerialName("busId")
    val busId: String,

    @SerialName("reachtime")
    val reachtime: String,

    @SerialName("travelTime")
    val travelTime: String,

    @SerialName("surplus")
    val surplus: String
)
