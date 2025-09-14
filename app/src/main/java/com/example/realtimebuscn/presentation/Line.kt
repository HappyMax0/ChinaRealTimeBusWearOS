package com.example.realtimebuscn.presentation

import java.time.LocalTime

data class Line(val name:String, var terminal:String, val station:List<LineStation>)
