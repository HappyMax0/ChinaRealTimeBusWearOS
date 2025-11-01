package com.happymax.realtimebuscnwear

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://api.lolimi.cn/" // **必须替换为您的基础 URL**
    private val contentType = "application/json".toMediaType()

    // 配置 Json 解析器，忽略未知的鍵，以增加 API 變動時的穩健性
    private val json = Json {
        ignoreUnknownKeys = true
    }

    // 配置 OkHttp 並添加日誌攔截器 (方便調試)
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY // 打印請求和響應的詳細信息
            }
        )
        // 3. 【新功能】添加 User-Agent 攔截器，偽裝成 Chrome 瀏覽器
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val newRequest = originalRequest.newBuilder()
                // 設置一個看起來像真實瀏覽器的 User-Agent
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36 Edg/118.0.2088.57")
                .build()
            chain.proceed(newRequest)
        }
       // --- 建議添加以下配置 ---
        // 設置連接超時為 30 秒
       /* .connectTimeout(30, TimeUnit.SECONDS)
        // 設置讀取超時為 30 秒
        .readTimeout(30, TimeUnit.SECONDS)
        // 設置寫入超時為 30 秒
        .writeTimeout(30, TimeUnit.SECONDS)*/
        // 強制使用 HTTP/1.1 協議，避免伺服器 HTTP/2 實作不佳的問題
        //.protocols(listOf(Protocol.HTTP_1_1))
        .build()

    val api: BusApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient) // 使用配置好的 OkHttpClient
            .addConverterFactory(
                json.asConverterFactory(contentType)
            )
            .build()
            .create(BusApiService::class.java)
    }
}