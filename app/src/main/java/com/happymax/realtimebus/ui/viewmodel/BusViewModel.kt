package com.happymax.realtimebus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.happymax.realtimebus.shared.model.BusLineInfo
import com.happymax.realtimebus.shared.model.BusStation
import com.happymax.realtimebus.shared.repository.BusRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.happymax.realtimebus.data.manager.WearSyncManager

data class BusUiState(
    val favoriteStations: List<BusStation> = emptyList(),
    val searchResults: List<BusStation> = emptyList(),
    val searchQuery: String = "",
    val selectedCity: String = "上海",
    val isSearching: Boolean = false,
    val isRefreshing: Boolean = false,
    val refreshCountdown: Int = 60,
    val selectedLineDetail: BusLineInfo? = null,
    val selectedStationDetail: BusStation? = null,
    val favoritesFilterQuery: String = "",
    val messageSnackbar: String? = null
)

private data class Tuple5<A, B, C, D, E>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E
)

private data class Tuple4<A, B, C, D>(
    val a: A,
    val b: B,
    val c: C,
    val d: D
)

class BusViewModel(application: Application,
    private val repository: BusRepository
) : AndroidViewModel(application) {
    private val wearSyncManager:WearSyncManager= WearSyncManager(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCity = MutableStateFlow("上海")
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    private val _searchResults = MutableStateFlow<List<BusStation>>(emptyList())
    val searchResults: StateFlow<List<BusStation>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

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

    val favoriteStations: StateFlow<List<BusStation>> = repository.favoriteStationsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _stateParams = combine(
        _searchResults,
        _searchQuery,
        _selectedCity,
        _isSearching,
        _isRefreshing
    ) { results, query, city, isSearching, isRefreshing ->
        Tuple5(results, query, city, isSearching, isRefreshing)
    }

    private val _uiExtraParams = combine(
        _refreshCountdown,
        _selectedLineDetail,
        _favoritesFilterQuery,
        _messageSnackbar
    ) { countdown, lineDetail, filterQuery, message ->
        Tuple4(countdown, lineDetail, filterQuery, message)
    }

    val uiState: StateFlow<BusUiState> = combine(
        favoriteStations,
        _stateParams,
        _uiExtraParams
    ) { favorites, state, extra ->
        val (results, query, city, isSearching, isRefreshing) = state
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
            searchResults = results,
            searchQuery = query,
            selectedCity = city,
            isSearching = isSearching,
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

    private var countdownJob: Job? = null

    init {
        // Initial search to give rich suggestions on search tab
//        performSearch("")
        startAutoRefreshLoop()
    }

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
                // Re-trigger search or dynamic refresh
                if (_searchQuery.value.isNotBlank()) {
                    val updated = repository.searchStations(_searchQuery.value, _selectedCity.value)
                    _searchResults.value = updated
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            performSearch(query)
        }
    }

    fun onFavoritesFilterChanged(query: String) {
        _favoritesFilterQuery.value = query
    }

    fun onCitySelected(city: String) {
        _selectedCity.value = city
        performSearch(_searchQuery.value.ifBlank { "站" })
    }

    fun performSearch(query: String = _searchQuery.value) {
        viewModelScope.launch {
            _isSearching.value = true
            try {
                val results = repository.searchStations(query.ifBlank { "公交站" }, _selectedCity.value)
                _searchResults.value = results
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun toggleFavorite(station: BusStation) {
        viewModelScope.launch {
            val isNowFav = repository.toggleFavorite(station)
            val updatedResults = _searchResults.value.map {
                if (it.id == station.id) it.copy(isFavorite = isNowFav) else it
            }
            _searchResults.value = updatedResults
            _messageSnackbar.value = if (isNowFav) "已收藏站点: ${station.name}" else "已取消收藏: ${station.name}"

            //同步手表端
            wearSyncManager.sendStationsToWearable(favoriteStations.value)
        }
    }

    fun removeFavorite(stationId: String, stationName: String) {
        viewModelScope.launch {
            repository.removeFavorite(stationId)
            val updatedResults = _searchResults.value.map {
                if (it.id == stationId) it.copy(isFavorite = false) else it
            }
            _searchResults.value = updatedResults
            _messageSnackbar.value = "已取消收藏: $stationName"
        }
    }

    fun manualRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            wearSyncManager.sendStationsToWearable(favoriteStations.value)
            _isRefreshing.value = false
            _messageSnackbar.value = "收藏数据已同步到手表"
        }
    }

    fun showLineDetail(line: BusLineInfo, station: BusStation) {
        _selectedLineDetail.value = line
        _selectedStationDetail.value = station
    }

    fun dismissLineDetail() {
        _selectedLineDetail.value = null
        _selectedStationDetail.value = null
    }

    fun clearSnackbarMessage() {
        _messageSnackbar.value = null
    }
}

class BusViewModelFactory(private val application: Application,
    private val repository: BusRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BusViewModel::class.java)) {
            return BusViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
