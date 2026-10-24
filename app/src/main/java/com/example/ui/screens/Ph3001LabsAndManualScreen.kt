package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.PH3001Module
import com.example.ui.theme.LabBorder
import com.example.ui.theme.LabSurface
import com.example.ui.theme.LabSurfaceElevated
import com.example.ui.theme.LabVoidBg
import com.example.ui.theme.LaserCyan
import com.example.ui.theme.SpectralAmber
import com.example.ui.theme.SpectralCrimson
import com.example.ui.theme.SpectralEmerald
import com.example.ui.theme.SpectralViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.wavelengthToColor
import com.example.viewmodel.DiffractionLabUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun Ph3001CompanionLabsTabContent(
    uiState: DiffractionLabUiState,
    onSelectModule: (PH3001Module) -> Unit,
    onNewtonRadiusChange: (Float) -> Unit,
    onNewtonMediumChange: (Float) -> Unit,
    onNewtonMicroscopeChange: (Float) -> Unit,
    onFiberIndicesChange: (Float, Float) -> Unit,
    onFiberOffsetChange: (Float) -> Unit,
    onFiberDistanceChange: (Float) -> Unit,
    onPlanckLedSelect: (Int) -> Unit,
    onPlanckVoltageChange: (Float) -> Unit,
    onScrewGaugeChange: (Int, Int, Int) -> Unit,
    onJumpToManualPage: (Int) -> Unit,
    onPushGratingToBench: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Module Selector Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(LabSurface)
                .border(1.dp, LabBorder, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Text(
                text = "PHOTONIC GRATING CAD (GD-CALC / LUMERICAL / S4 / TMM) & PH3001 LABS",
                style = MaterialTheme.typography.labelMedium,
                color = LaserCyan
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PH3001Module.entries.forEach { mod ->
                    val selected = uiState.selectedPh3001Module == mod
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectModule(mod) },
                        label = {
                            Text("${mod.expCode}: ${mod.title}", style = MaterialTheme.typography.labelMedium)
                        },
                        modifier = Modifier.testTag("chip_ph3001_${mod.name.lowercase()}"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LaserCyan.copy(alpha = 0.25f),
                            selectedLabelColor = LaserCyan,
                            containerColor = LabSurfaceElevated,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
        }

        // Render active Photonic CAD or PH3001 module simulator
        when (uiState.selectedPh3001Module) {
            PH3001Module.GD_CALC_LUMERICAL -> GdCalcLumericalSubWavelengthCard(
                onPushToOpticalBench = onPushGratingToBench
            )
            PH3001Module.DFB_BRAGG_TMM -> OghmaNanoDfbBraggTmmCard()
            PH3001Module.NEWTONS_RINGS -> NewtonsRingsSimulatorCard(
                uiState = uiState,
                onRadiusChange = onNewtonRadiusChange,
                onMediumChange = onNewtonMediumChange,
                onMicroscopeChange = onNewtonMicroscopeChange,
                onOpenManual = { onJumpToManualPage(44) }
            )
            PH3001Module.OPTICAL_FIBER_NA -> OpticalFiberNaSimulatorCard(
                uiState = uiState,
                onIndicesChange = onFiberIndicesChange,
                onOffsetChange = onFiberOffsetChange,
                onDistanceChange = onFiberDistanceChange,
                onOpenManual = { onJumpToManualPage(37) }
            )
            PH3001Module.PLANCK_LED -> PlanckConstantLedSimulatorCard(
                uiState = uiState,
                onSelectLed = onPlanckLedSelect,
                onVoltageChange = onPlanckVoltageChange,
                onOpenManual = { onJumpToManualPage(74) }
            )
            PH3001Module.INSTRUMENTS_LC -> ScrewGaugeAndVernierTrainerCard(
                uiState = uiState,
                onScrewGaugeChange = onScrewGaugeChange,
                onOpenManual = { onJumpToManualPage(5) }
            )
        }
    }
}

@Composable
private fun NewtonsRingsSimulatorCard(
    uiState: DiffractionLabUiState,
    onRadiusChange: (Float) -> Unit,
    onMediumChange: (Float) -> Unit,
    onMicroscopeChange: (Float) -> Unit,
    onOpenManual: () -> Unit
) {
    // Sodium λ = 5893 Å = 5.893e-4 mm
    val lambdaMm = 5.893e-4f
    val rMm = uiState.newtonLensRadiusMm
    val mu = uiState.newtonMediumRefractiveIndex

    // Diameter of nth dark ring: D_n = 2 * sqrt(n * λ * R / μ)
    fun ringDiameterMm(n: Int): Float = 2f * sqrt((n * lambdaMm * rMm) / mu)

    val d12 = ringDiameterMm(12)
    val d10 = ringDiameterMm(10)
    val d8 = ringDiameterMm(8)
    val d6 = ringDiameterMm(6)
    val diffSq12_10 = d12 * d12 - d10 * d10
    val calcLambdaAngstrom = ((diffSq12_10 * mu) / (4f * 2f * rMm)) * 1e7f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LabSurface)
            .border(1.dp, LabBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "EXP 4: NEWTON'S RINGS TRAVELING MICROSCOPE",
                    style = MaterialTheme.typography.labelMedium,
                    color = SpectralAmber
                )
                Text(
                    text = "Sodium Light λ = 5893 Å  •  D²_{n+p} - D²_n = 4pRλ / μ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            OutlinedButton(onClick = onOpenManual, shape = RoundedCornerShape(10.dp)) {
                Text("Manual p.45", style = MaterialTheme.typography.labelSmall, color = LaserCyan)
            }
        }

        // Interactive Traveling Microscope Eyepiece Canvas showing concentric Newton's Rings
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF05070E))
                .border(1.dp, Color(0xFF283963), RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val deltaMm = (dragAmount.x / size.width.coerceAtLeast(1)) * 9f
                        onMicroscopeChange(uiState.newtonMicroscopePosMm + deltaMm)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxVisualRadiusPx = size.minDimension * 0.44f
                val mmToPx = maxVisualRadiusPx / 2.2f // ±2.2 mm field

                // Central dark spot + 14 concentric bright yellow-amber interference rings
                for (n in 14 downTo 1) {
                    val darkRadiusMm = ringDiameterMm(n) / 2f
                    val brightRadiusMm = sqrt(((n - 0.5f) * lambdaMm * rMm) / mu)
                    val brightPx = brightRadiusMm * mmToPx
                    if (brightPx < maxVisualRadiusPx) {
                        drawCircle(
                            color = Color(0xFFFBBF24).copy(alpha = (0.85f - n * 0.035f).coerceIn(0.25f, 0.85f)),
                            radius = brightPx,
                            center = center,
                            style = Stroke(width = (6.5f - n * 0.25f).coerceAtLeast(2.0f))
                        )
                    }
                    val darkPx = darkRadiusMm * mmToPx
                    if (darkPx < maxVisualRadiusPx) {
                        drawCircle(
                            color = Color(0xFF090804),
                            radius = darkPx,
                            center = center,
                            style = Stroke(width = 2.2f)
                        )
                    }
                }
                // Central dark contact point O
                drawCircle(color = Color(0xFF050505), radius = 8f, center = center)

                // Traveling Microscope Crosswire at newtonMicroscopePosMm
                val crossX = (center.x + (uiState.newtonMicroscopePosMm / 2f) * mmToPx)
                    .coerceIn(10f, size.width - 10f)
                drawLine(
                    color = LaserCyan,
                    start = Offset(crossX, 0f),
                    end = Offset(crossX, size.height),
                    strokeWidth = 2f
                )
                drawLine(
                    color = LaserCyan.copy(alpha = 0.5f),
                    start = Offset(0f, center.y),
                    end = Offset(size.width, center.y),
                    strokeWidth = 1.2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            }
        }

        Text(
            text = "Microscope Reading x = ${"%.3f".format(uiState.newtonMicroscopePosMm)} mm  (LC = 0.01 mm)",
            style = MaterialTheme.typography.labelLarge,
            color = LaserCyan
        )
        Slider(
            value = uiState.newtonMicroscopePosMm,
            onValueChange = onMicroscopeChange,
            valueRange = -4.2f..4.2f,
            colors = SliderDefaults.colors(thumbColor = LaserCyan, activeTrackColor = LaserCyan)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Plano-Convex Lens Radius R = ${rMm.roundToInt()} mm",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Text(
                text = "Film Index μ = ${"%.2f".format(mu)}",
                style = MaterialTheme.typography.labelMedium,
                color = SpectralEmerald
            )
        }
        Slider(
            value = rMm,
            onValueChange = onRadiusChange,
            valueRange = 300f..800f,
            colors = SliderDefaults.colors(thumbColor = SpectralAmber, activeTrackColor = SpectralAmber)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                1.00f to "Air Film (μ=1.00)",
                1.33f to "Water (μ=1.33)",
                1.50f to "Oil (μ=1.50)"
            ).forEach { (idxVal, label) ->
                FilterChip(
                    selected = abs(mu - idxVal) < 0.02f,
                    onClick = { onMediumChange(idxVal) },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        // Manual Page 50–51 Ring Diameter Table (12th, 10th, 8th, 6th rings, p = 2)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(LabVoidBg)
                .border(1.dp, LabBorder, RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "MANUAL 4.5 RING DIAMETER TABLE (p = 2):",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = "n=12: D₁₂ = ${"%.3f".format(d12)} mm  |  D₁₂² = ${"%.3f".format(d12 * d12)} mm²",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary
                )
                Text(
                    text = "n=10: D₁₀ = ${"%.3f".format(d10)} mm  |  D₁₂² - D₁₀² = ${"%.3f".format(diffSq12_10)} mm²",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary
                )
                Text(
                    text = "n=8:  D₈  = ${"%.3f".format(d8)} mm   |  n=6: D₆ = ${"%.3f".format(d6)} mm",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary
                )
                Text(
                    text = "Calculated λ = (D₁₂² - D₁₀²) / (4·p·R) = ${calcLambdaAngstrom.roundToInt()} Å (Std = 5893 Å)",
                    style = MaterialTheme.typography.labelMedium,
                    color = SpectralEmerald
                )
            }
        }
    }
}

