package com.happymax.realtimebus.shared.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_stations")
data class FavoriteStationEntity(
    @PrimaryKey val stationId: String,
    val name: String,
    val city: String,
    val adcode: String = "",
    val location: String = "",
    val address: String = "",
    val busLinesJson: String = "[]",
    val addedAt: Long = System.currentTimeMillis()
)

