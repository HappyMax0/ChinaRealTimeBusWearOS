package com.happymax.realtimebus.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Typography

// 自定义主色调（如果不想要死板的深蓝，可以使用现代薄荷绿/青空蓝/活力橙等）
val M3Primary = Color(0xFF70D2FF)        // 柔和高亮主色（用于重点强调、图标）
val M3OnPrimary = Color(0xFF003548)      // 主色上的文字
val M3PrimaryContainer = Color(0xFF004D67) // 主容器背景色（路牌号胶囊等）
val M3OnPrimaryContainer = Color(0xFFC3E8FF)

val M3Secondary = Color(0xFFB4CAD6)      // 次要强调色
val M3SecondaryContainer = Color(0xFF354A53) // 城市 Tag 等次要容器
val M3OnSecondaryContainer = Color(0xFFCFE6F2)

val M3Tertiary = Color(0xFFC6C2EA)       // 第三强调色
val M3TertiaryContainer = Color(0xFF433F62)

val M3SurfaceContainer = Color(0xFF1E2022)     // 站点大卡片背景色
val M3SurfaceContainerHigh = Color(0xFF282B2E) // 刷新按钮、子组件背景色
val M3OnSurface = Color(0xFFE2E2E5)            // 主要文字颜色
val M3OnSurfaceVariant = Color(0xFFC4C7C8)     // 次要描述文字颜色
val M3Outline = Color(0xFF8E9192)              // 辅助弱化线条/文字

// 组装 Wear OS 3 专属暗色 ColorScheme
val WearColorScheme = ColorScheme(
    primary = M3Primary,
    onPrimary = M3OnPrimary,
    primaryContainer = M3PrimaryContainer,
    onPrimaryContainer = M3OnPrimaryContainer,

    secondary = M3Secondary,
    secondaryContainer = M3SecondaryContainer,
    onSecondaryContainer = M3OnSecondaryContainer,

    tertiary = M3Tertiary,
    tertiaryContainer = M3TertiaryContainer,

    background = Color.Black,             // Wear OS 规范：全局背景强制纯黑
    onBackground = M3OnSurface,

    surfaceContainer = M3SurfaceContainer,
    surfaceContainerHigh = M3SurfaceContainerHigh,
    onSurface = M3OnSurface,
    onSurfaceVariant = M3OnSurfaceVariant,
    outline = M3Outline
)

@Composable
fun RealTimeBusTheme(
    content: @Composable () -> Unit
) {
    // 必须使用 androidx.wear.compose.material3.MaterialTheme
    MaterialTheme(
        colorScheme = WearColorScheme,
        typography = Typography(),
        content = content
    )
}