@Composable
private fun OpticalFiberNaSimulatorCard(
    uiState: DiffractionLabUiState,
    onIndicesChange: (Float, Float) -> Unit,
    onOffsetChange: (Float) -> Unit,
    onDistanceChange: (Float) -> Unit,
    onOpenManual: () -> Unit
) {
    val n1 = uiState.fiberCoreIndex
    val n2 = uiState.fiberCladdingIndex
    val naTheoretical = sqrt((n1 * n1 - n2 * n2).coerceAtLeast(0.001f))
    val acceptanceAngleDeg = Math.toDegrees(asin(naTheoretical.coerceAtMost(0.999f).toDouble())).toFloat()
    val rMm = uiState.fiberDetectorDistanceMm
    val xMm = uiState.fiberDetectorOffsetMm
    val currentThetaDeg = Math.toDegrees(atan2(xMm.toDouble(), rMm.toDouble())).toFloat()

    // Gaussian output power profile reaching 5% of P_max at θ = ±θ_a (Manual Page 41 Step 12–16)
    val pMaxUw = 120.0f
    val sigmaDeg = (acceptanceAngleDeg / sqrt(2.0 * kotlin.math.ln(20.0))).toFloat().coerceAtLeast(1f)
    val currentPowerUw = pMaxUw * exp(-0.5f * (currentThetaDeg / sigmaDeg) * (currentThetaDeg / sigmaDeg))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LabSurface)
            .border(1.dp, LabBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "EXP 3: OPTICAL FIBER NUMERICAL APERTURE (SCANNING)",
                    style = MaterialTheme.typography.labelMedium,
                    color = LaserCyan
                )
                Text(
                    text = "N.A. = sin θ_a = √(n₁² - n₂²)  •  5% Max Power Cutoff",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            OutlinedButton(onClick = onOpenManual, shape = RoundedCornerShape(10.dp)) {
                Text("Manual p.38", style = MaterialTheme.typography.labelSmall, color = LaserCyan)
            }
        }

        // Power P(μW) vs Angle θ Graph with 5% cutoff line (Manual Page 41–42)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LabVoidBg)
                .border(1.dp, Color(0xFF283963), RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val baseY = h - 22f
                val usableH = h - 34f
                val maxPlotDeg = 35f

                // 5% Max Power horizontal reference line (Manual Step 13)
                val y5Pct = baseY - 0.05f * usableH
                drawLine(
                    color = SpectralEmerald,
                    start = Offset(0f, y5Pct),
                    end = Offset(w, y5Pct),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f), 0f)
                )

                // Drop perpendiculars at -θ_a and +θ_a (Manual Step 14)
                for (sign in listOf(-1f, 1f)) {
                    val xCut = (w / 2f) + ((sign * acceptanceAngleDeg) / maxPlotDeg) * (w / 2f)
                    drawLine(
                        color = SpectralEmerald.copy(alpha = 0.75f),
                        start = Offset(xCut, y5Pct),
                        end = Offset(xCut, baseY),
                        strokeWidth = 1.5f
                    )
                }

                // Bell curve P(θ)
                val path = Path()
                val steps = 200
                for (i in 0..steps) {
                    val frac = i.toFloat() / steps
                    val deg = (frac * 2f - 1f) * maxPlotDeg
                    val pNorm = exp(-0.5f * (deg / sigmaDeg) * (deg / sigmaDeg))
                    val x = frac * w
                    val y = baseY - pNorm * usableH
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path = path, color = SpectralCrimson, style = Stroke(width = 2.8f))

                // Current detector cursor
                val curX = ((w / 2f) + (currentThetaDeg / maxPlotDeg) * (w / 2f)).coerceIn(4f, w - 4f)
                val curY = baseY - (currentPowerUw / pMaxUw) * usableH
                drawCircle(color = SpectralAmber, radius = 6f, center = Offset(curX, curY))
                drawLine(
                    color = SpectralAmber,
                    start = Offset(curX, 6f),
                    end = Offset(curX, baseY),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                )
            }
        }

        Text(
            text = "Detector Translation x = ${"%.1f".format(xMm)} mm  |  θ = tan⁻¹(x/r) = ${"%.2f".format(currentThetaDeg)}°  |  P = ${"%.1f".format(currentPowerUw)} µW",
            style = MaterialTheme.typography.labelMedium,
            color = SpectralAmber
        )
        Slider(
            value = xMm,
            onValueChange = onOffsetChange,
            valueRange = -12f..12f,
            colors = SliderDefaults.colors(thumbColor = SpectralAmber, activeTrackColor = SpectralAmber)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Core n₁ = ${"%.2f".format(n1)}  |  Cladding n₂ = ${"%.2f".format(n2)}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Text(
                text = "Distance r = ${rMm.roundToInt()} mm",
                style = MaterialTheme.typography.labelMedium,
                color = LaserCyan
            )
        }
        Slider(
            value = n1,
            onValueChange = { onIndicesChange(it, n2) },
            valueRange = 1.44f..1.60f,
            colors = SliderDefaults.colors(thumbColor = LaserCyan, activeTrackColor = LaserCyan)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(LabVoidBg)
                .border(1.dp, LabBorder, RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Acceptance Cone 2θ_a = |θ₂ - θ₁| = ${"%.2f".format(acceptanceAngleDeg * 2f)}°  ⟹  θ_a = ${"%.2f".format(acceptanceAngleDeg)}°",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Numerical Aperture N.A. = sin(${"%.2f".format(acceptanceAngleDeg)}°) = ${"%.4f".format(naTheoretical)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = SpectralEmerald
                )
            }
        }
    }
}

