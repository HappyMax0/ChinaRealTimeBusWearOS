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

private val DarkColorScheme =
    darkColorScheme(
        primary = TransitPrimaryDark,
        onPrimary = TransitOnPrimaryDark,
        primaryContainer = TransitPrimaryContainerDark,
        onPrimaryContainer = TransitOnPrimaryContainerDark,
        secondary = TransitSecondaryDark,
        onSecondary = TransitOnSecondaryDark,
        secondaryContainer = TransitSecondaryContainerDark,
        onSecondaryContainer = TransitOnSecondaryContainerDark,
        background = TransitBackgroundDark,
        onBackground = TransitOnBackgroundDark,
        surface = TransitSurfaceDark,
        onSurface = TransitOnSurfaceDark,
        surfaceVariant = TransitSurfaceVariantDark,
        onSurfaceVariant = TransitOnSurfaceVariantDark,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = TransitPrimary,
        onPrimary = TransitOnPrimary,
        primaryContainer = TransitPrimaryContainer,
        onPrimaryContainer = TransitOnPrimaryContainer,
        secondary = TransitSecondary,
        onSecondary = TransitOnSecondary,
        secondaryContainer = TransitSecondaryContainer,
        onSecondaryContainer = TransitOnSecondaryContainer,
        background = TransitBackground,
        onBackground = TransitOnBackground,
        surface = TransitSurface,
        onSurface = TransitOnSurface,
        surfaceVariant = TransitSurfaceVariant,
        onSurfaceVariant = TransitOnSurfaceVariant,
    )

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
