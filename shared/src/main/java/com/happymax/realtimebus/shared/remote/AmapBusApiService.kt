package com.happymax.realtimebus.shared.remote

import com.happymax.realtimebus.shared.model.AmapBusLineResponse
import com.happymax.realtimebus.shared.model.AmapBusStopResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AmapBusApiService {

    /**
     * Search bus stops by name or keywords
     * Documentation: https://lbs.amap.com/api/webservice/guide/api-advanced/bus-inquiry
     */
    @GET("v3/bus/stopname")
    suspend fun searchBusStops(
        @Query("key") key: String,
        @Query("keywords") keywords: String,
        @Query("city") city: String? = null,
        @Query("offset") offset: Int = 20,
        @Query("page") page: Int = 1,
        @Query("output") output: String = "json"
    ): AmapBusStopResponse

    /**
     * Search bus lines by name or number (e.g. 919, 1, 快3)
     */
    @GET("v3/bus/linename")
    suspend fun searchBusLines(
        @Query("key") key: String,
        @Query("keywords") keywords: String,
        @Query("city") city: String? = null,
        @Query("extensions") extensions: String = "all",
        @Query("offset") offset: Int = 20,
        @Query("page") page: Int = 1,
        @Query("output") output: String = "json"
    ): AmapBusLineResponse

    /**
     * Search bus line by specific line ID
     */
    @GET("v3/bus/lineid")
    suspend fun getBusLineById(
        @Query("key") key: String,
        @Query("id") lineId: String,
        @Query("extensions") extensions: String = "all",
        @Query("output") output: String = "json"
    ): AmapBusLineResponse
}
