package com.happymax.realtimebuscnwear

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 定義 UI 狀態
sealed interface BusUiState {
    data object Loading : BusUiState
    data class Success(val responseList: MutableList<BusResponse?>) : BusUiState
    data class Error(val message: String) : BusUiState
}

class BusViewModel() : ViewModel() {

    // 使用 StateFlow 來持有 UI 狀態
    private val _uiState = MutableStateFlow<BusUiState>(BusUiState.Loading)
    val uiState: StateFlow<BusUiState> = _uiState.asStateFlow()

    fun fetchBusListData(busList:List<LolimiBusApiParam>) {
        // 設置為加載狀態
        _uiState.update { BusUiState.Loading }

        // 在 ViewModel 的 CoroutineScope 中啟動協程
        viewModelScope.launch {
            try {
                // 調用 API
                /*val response = if (o.isEmpty()) RetrofitClient.api.getBusData(type, city, line)
                else RetrofitClient.api.getReverseBusData(type, city, line, o)*/
                val responseList = mutableListOf<BusResponse?>()
                for (bus in busList){
                    val response = LolimiBusApi.getBusData(bus.type, bus.city, bus.line, bus.o)
                    if(response != null && response.code == 200)
                        responseList.add(response)
                }
                var first = responseList.first()
                // 檢查 API 內部的 code (可選，但建議)
                if (responseList.isNotEmpty()) {
                    _uiState.update { BusUiState.Success(responseList) }
                } else
                {
                    _uiState.update { BusUiState.Error("API 錯誤: ${responseList}") }
                }
            } catch (e: Exception) {
                // 處理網路錯誤或解析錯誤
                _uiState.update { BusUiState.Error(e.message ?: "未知錯誤") }
            }
        }
    }
}