package com.happymax.realtimebus.shared.repository

import android.util.Log
import com.happymax.realtimebus.shared.BuildConfig
import com.happymax.realtimebus.shared.local.FavoriteStationDao
import com.happymax.realtimebus.shared.local.FavoriteStationEntity
import com.happymax.realtimebus.shared.model.BusLineInfo
import com.happymax.realtimebus.shared.model.BusStation
import com.happymax.realtimebus.shared.remote.AmapApiClient
import com.happymax.realtimebus.shared.remote.AmapBusApiService
import com.happymax.realtimebus.shared.remote.SimulatedTransitEngine
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class BusRepository(
    private val favoriteStationDao: FavoriteStationDao,
    private val apiService: AmapBusApiService = AmapApiClient.apiService
) {
    private val moshi = AmapApiClient.moshiInstance
    private val listType = Types.newParameterizedType(List::class.java, BusLineInfo::class.java)
    private val jsonAdapter = moshi.adapter<List<BusLineInfo>>(listType)

    val favoriteStationsFlow: Flow<List<BusStation>> = favoriteStationDao.getAllFavorites()
        .map { entities ->
            entities.map { entity ->
                val lines = try {
                    jsonAdapter.fromJson(entity.busLinesJson) ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
                // Refresh dynamic real-time arrival info with current timestamp
                val updatedLines = lines.map { line ->
                    line.copy(
                        realtime = SimulatedTransitEngine.calculateRealtimeArrival(
                            lineName = line.lineName,
                            stationId = entity.stationId
                        )
                    )
                }
                BusStation(
                    id = entity.stationId,
                    name = entity.name,
                    city = entity.city,
                    adcode = entity.adcode,
                    location = entity.location,
                    address = entity.address,
                    lines = updatedLines,
                    isFavorite = true
                )
            }
        }
        .flowOn(Dispatchers.IO)

    suspend fun isFavorite(stationId: String): Boolean = withContext(Dispatchers.IO) {
        favoriteStationDao.getFavoriteById(stationId) != null
    }

    fun isFavoriteFlow(stationId: String): Flow<Boolean> {
        return favoriteStationDao.isFavorite(stationId)
    }

    suspend fun addFavorite(station: BusStation) = withContext(Dispatchers.IO) {
        val linesJson = try {
            jsonAdapter.toJson(station.lines)
        } catch (e: Exception) {
            "[]"
        }
        val entity = FavoriteStationEntity(
            stationId = station.id,
            name = station.name,
            city = station.city,
            adcode = station.adcode,
            location = station.location,
            address = station.address,
            busLinesJson = linesJson,
            addedAt = System.currentTimeMillis()
        )
        favoriteStationDao.insertFavorite(entity)
    }

    suspend fun removeFavorite(stationId: String) = withContext(Dispatchers.IO) {
        favoriteStationDao.deleteFavorite(stationId)
    }

    suspend fun toggleFavorite(station: BusStation): Boolean = withContext(Dispatchers.IO) {
        val exists = favoriteStationDao.getFavoriteById(station.id) != null
        if (exists) {
            favoriteStationDao.deleteFavorite(station.id)
            false
        } else {
            addFavorite(station)
            true
        }
    }

    /**
     * Search bus stops using AMap Web Service API with fallback to simulated engine
     */
    suspend fun searchStations(query: String, city: String?): List<BusStation> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.AMAP_KEY
        } catch (e: Exception) {
            ""
        }

        val hasValidApiKey = apiKey.isNotBlank() && !apiKey.contains("placeholder")

        if (hasValidApiKey) {
            try {
                Log.d("BusRepository", "Calling AMap Web Service for query: $query, city: $city")
                val response = apiService.searchBusStops(
                    key = apiKey,
                    keywords = query,
                    city = "021",
                    offset = 20,
                    page = 1
                )

                if (response.status == "1" && !response.busstops.isNullOrEmpty()) {
                    return@withContext response.busstops?.map { dto ->
                        val lines = dto.buslines?.map { lineDto ->
                            val direction = if (!lineDto.startStop.isNullOrBlank() && !lineDto.endStop.isNullOrBlank()) {
                                "${lineDto.startStop} ➔ ${lineDto.endStop}"
                            } else {
                                lineDto.name
                            }
                            BusLineInfo(
                                lineId = lineDto.id.ifBlank { "L_${lineDto.name.hashCode()}" },
                                lineName = lineDto.name,
                                lineShortName = lineDto.name.substringBefore("路").substringBefore("("),
                                direction = direction,
                                startStop = lineDto.startStop ?: "",
                                endStop = lineDto.endStop ?: "",
                                startTime = lineDto.startTime ?: "06:00",
                                endTime = lineDto.endTime ?: "22:00",
                                price = lineDto.basicPrice?.let { "${it}元" } ?: "2元",
                                type = lineDto.type ?: "常规公交",
                                realtime = SimulatedTransitEngine.calculateRealtimeArrival(lineDto.name, dto.id)
                            )
                        } ?: emptyList()

                        BusStation(
                            id = dto.id.ifBlank { "AMAP_STOP_${dto.name.hashCode()}" },
                            name = dto.name,
                            city = city ?: "城市",
                            adcode = dto.adcode ?: "",
                            location = dto.location,
                            address = dto.address ?: "公共交通站点",
                            lines = lines,
                            isFavorite = favoriteStationDao.getFavoriteById(dto.id) != null
                        )
                    } ?: emptyList()
                }
            } catch (e: Exception) {
                Log.w("BusRepository", "AMap API call failed, falling back to simulated data", e)
            }
        }

        // Fallback to rich preloaded/simulated stations
        val preset = SimulatedTransitEngine.searchPresetStations(query, city)
        preset.map { station ->
            val isFav = favoriteStationDao.getFavoriteById(station.id) != null
            station.copy(isFavorite = isFav)
        }
    }

    /**
     * Preload default initial favorite station if the table is empty
     */
    suspend fun seedInitialFavoritesIfEmpty() = withContext(Dispatchers.IO) {
        val initialPreset = SimulatedTransitEngine.getPreloadedStations().take(2)
        initialPreset.forEach { station ->
            if (favoriteStationDao.getFavoriteById(station.id) == null) {
                addFavorite(station)
            }
        }
    }
}
