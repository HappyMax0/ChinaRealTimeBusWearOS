package com.happymax.realtimebus.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// 备用静态配色（用于 Android 11 及以下版本，或关闭动态取色时）
private val DarkColorScheme = darkColorScheme(
    primary = TransitPrimary,
    secondary = TransitSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = TransitPrimary,
    secondary = TransitSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 关键点：开启 dynamicColor（默认 Android 12+ 生效）
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Android 12 (API 31) 及以上系统支持从系统壁纸动态取色 (Material You)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // 确保有默认 Typography
        content = content
    )
}