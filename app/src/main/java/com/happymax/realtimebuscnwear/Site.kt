package com.happymax.realtimebuscnwear

import kotlinx.serialization.Serializable

@Serializable
data class Site(val name:String, var isReverse: Boolean)
