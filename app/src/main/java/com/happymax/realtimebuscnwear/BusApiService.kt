package com.happymax.realtimebuscnwear

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface BusApiService {
    // 替换为实际的 API 路径
    @GET("API/che/api.php")
    suspend fun getReverseBusData(@Query("type") type: String,
                                  @Query("city") city: String,
                                  @Query("line") line: String,
                                  @Query("o") o: String): BusResponse

    @Headers(
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36 Edg/118.0.2088.57"
    )
    @GET("API/che/api.php")
    suspend fun getBusData(@Query("type") type: String,
                           @Query("city") city: String,
                           @Query("line") line: String): BusResponse
}