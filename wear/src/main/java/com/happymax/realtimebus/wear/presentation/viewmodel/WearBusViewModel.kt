package com.happymax.realtimebus.wear.presentation.viewmodel

import com.happymax.realtimebus.wear.presentation.data.WearDataReceiver
import com.happymax.realtimebus.wear.presentation.data.WearLocalCache
import com.happymax.realtimebus.shared.model.BusStation
import android.util.Log
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.happymax.realtimebus.shared.model.BusLineInfo
import com.happymax.realtimebus.shared.repository.BusRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BusUiState(
    val favoriteStations: List<BusStation> = emptyList(),
    val selectedCity: String = "上海",
    val isRefreshing: Boolean = false,
    val refreshCountdown: Int = 60,
    val selectedLineDetail: BusLineInfo? = null,
    val selectedStationDetail: BusStation? = null,
    val favoritesFilterQuery: String = "",
    val messageSnackbar: String? = null
)

private data class Tuple2<A, B>(
    val a: A,
    val b: B
)

private data class Tuple4<A, B, C, D>(
    val a: A,
    val b: B,
    val c: C,
    val d: D
)

class WearBusViewModel(application: Application, private val repository: BusRepository) : AndroidViewModel(application) {

    private val localCache = WearLocalCache(application)
    private val dataReceiver = WearDataReceiver(application)

    // Moshi 解析器
    private val moshi = Moshi.Builder().build()
    private val listType = Types.newParameterizedType(List::class.java, BusStation::class.java)
    private val listAdapter = moshi.adapter<List<BusStation>>(listType)

    val favoriteStations: StateFlow<List<BusStation>> = repository.favoriteStationsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedCity = MutableStateFlow("上海")
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _refreshCountdown = MutableStateFlow(60)
    val refreshCountdown: StateFlow<Int> = _refreshCountdown.asStateFlow()

    private val _selectedLineDetail = MutableStateFlow<BusLineInfo?>(null)
    val selectedLineDetail: StateFlow<BusLineInfo?> = _selectedLineDetail.asStateFlow()

    private val _selectedStationDetail = MutableStateFlow<BusStation?>(null)
    val selectedStationDetail: StateFlow<BusStation?> = _selectedStationDetail.asStateFlow()

    private val _favoritesFilterQuery = MutableStateFlow("")
    val favoritesFilterQuery: StateFlow<String> = _favoritesFilterQuery.asStateFlow()

    private val _messageSnackbar = MutableStateFlow<String?>(null)
    val messageSnackbar: StateFlow<String?> = _messageSnackbar.asStateFlow()

    // Trigger tick to dynamically recalculate real-time arrival info on intervals
    private val _refreshTicker = MutableStateFlow(System.currentTimeMillis())

    private val _stateParams = combine(
        _selectedCity,
        _isRefreshing
    ) { city, isRefreshing ->
        Tuple2(city, isRefreshing)
    }

    private val _uiExtraParams = combine(
        _refreshCountdown,
        _selectedLineDetail,
        _favoritesFilterQuery,
        _messageSnackbar
    ) { countdown, lineDetail, filterQuery, message ->
        Tuple4(countdown, lineDetail, filterQuery, message)
    }

    val uiState: StateFlow<com.happymax.realtimebus.wear.presentation.viewmodel.BusUiState> = combine(
        favoriteStations,
        _stateParams,
        _uiExtraParams
    ) { favorites, state, extra ->
        val (city, isRefreshing) = state
        val (countdown, lineDetail, filterQuery, message) = extra

        val filteredFavs = if (filterQuery.isBlank()) {
            favorites
        } else {
            favorites.filter { station ->
                station.name.contains(filterQuery, ignoreCase = true) ||
                        station.city.contains(filterQuery, ignoreCase = true) ||
                        station.lines.any { it.lineName.contains(filterQuery, ignoreCase = true) }
            }
        }

        BusUiState(
            favoriteStations = filteredFavs,
            selectedCity = city,
            isRefreshing = isRefreshing,
            refreshCountdown = countdown,
            selectedLineDetail = lineDetail,
            selectedStationDetail = _selectedStationDetail.value,
            favoritesFilterQuery = filterQuery,
            messageSnackbar = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BusUiState()
    )


    private var countdownJob: kotlinx.coroutines.Job? = null

    private fun startAutoRefreshLoop() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                for (i in 60 downTo 1) {
                    _refreshCountdown.value = i
                    delay(1000)
                }
                _refreshCountdown.value = 0
                _refreshTicker.value = System.currentTimeMillis()
                // You might want to update the favorite stations data here 
                // by fetching them again if repository allows that
            }
        }
    }

    init {
        // 1. 启动时：立刻监听本地 DataStore，有数据就解析并显示
        observeLocalCache()

        // 2. 启动时：开启手机同步监听
        dataReceiver.startListening()
        startAutoRefreshLoop()
    }

    private fun observeLocalCache() {
        viewModelScope.launch {
            repository.favoriteStationsFlow.collect { _ ->
                // Empty collect to force the flow to start emitting and querying the DB immediately
            }
        }
        viewModelScope.launch {
            localCache.favoritesJsonFlow
                .catch { e -> Log.e("WearSync", "读取本地缓存失败", e) }
                .collect { jsonString ->
                    if (!jsonString.isNullOrBlank()) {
                        // 将本地 JSON 反序列化为对象列表，并刷新 UI
                        val stations = listAdapter.fromJson(jsonString) ?: emptyList()
                        // Update repository with the parsed stations to update favoriteStations Flow
                         for(station in stations) {
                             repository.toggleFavorite(station)
                         }
                    }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // ViewModel 销毁时停止监听，防止内存泄漏
        dataReceiver.stopListening()
    }

    fun manualRefresh() {
        _refreshCountdown.value = 60
        _refreshTicker.value = System.currentTimeMillis()
    }
}

class WearBusViewModelFactory(private val application: Application,
                              private val repository: BusRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WearBusViewModel::class.java)) {
            return WearBusViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
