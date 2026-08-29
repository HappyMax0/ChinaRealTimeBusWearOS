package com.happymax.realtimebus.shared.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AmapBusStopResponse(
    @Json(name = "status") val status: String = "0",
    @Json(name = "info") val info: String = "",
    @Json(name = "infocode") val infocode: String = "",
    @Json(name = "count") val count: String = "0",
    @Json(name = "busstops") val busstops: List<BusStopDto>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class BusStopDto(
    @Json(name = "id") val id: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "location") val location: String = "",
    @Json(name = "address") val address: String? = null,
    @Json(name = "adcode") val adcode: String? = null,
    @Json(name = "citycode") val citycode: String? = null,
    @Json(name = "buslines") val buslines: List<BusLineDto>? = emptyList(),
    @Json(name = "sequence") val sequence: String? = null
)

@JsonClass(generateAdapter = true)
data class AmapBusLineResponse(
    @Json(name = "status") val status: String = "0",
    @Json(name = "info") val info: String = "",
    @Json(name = "infocode") val infocode: String = "",
    @Json(name = "count") val count: String = "0",
    @Json(name = "buslines") val buslines: List<BusLineDto>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class BusLineDto(
    @Json(name = "id") val id: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "type") val type: String? = null,
    @Json(name = "start_stop") val startStop: String? = null,
    @Json(name = "end_stop") val endStop: String? = null,
    @Json(name = "start_time") val startTime: String? = null,
    @Json(name = "end_time") val endTime: String? = null,
    @Json(name = "basic_price") val basicPrice: String? = null,
    @Json(name = "total_price") val totalPrice: String? = null,
    @Json(name = "distance") val distance: String? = null,
    @Json(name = "company") val company: String? = null,
    @Json(name = "busstops") val busstops: List<BusStopDto>? = emptyList()
)

// UI Domain Models
@JsonClass(generateAdapter = true)
data class BusStation(
    val id: String,
    val name: String,
    val city: String,
    val adcode: String = "",
    val location: String = "",
    val address: String = "",
    val lines: List<BusLineInfo> = emptyList(),
    val isFavorite: Boolean = false
)

@JsonClass(generateAdapter = true)
data class BusLineInfo(
    val lineId: String,
    val lineName: String,
    val lineShortName: String,
    val direction: String,
    val startStop: String = "",
    val endStop: String = "",
    val startTime: String = "06:00",
    val endTime: String = "22:00",
    val price: String = "2元",
    val type: String = "常规公交",
    val realtime: RealtimeArrivalInfo = RealtimeArrivalInfo()
)

@JsonClass(generateAdapter = true)
data class RealtimeArrivalInfo(
    val stopsAway: Int = 2,
    val etaMinutes: Int = 4,
    val etaDistanceMeters: Int = 1200,
    val statusText: String = "约 4 分钟到达",
    val statusType: ArrivalStatusType = ArrivalStatusType.ON_WAY,
    val crowdedness: CrowdednessLevel = CrowdednessLevel.COMFORTABLE,
    val busPlate: String = "京A·88392",
    val nextBusEtaMinutes: Int = 12
)

enum class ArrivalStatusType {
    ARRIVING_SOON, // 即将到站 (<2 mins)
    ON_WAY,        // 途中 (2-15 mins)
    WAITING_DEPARTURE, // 起点站等待发车
    OUT_OF_SERVICE // 停运/非运营时间
}

enum class CrowdednessLevel(val label: String) {
    COMFORTABLE("畅通舒适"),
    MODERATE("适中"),
    CROWDED("拥挤")
}
