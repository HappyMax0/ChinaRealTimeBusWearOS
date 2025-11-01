package com.happymax.realtimebuscnwear

// 假设您使用的是像 Gson 或 Moshi 这样的库，需要引入相应的注解
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BusResponse(
    // 如果使用 Gson
    @SerialName("code")
    val code: Int,

    @SerialName("city")
    val city: String,

    @SerialName("line")
    val line: String,

    @SerialName("car_count")
    val carCount: Int, // Kotlin推荐使用驼峰命名法 for car_count

    @SerialName("data")
    val data: List<BusInfo>
)

//---------------------------------------------------------------------------------

