package com.happymax.realtimebus.data.manager

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.happymax.realtimebus.shared.model.BusStation

class WearSyncManager(private val context: Context) {

    // 取得 DataClient 實例
    private val dataClient = Wearable.getDataClient(context)

    // 定義一個固定的路徑 (Path)，手錶端會監聽這個路徑
    private val FAVORITES_PATH = "/bus_favorites"

    // 初始化 Moshi 和 Adapter
    private val moshi = Moshi.Builder().build()
    private val listType = Types.newParameterizedType(List::class.java, BusStation::class.java)
    private val listAdapter = moshi.adapter<List<BusStation>>(listType)

    fun sendStationsToWearable(stations: List<BusStation>) {
        // 1. 將 List 序列化為 JSON 字串
        val jsonString = listAdapter.toJson(stations)

        // 2. 建立 PutDataMapRequest，指定路徑
        val putDataReq = PutDataMapRequest.create(FAVORITES_PATH).apply {
            // 將 JSON 字串存入 DataMap
            dataMap.putString("station_list", jsonString)

            // 💡 關鍵：加入時間戳記！
            // DataClient 會比對資料是否有變更。如果 JSON 完全一樣，它不會觸發手錶更新。
            // 加上時間戳，確保每次發送都會被視為「新資料」強制同步。
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }.asPutDataRequest()

        // 3. 執行發送
        dataClient.putDataItem(putDataReq)
            .addOnSuccessListener {
                Log.d("WearSync", "發送成功！路徑: $FAVORITES_PATH")
            }
            .addOnFailureListener { e ->
                Log.e("WearSync", "發送失敗", e)
            }
    }
}