@Composable
private fun PlanckConstantLedSimulatorCard(
    uiState: DiffractionLabUiState,
    onSelectLed: (Int) -> Unit,
    onVoltageChange: (Float) -> Unit,
    onOpenManual: () -> Unit
) {
    // Manual Page 80 standard LED values: Red(6600Å), Yellow(6100Å), Green(5250Å), Blue(4700Å)
    val leds = listOf(
        Triple("Red LED (6600 Å)", 660.0f, 1.86f),
        Triple("Yellow LED (6100 Å)", 610.0f, 2.02f),
        Triple("Green LED (5250 Å)", 525.0f, 2.34f),
        Triple("Blue LED (4700 Å)", 470.0f, 2.62f)
    )
    val (ledName, wlNm, vKnee) = leds[uiState.planckSelectedLedIndex]
    val ledCol = wavelengthToColor(wlNm)
    val vBias = uiState.planckBiasVoltageV
    val currentMa = if (vBias < vKnee) {
        0.05f * exp((vBias - vKnee) * 8f)
    } else {
        0.05f + (vBias - vKnee) * 28f
    }
    val freqHz = 3.0e8 / (wlNm * 1e-9)
    val calcPlanckH = (1.6021765e-19 * vKnee) / freqHz

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LabSurface)
            .border(1.dp, LabBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "EXP 6: PLANCK'S CONSTANT (h) BY MULTI-COLOR LEDs",
                    style = MaterialTheme.typography.labelMedium,
                    color = SpectralViolet
                )
                Text(
                    text = "E = h·f = e·V₀   ⟹   h = (e · V₀ · λ) / c",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            OutlinedButton(onClick = onOpenManual, shape = RoundedCornerShape(10.dp)) {
                Text("Manual p.75", style = MaterialTheme.typography.labelSmall, color = LaserCyan)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            leds.forEachIndexed { idx, (name, wl, _) ->
                FilterChip(
                    selected = uiState.planckSelectedLedIndex == idx,
                    onClick = { onSelectLed(idx) },
                    label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = wavelengthToColor(wl).copy(alpha = 0.25f),
                        selectedLabelColor = TextPrimary
                    )
                )
            }
        }

        // V-I Characteristic Curve with Tangent to Knee Voltage V0 (Manual Page 78–79)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LabVoidBg)
                .border(1.dp, Color(0xFF283963), RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val baseY = h - 20f
                val usableH = h - 30f
                val maxV = 3.2f
                val maxI = 40f

                val path = Path()
                for (i in 0..160) {
                    val v = (i / 160f) * maxV
                    val cur = if (v < vKnee) 0.05f * exp((v - vKnee) * 8f) else 0.05f + (v - vKnee) * 28f
                    val x = (v / maxV) * w
                    val y = (baseY - (cur / maxI) * usableH).coerceIn(4f, baseY)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path = path, color = ledCol, style = Stroke(width = 3f))

                // Tangent line intersecting X-axis at Turn-On Voltage V0
                val xKnee = (vKnee / maxV) * w
                val xTop = ((vKnee + maxI / 28f) / maxV) * w
                drawLine(
                    color = SpectralAmber,
                    start = Offset(xKnee, baseY),
                    end = Offset(xTop.coerceAtMost(w), 4f),
                    strokeWidth = 1.8f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f)
                )

                val curX = (vBias / maxV) * w
                val curY = (baseY - (currentMa / maxI) * usableH).coerceIn(6f, baseY)
                drawCircle(color = TextPrimary, radius = 6f, center = Offset(curX, curY))
            }
        }

        Text(
            text = "$ledName  •  Bias V = ${"%.2f".format(vBias)} V  •  Current I = ${"%.2f".format(currentMa)} mA",
            style = MaterialTheme.typography.labelLarge,
            color = ledCol
        )
        Slider(
            value = vBias,
            onValueChange = onVoltageChange,
            valueRange = 0f..3.2f,
            colors = SliderDefaults.colors(thumbColor = ledCol, activeTrackColor = ledCol)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(LabVoidBg)
                .border(1.dp, LabBorder, RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Turn-On Knee Voltage V₀ = ${"%.2f".format(vKnee)} V  |  Frequency f = c/λ = ${"%.2e".format(freqHz)} Hz",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary
                )
                Text(
                    text = "Planck's Constant h = e·V₀ / f = ${"%.3e".format(calcPlanckH)} J·s  (Std = 6.626×10⁻³⁴ J·s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = SpectralEmerald
                )
            }
        }
    }
}

