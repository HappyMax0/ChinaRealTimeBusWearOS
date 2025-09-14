package com.example.realtimebuscn.presentation

import kotlin.time.Duration

data class LineStation(val name:String, val stationName:String, val terminalStation:String, var remainStations:Int, var remainTime:Duration)