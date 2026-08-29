package com.happymax.realtimebus.wear.presentation.data

import com.happymax.realtimebus.shared.model.BusStation
import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WearDataReceiver(context: Context) {
    private val dataClient: DataClient = Wearable.getDataClient(context)
    private val localCache = WearLocalCache(context)
    private val FAVORITES_PATH = "/bus_favorites"

    // 独立一个协程作用域用于写入磁盘
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val listener = DataClient.OnDataChangedListener { dataEvents ->
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == FAVORITES_PATH) {
                val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                val jsonString = dataMapItem.dataMap.getString("station_list")

                if (jsonString != null) {
                    // 收到手机传来的最新数据，直接写入本地 DataStore！
                    ioScope.launch {
                        localCache.saveFavoritesJson(jsonString)
                        Log.d("WearSync", "已将手机同步的新数据存入本地缓存")
                    }
                }
            }
        }
    }

    fun startListening() {
        dataClient.addListener(listener)
    }

    fun stopListening() {
        dataClient.removeListener(listener)
    }
}