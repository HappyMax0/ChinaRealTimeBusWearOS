package com.happymax.realtimebus.wear.presentation.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 在文件顶层声明 DataStore (确保它是单例)
val Context.dataStore by preferencesDataStore(name = "bus_cache")

class WearLocalCache(private val context: Context) {
    // 定义存取 JSON 用的 Key
    private val FAVORITES_JSON_KEY = stringPreferencesKey("favorites_json_string")

    // 读取流：只要本地数据有变动，这里就会立刻发出最新的 JSON
    // App 启动第一次收集时，也会立刻发出最后一次保存的数据
    val favoritesJsonFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[FAVORITES_JSON_KEY]
    }

    // 写入挂起函数：将手机传来的新 JSON 存入本地
    suspend fun saveFavoritesJson(jsonString: String) {
        context.dataStore.edit { preferences ->
            preferences[FAVORITES_JSON_KEY] = jsonString
        }
    }
}