package com.happymax.realtimebuscnwear

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object LolimiBusApi {

    private const val url = "https://api.lolimi.cn/API/che/api.php"

    suspend fun getBusData(type: String, city: String, line: String, o: String): BusResponse? = withContext(
        Dispatchers.IO) {
        // 请求参数
        var params = ""
        params += "type=" + URLEncoder.encode(type, "UTF-8") + "&"  // 默认返回text可写json/text
        params += "city=" + URLEncoder.encode(city, "UTF-8") + "&"  // 城市名称
        params += "line=" + URLEncoder.encode(line, "UTF-8") + "&"  // 车站名称
        params += "o=" + URLEncoder.encode(o, "UTF-8") + "&"  // 写入2即可查询反方向
        params = params.substring(0, params.length - 1)

        // 创建连接
        val conn = URL(url + "?" + params).openConnection() as HttpURLConnection
        conn.requestMethod = "GET" // 注意：这里通常应是 "GET" 或 "POST" 的大写形式
        // 增加连接超时和读取超时（可选，但推荐）
        conn.connectTimeout = 5000 // 5秒
        conn.readTimeout = 5000    // 5秒

        try {
            // 3. 获取响应
            val responseCode = conn.responseCode

            // 检查响应码，使用 try-catch 处理可能抛出的异常（例如 4xx/5xx 错误可能导致 getInputStream 失败）
            if (responseCode !in 200..299) {
                // 如果响应失败，尝试读取错误流
                val errorResponse = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error stream"

                Log.e("API", errorResponse)

                throw RuntimeException("HTTP 错误: $responseCode. 错误详情: $errorResponse")
            }

            // 获取响应
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            Log.d("API", response)
            // 或者如果需要保留行分隔符，可以使用以下方式（更接近您原始 Java 代码的行为）：
            // val response = conn.inputStream.bufferedReader().lineSequence().joinToString("\n")
            // 创建一个 Json 实例。通常使用默认的实例即可。
            // 如果您的 JSON 包含对象类中没有的字段，可能需要配置它。
            val json = Json {
                // 例如：如果 JSON 中包含您的数据类没有定义的字段，使用此选项忽略它们
                ignoreUnknownKeys = true
            }

            // 使用 decodeFromString 扩展函数将 JSON 字符串转换为 MyResponse 对象
            val responseObject: BusResponse = json.decodeFromString(response)
            return@withContext responseObject

        } catch (e: Exception) {
            // 处理解析过程中可能出现的异常，例如格式错误 (SerializationException)
            println("JSON 解析失败: ${e.message}")
            e.printStackTrace()
        }

        return@withContext null
    }

    suspend fun saveList(dataStore: DataStore<Preferences>, list: List<Site>) {
        val json = Json {
            // 例如：如果 JSON 中包含您的数据类没有定义的字段，使用此选项忽略它们
            prettyPrint  = true
            ignoreUnknownKeys = true
        }

        val jsonStr = json.encodeToString  (list)

        val key = stringPreferencesKey("SITE_LIST_JSON")
        dataStore.edit { preferences ->
            preferences[key] = jsonStr
        }
    }

    suspend fun getList(dataStore: DataStore<Preferences>): List<Site> {
        val key = stringPreferencesKey("SITE_LIST_JSON")
        val siteFlow: Flow<String> = dataStore.data.catch {
            if(it is IOException) {
                Log.e("API", "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            preferences[key] ?: "[]"
        }

        val currentSiteListJson = siteFlow.first()

        Log.d("API", "currentSiteListJson: ${currentSiteListJson}")

        val json = Json {
            // 例如：如果 JSON 中包含您的数据类没有定义的字段，使用此选项忽略它们
            ignoreUnknownKeys = true
        }
        val list: List<Site> = json.decodeFromString(currentSiteListJson)

        return list
    }
}