@Composable
private fun ScrewGaugeAndVernierTrainerCard(
    uiState: DiffractionLabUiState,
    onScrewGaugeChange: (Int, Int, Int) -> Unit,
    onOpenManual: () -> Unit
) {
    val psr = uiState.screwGaugePitchScaleMm
    val hsc = uiState.screwGaugeHeadDiv
    val zeDiv = uiState.screwGaugeZeroErrorDiv
    val lcMm = 0.01f
    val hsrMm = hsc * lcMm
    val totalReadingMm = psr + hsrMm
    val zeroCorrectionMm = -zeDiv * lcMm
    val correctedMm = totalReadingMm + zeroCorrectionMm

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LabSurface)
            .border(1.dp, LabBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "EXP 0: SCREW GAUGE & VERNIER LEAST COUNT TRAINER",
                    style = MaterialTheme.typography.labelMedium,
                    color = SpectralEmerald
                )
                Text(
                    text = "Pitch = 1 mm  •  100 Head Divisions  •  LC = 0.01 mm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            OutlinedButton(onClick = onOpenManual, shape = RoundedCornerShape(10.dp)) {
                Text("Manual p.6", style = MaterialTheme.typography.labelSmall, color = LaserCyan)
            }
        }

        Text("Pitch Scale Reading (PSR): $psr mm", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        Slider(
            value = psr.toFloat(),
            onValueChange = { onScrewGaugeChange(it.roundToInt(), hsc, zeDiv) },
            valueRange = 0f..12f,
            colors = SliderDefaults.colors(thumbColor = LaserCyan, activeTrackColor = LaserCyan)
        )

        Text("Head Scale Coincidence (HSC): $hsc div", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        Slider(
            value = hsc.toFloat(),
            onValueChange = { onScrewGaugeChange(psr, it.roundToInt(), zeDiv) },
            valueRange = 0f..99f,
            colors = SliderDefaults.colors(thumbColor = SpectralAmber, activeTrackColor = SpectralAmber)
        )

        Text(
            text = "Zero Error (Z.E.): ${if (zeDiv > 0) "+$zeDiv" else zeDiv} div  ⟹  Zero Correction (Z.C.) = ${if (zeroCorrectionMm >= 0) "+${"%.2f".format(zeroCorrectionMm)}" else "${"%.2f".format(zeroCorrectionMm)}"} mm",
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
        Slider(
            value = zeDiv.toFloat(),
            onValueChange = { onScrewGaugeChange(psr, hsc, it.roundToInt()) },
            valueRange = -10f..10f,
            colors = SliderDefaults.colors(thumbColor = SpectralEmerald, activeTrackColor = SpectralEmerald)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(LabVoidBg)
                .border(1.dp, LabBorder, RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Total Reading T.R. = PSR + (HSC × LC) = $psr + ${"%.2f".format(hsrMm)} = ${"%.2f".format(totalReadingMm)} mm",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Corrected Reading = T.R. ± Z.C. = ${"%.2f".format(correctedMm)} mm",
                    style = MaterialTheme.typography.labelLarge,
                    color = SpectralEmerald
                )
            }
        }
    }
}

