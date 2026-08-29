package com.happymax.realtimebus.shared.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteStationDao {
    @Query("SELECT * FROM favorite_stations ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<com.happymax.realtimebus.shared.local.FavoriteStationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(station: com.happymax.realtimebus.shared.local.FavoriteStationEntity)

    @Query("DELETE FROM favorite_stations WHERE stationId = :stationId")
    suspend fun deleteFavorite(stationId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stations WHERE stationId = :stationId)")
    fun isFavorite(stationId: String): Flow<Boolean>

    @Query("SELECT * FROM favorite_stations WHERE stationId = :stationId LIMIT 1")
    suspend fun getFavoriteById(stationId: String): com.happymax.realtimebus.shared.local.FavoriteStationEntity?
}