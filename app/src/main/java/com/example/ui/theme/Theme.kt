package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OpticalLabColorScheme = darkColorScheme(
    primary = LaserCyan,
    onPrimary = Color(0xFF041326),
    primaryContainer = Color(0xFF0B3558),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = SpectralViolet,
    onSecondary = Color(0xFF1B0D38),
    secondaryContainer = Color(0xFF2D1B54),
    onSecondaryContainer = Color(0xFFDDD6FE),
    tertiary = SpectralEmerald,
    onTertiary = Color(0xFF04261C),
    tertiaryContainer = Color(0xFF0B4635),
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = LabVoidBg,
    onBackground = TextPrimary,
    surface = LabSurface,
    onSurface = TextPrimary,
    surfaceVariant = LabSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = LabBorder,
    error = SpectralCrimson
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OpticalLabColorScheme,
        typography = Typography,
        content = content
    )
}
