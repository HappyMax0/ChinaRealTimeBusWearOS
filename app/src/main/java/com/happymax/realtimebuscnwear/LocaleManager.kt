package com.happymax.realtimebuscnwear

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleManager {
    fun setLocale(context: Context?, locale: Locale): Context? {
        if (context == null) return null

        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}