@Composable
fun EmbeddedPdfManualViewerCard(
    currentPageIndex: Int,
    onSelectPage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var useDedicatedGratingPdf by remember { mutableStateOf(true) }
    var gratingPageIndex by remember { mutableIntStateOf(5) } // Default Page 6 (Fig 5.1b Grating Spectrometer)
    var pageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var totalPages by remember { mutableIntStateOf(11) }

    val activePageIdx = if (useDedicatedGratingPdf) gratingPageIndex else currentPageIndex
    val activeAssetFileName = if (useDedicatedGratingPdf) {
        "Wavelength_using_Grating_de93cc07.pdf"
    } else {
        "Applied_Physics_Lab_Mnaual_2026_4e1a295e.pdf"
    }

    LaunchedEffect(activePageIdx, useDedicatedGratingPdf) {
        withContext(Dispatchers.IO) {
            try {
                val cacheFile = File(context.cacheDir, activeAssetFileName)
                if (!cacheFile.exists()) {
                    context.assets.open(activeAssetFileName).use { input ->
                        cacheFile.outputStream().use { output -> input.copyTo(output) }
                    }
                }
                ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        totalPages = renderer.pageCount
                        val safeIdx = activePageIdx.coerceIn(0, renderer.pageCount - 1)
                        renderer.openPage(safeIdx).use { page ->
                            val bmp = Bitmap.createBitmap(
                                (page.width * 1.6f).toInt().coerceAtLeast(600),
                                (page.height * 1.6f).toInt().coerceAtLeast(800),
                                Bitmap.Config.ARGB_8888
                            )
                            bmp.eraseColor(android.graphics.Color.WHITE)
                            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            pageBitmap = bmp
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LabSurface)
            .border(1.dp, LabBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "UPLOADED PDF LAB MANUAL VIEWER",
                    style = MaterialTheme.typography.labelMedium,
                    color = LaserCyan
                )
                Text(
                    text = "Page ${activePageIdx + 1} of $totalPages  •  ${if (useDedicatedGratingPdf) "Wavelength using Grating (11p)" else "Full Applied Physics Manual (113p)"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = {
                        if (useDedicatedGratingPdf) {
                            gratingPageIndex = (gratingPageIndex - 1).coerceAtLeast(0)
                        } else {
                            onSelectPage((currentPageIndex - 1).coerceAtLeast(0))
                        }
                    },
                    modifier = Modifier.height(42.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = "Previous page", tint = TextPrimary)
                }
                OutlinedButton(
                    onClick = {
                        if (useDedicatedGratingPdf) {
                            gratingPageIndex = (gratingPageIndex + 1).coerceAtMost(totalPages - 1)
                        } else {
                            onSelectPage((currentPageIndex + 1).coerceAtMost(totalPages - 1))
                        }
                    },
                    modifier = Modifier.height(42.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = "Next page", tint = TextPrimary)
                }
            }
        }

        // Toggle between the 2 uploaded PDFs
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = useDedicatedGratingPdf,
                onClick = { useDedicatedGratingPdf = true },
                label = { Text("Wavelength using Grating PDF (11p)", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SpectralEmerald.copy(alpha = 0.22f),
                    selectedLabelColor = SpectralEmerald
                )
            )
            FilterChip(
                selected = !useDedicatedGratingPdf,
                onClick = { useDedicatedGratingPdf = false },
                label = { Text("Full Lab Manual PDF (113p)", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LaserCyan.copy(alpha = 0.22f),
                    selectedLabelColor = LaserCyan
                )
            )
        }

        // Quick Page Jumpers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (useDedicatedGratingPdf) {
                listOf(
                    2 to "5.1 Theory (p.3)",
                    4 to "Schuster Method (p.5)",
                    5 to "Fig 5.1b 3D Setup (p.6)",
                    6 to "5.5 Table (p.7)",
                    7 to "5.6 Results (p.8)",
                    8 to "Flow Chart (p.9)",
                    9 to "5.10 Viva (p.10)",
                    10 to "VLab Links (p.11)"
                ).forEach { (pageIdx, label) ->
                    FilterChip(
                        selected = gratingPageIndex == pageIdx,
                        onClick = { gratingPageIndex = pageIdx },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LaserCyan.copy(alpha = 0.22f),
                            selectedLabelColor = LaserCyan,
                            containerColor = LabSurfaceElevated,
                            labelColor = TextSecondary
                        )
                    )
                }
            } else {
                listOf(
                    2 to "Index (p.3)",
                    12 to "Spectrometer LC (p.13)",
                    16 to "Exp 1 Wire (p.17)",
                    37 to "Exp 3 Fiber NA (p.38)",
                    44 to "Exp 4 Newton Ring (p.45)",
                    54 to "Exp 5 Grating (p.55)",
                    59 to "Grating Table (p.60)",
                    74 to "Exp 6 Planck (p.75)",
                    93 to "Exp 9 Heisenberg (p.94)"
                ).forEach { (pageIdx, label) ->
                    FilterChip(
                        selected = abs(currentPageIndex - pageIdx) <= 1,
                        onClick = { onSelectPage(pageIdx) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LaserCyan.copy(alpha = 0.22f),
                            selectedLabelColor = LaserCyan,
                            containerColor = LabSurfaceElevated,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
        }

        pageBitmap?.let { bmp ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(430.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, LabBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Manual Page ${activePageIdx + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
