package com.happymax.realtimebus.wear.presentation

import android.app.Application
import kotlin.getValue
import com.happymax.realtimebus.shared.local.AppDatabase
import com.happymax.realtimebus.shared.repository.BusRepository

class BusApplication : Application() {
    //private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.getInstance(this) }

    val repository by lazy { BusRepository(database.favoriteStationDao()) }

    override fun onCreate() {
        super.onCreate()
        /*applicationScope.launch {
            repository.seedInitialFavoritesIfEmpty()
        }*/
    }
}