package com.happymax.realtimebus.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = ColorScheme(
    background = Color.Black
)

@Composable
fun RealTimeBusTheme(
    content: @Composable () -> Unit
) {
    /**
     * Empty theme to customize for your app.
     * See: https://developer.android.com/jetpack/compose/designsystems/custom
     */
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}