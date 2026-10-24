package com.example

import com.example.model.DiffractionPhysics
import com.example.model.LightSourceMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.asin

class ExampleUnitTest {
    @Test
    fun gratingSpacingAndLpiConversion_isAccurate() {
        val spacingUm = DiffractionPhysics.lpiToSlitSpacingUm(15000)
        assertEquals(1.6933f, spacingUm, 0.001f)
        val lpi = DiffractionPhysics.slitSpacingUmToLpi(spacingUm)
        assertEquals(15000, lpi)
    }

    @Test
    fun fraunhoferPrincipalMaxima_matchesGratingEquation() {
        val lines = DiffractionPhysics.activeLinesForMode(LightSourceMode.MONOCHROMATIC, 532f)
        val peaks = DiffractionPhysics.computePrincipalPeaks(
            lines = lines,
            slitSpacingUm = 2.0f, // 2000 nm
            slitWidthUm = 0.5f,
            screenDistanceM = 1.0f
        )
        val firstOrder = peaks.first { it.order == 1 }
        val expectedRad = asin(532.0 / 2000.0).toFloat()
        assertEquals(expectedRad, firstOrder.angleRad, 0.001f)

        val peakIntensity = DiffractionPhysics.calculateIntensityAtAngle(
            angleRad = firstOrder.angleRad,
            wavelengthNm = 532f,
            slitSpacingUm = 2.0f,
            slitWidthUm = 0.5f,
            numSlits = 20,
            includeEnvelope = false
        )
        assertTrue(peakIntensity > 0.98f)
    }
}
