package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.pow

// Deep Optical Laboratory Palette
val LabVoidBg = Color(0xFF070B16)
val LabSurface = Color(0xFF0E1528)
val LabSurfaceElevated = Color(0xFF16203A)
val LabSurfaceGlass = Color(0xFF1E2B4D)
val LabBorder = Color(0xFF283963)

val LaserCyan = Color(0xFF38BDF8)
val LaserCyanDim = Color(0xFF0284C7)
val SpectralViolet = Color(0xFFA78BFA)
val SpectralEmerald = Color(0xFF34D399)
val SpectralAmber = Color(0xFFFBBF24)
val SpectralCrimson = Color(0xFFF87171)

val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

/**
 * Converts a visible light wavelength in nanometers (380nm - 750nm)
 * to a physically inspired RGB Color using Dan Bruton's spectral approximation
 * enhanced for vivid display on dark optical lab canvases.
 */
fun wavelengthToColor(wavelengthNm: Float, alpha: Float = 1f): Color {
    val wl = wavelengthNm.coerceIn(380f, 750f)
    var r = 0f
    var g = 0f
    var b = 0f

    when {
        wl in 380f..440f -> {
            r = -(wl - 440f) / (440f - 380f)
            g = 0f
            b = 1f
        }
        wl in 440f..490f -> {
            r = 0f
            g = (wl - 440f) / (490f - 440f)
            b = 1f
        }
        wl in 490f..510f -> {
            r = 0f
            g = 1f
            b = -(wl - 510f) / (510f - 490f)
        }
        wl in 510f..580f -> {
            r = (wl - 510f) / (580f - 510f)
            g = 1f
            b = 0f
        }
        wl in 580f..645f -> {
            r = 1f
            g = -(wl - 645f) / (645f - 580f)
            b = 0f
        }
        wl in 645f..750f -> {
            r = 1f
            g = 0f
            b = 0f
        }
    }

    // Intensity falloff near the edges of the human visible spectrum
    val factor = when {
        wl in 380f..419f -> 0.35f + 0.65f * (wl - 380f) / (420f - 380f)
        wl in 420f..699f -> 1.0f
        wl in 700f..750f -> 0.35f + 0.65f * (750f - wl) / (750f - 700f)
        else -> 1.0f
    }

    val gamma = 0.80f
    val red = if (r > 0f) (r * factor).pow(gamma) else 0f
    val green = if (g > 0f) (g * factor).pow(gamma) else 0f
    val blue = if (b > 0f) (b * factor).pow(gamma) else 0f

    return Color(
        red = red.coerceIn(0f, 1f),
        green = green.coerceIn(0f, 1f),
        blue = blue.coerceIn(0f, 1f),
        alpha = alpha.coerceIn(0f, 1f)
    )
}
