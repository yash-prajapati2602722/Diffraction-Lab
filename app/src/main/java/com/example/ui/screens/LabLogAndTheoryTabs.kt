package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DiffractionPeak
import com.example.model.DiffractionPhysics
import com.example.model.FlowchartActionStep
import com.example.model.ObservationRecord
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
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun LabLogTabContent(
    uiState: DiffractionLabUiState,
    onSnapToPeak: (DiffractionPeak) -> Unit,
    onRecordCurrent: () -> Unit,
    onDeleteRecord: (Int) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 0. Interactive 5-Step Screen Fringe Separation (x_n & s_n / 2L) Laser Wavelength Calculator
        ScreenFringeWavelengthCalculatorCard(uiState = uiState)

        // 1. Live Grating Equation & Amrita VLab Standardization Solver Card
        LiveGratingCalculatorCard(
            uiState = uiState,
            onRecordCurrent = onRecordCurrent
        )

        // 2. Observable Principal Maxima Orders Explorer
        PrincipalPeaksExplorerCard(
            uiState = uiState,
            onSnapToPeak = onSnapToPeak
        )

        // 3. Official Medicaps Manual 5.5 Observation Table (Page 7 of Wavelength_using_Grating.pdf)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(LabSurface)
                .border(1.dp, LabBorder, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "5.5 OBSERVATION TABLE (MS, VS, TOTAL a & b, 2θ, θ)",
                        style = MaterialTheme.typography.labelMedium,
                        color = LaserCyan
                    )
                    Text(
                        text = "N = ${uiState.linesPerInch} lines/in  •  1 MSD = 0.5°  •  VSD = ${uiState.vernierDivisions}  •  LC = ${if (uiState.vernierDivisions == 60) "30″" else "1′"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
                if (uiState.observations.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onClearAll,
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("btn_clear_observations"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear observation table",
                            tint = SpectralCrimson,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear", style = MaterialTheme.typography.labelMedium, color = SpectralCrimson)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.observations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LabVoidBg)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = LaserCyan,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No observations logged yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Align the telescope crosshair onto any diffracted order and tap 'Record Reading' to populate your V₁/V₂ lab table.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    uiState.observations.forEach { record ->
                        ObservationRowCard(
                            record = record,
                            onDelete = { onDeleteRecord(record.id) }
                        )
                    }
                }
            }
        }

        // 4. Official Section 5.6 & 5.7 Results & Percentage Error Summary Card (Page 8 of PDF)
        Section57ResultsSummaryCard(uiState = uiState)
    }
}

/**
 * Interactive 5-Step Screen Distance (L) & Fringe Separation (x or s_n) Calculator
 * implementing:
 *   1) d = 1 / (lines per unit length)
 *   2) tan(θ) = x / L  or  θ = arctan(s_n / 2L)
 *   3) λ = (d · sin θ) / n
 */
@Composable
private fun ScreenFringeWavelengthCalculatorCard(
    uiState: DiffractionLabUiState
) {
    var calcLinesPerMm by remember { mutableFloatStateOf(300f) } // e.g., 80, 100, 300, 600 L/mm
    var calcScreenLCm by remember { mutableFloatStateOf(100f) }  // L = 100 cm (1.0 m)
    var useSymmetricalSn by remember { mutableStateOf(true) }    // s_n (both sides) vs x_n (single side)
    var measuredDistCm by remember { mutableFloatStateOf(39.2f) } // s_n = 39.2 cm => x = 19.6 cm
    var calcOrderN by remember { mutableIntStateOf(1) }

    val dUm = 1000f / calcLinesPerMm.coerceAtLeast(10f)
    val dMm = 1f / calcLinesPerMm.coerceAtLeast(10f)
    val singleSideXCm = if (useSymmetricalSn) measuredDistCm / 2f else measuredDistCm
    val thetaRad = atan2(singleSideXCm.toDouble(), calcScreenLCm.toDouble())
    val thetaDeg = Math.toDegrees(thetaRad).toFloat()
    val sinTheta = sin(thetaRad).toFloat()
    val solvedLambdaNm = ((dUm * 1000f * sinTheta) / calcOrderN).coerceIn(100f, 1500f)
    val solvedLambdaAngstrom = (solvedLambdaNm * 10f).roundToInt()
    val swatchCol = wavelengthToColor(solvedLambdaNm)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LabSurface)
            .border(1.dp, LabBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "5-STEP LASER SCREEN FRINGE CALCULATOR (θ = arctan(s_n / 2L))",
            style = MaterialTheme.typography.labelMedium,
            color = SpectralAmber
        )
        Text(
            text = "Calculate wavelength λ from grating lines/mm, screen distance L, and measured fringe separation x or s_n:",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        // Quick Preset Scenarios (Physics Online 80 L/mm, IIT-B 300 L/mm, Standard 600 L/mm)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("80 L/mm Laser (Physics Online)", 80f, 10.2f),
                Triple("300 L/mm He-Ne (IIT-B)", 300f, 39.2f),
                Triple("600 L/mm Green (532 nm)", 600f, 67.6f),
                Triple("500 L/mm Violet (405 nm)", 500f, 41.4f)
            ).forEach { (label, lpmm, defaultSn) ->
                FilterChip(
                    selected = abs(calcLinesPerMm - lpmm) < 5f,
                    onClick = {
                        calcLinesPerMm = lpmm
                        calcScreenLCm = 100f
                        useSymmetricalSn = true
                        measuredDistCm = defaultSn
                        calcOrderN = 1
                    },
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

        // Measurement mode toggle (Symmetrical s_n vs Single-side x) + Order n
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = useSymmetricalSn,
                    onClick = { useSymmetricalSn = true },
                    label = { Text("Symmetrical s_n (2x)", style = MaterialTheme.typography.labelSmall) }
                )
                FilterChip(
                    selected = !useSymmetricalSn,
                    onClick = { useSymmetricalSn = false },
                    label = { Text("Single-side x", style = MaterialTheme.typography.labelSmall) }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(1, 2, 3).forEach { nOrd ->
                    FilterChip(
                        selected = calcOrderN == nOrd,
                        onClick = { calcOrderN = nOrd },
                        label = { Text("n=$nOrd", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }

        // Sliders for N (lines/mm), L (cm), and x / s_n (cm)
        Text(
            text = "1. Grating Density: ${calcLinesPerMm.roundToInt()} lines/mm (${(calcLinesPerMm * 25.4f).roundToInt()} LPI)  ⟹  d = ${"%.3f".format(dUm)} µm",
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary
        )
        Slider(
            value = calcLinesPerMm,
            onValueChange = { calcLinesPerMm = it },
            valueRange = 50f..1000f,
            colors = SliderDefaults.colors(thumbColor = LaserCyan, activeTrackColor = LaserCyan)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "2. Screen L: ${"%.0f".format(calcScreenLCm)} cm",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary
                )
                Slider(
                    value = calcScreenLCm,
                    onValueChange = { calcScreenLCm = it },
                    valueRange = 20f..200f,
                    colors = SliderDefaults.colors(thumbColor = SpectralEmerald, activeTrackColor = SpectralEmerald)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (useSymmetricalSn) "3. Span s_n: ${"%.1f".format(measuredDistCm)} cm" else "3. Spot x: ${"%.1f".format(measuredDistCm)} cm",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary
                )
                Slider(
                    value = measuredDistCm,
                    onValueChange = { measuredDistCm = it },
                    valueRange = 2f..140f,
                    colors = SliderDefaults.colors(thumbColor = SpectralAmber, activeTrackColor = SpectralAmber)
                )
            }
        }

        // Step-by-Step Solution Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(LabVoidBg)
                .border(1.dp, Color(0xFF24355C), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Step 1 (Grating Spacing d): d = 1 / (${calcLinesPerMm.roundToInt()} lines/mm) = ${"%.5f".format(dMm)} mm = ${"%.2f".format(dUm * 1000f)} nm",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = if (useSymmetricalSn) {
                        "Step 2 (Half-Distance x): x = s_n / 2 = ${"%.2f".format(measuredDistCm)} / 2 = ${"%.2f".format(singleSideXCm)} cm"
                    } else {
                        "Step 2 (Measured Spot Distance x): x = ${"%.2f".format(singleSideXCm)} cm"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = "Step 3 (Angle θ): θ = arctan(x / L) = arctan(${"%.2f".format(singleSideXCm)} / ${"%.0f".format(calcScreenLCm)}) = ${"%.2f".format(thetaDeg)}°  (sin θ = ${"%.4f".format(sinTheta)})",
                    style = MaterialTheme.typography.labelSmall,
                    color = LaserCyan
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(swatchCol)
                            .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                    )
                    Text(
                        text = "Step 4 (Solve λ = d sin θ / n): λ = ${"%.1f".format(solvedLambdaNm)} nm  ($solvedLambdaAngstrom Å)",
                        style = MaterialTheme.typography.titleMedium,
                        color = swatchCol
                    )
                }
            }
        }
    }
}

@Composable
private fun Section57ResultsSummaryCard(
    uiState: DiffractionLabUiState
) {
    val violetRec = uiState.observations.firstOrNull { it.trueAngstrom in 4000..4200 }
    val redRec = uiState.observations.firstOrNull { it.trueAngstrom in 6200..6900 }
    val greenRec = uiState.observations.firstOrNull { it.trueAngstrom in 5300..5600 }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LabSurface)
            .border(1.dp, LabBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "5.6 & 5.7 CALCULATION, RESULTS & PERCENTAGE ERROR (PDF PAGE 8)",
            style = MaterialTheme.typography.labelMedium,
            color = SpectralEmerald
        )
        Text(
            text = "Grating Element e = 2.54 / N = 2.54 / ${uiState.linesPerInch} = ${"%.4e".format(uiState.gratingElementCm)} cm",
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
        Text(
            text = "Standard Wavelength Values: Violet = 4046 Å  |  Green = 5461 Å  |  Red = 6800 Å",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
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
                    text = "• Wavelength of Violet line (θ_V = ${violetRec?.let { "%.2f".format(it.meanThetaDeg) } ?: "13.82"}°) = ${violetRec?.calculatedAngstrom ?: 4046} Å  (Error: ${violetRec?.let { "%.2f".format(it.percentError) } ?: "0.00"}%)",
                    style = MaterialTheme.typography.labelMedium,
                    color = SpectralViolet
                )
                Text(
                    text = "• Wavelength of Green line  (θ_G = ${greenRec?.let { "%.2f".format(it.meanThetaDeg) } ?: "18.82"}°) = ${greenRec?.calculatedAngstrom ?: 5461} Å  (Error: ${greenRec?.let { "%.2f".format(it.percentError) } ?: "0.00"}%)",
                    style = MaterialTheme.typography.labelMedium,
                    color = SpectralEmerald
                )
                Text(
                    text = "• Wavelength of Red line    (θ_R = ${redRec?.let { "%.2f".format(it.meanThetaDeg) } ?: "23.68"}°) = ${redRec?.calculatedAngstrom ?: 6800} Å  (Error: ${redRec?.let { "%.2f".format(it.percentError) } ?: "0.00"}%)",
                    style = MaterialTheme.typography.labelMedium,
                    color = SpectralCrimson
                )
            }
        }
    }
}

@Composable
private fun LiveGratingCalculatorCard(
    uiState: DiffractionLabUiState,
    onRecordCurrent: () -> Unit
) {
    val meanDeg = abs(uiState.telescopeAngleDeg).coerceAtLeast(0.01f)
    val sinTheta = sin(Math.toRadians(meanDeg.toDouble())).toFloat()
    val nearestOrder = uiState.nearestPeak?.order?.let { if (it == 0) 1 else abs(it) } ?: 1
    val expLambdaNm = (uiState.slitSpacingUm * 1000f * sinTheta) / nearestOrder
    val expLambdaAngstrom = (expLambdaNm * 10f).roundToInt()

    val standardizedLinesPerMm = (sinTheta / (nearestOrder * 546.1e-6f)).roundToInt()
    val standardizedLinesPerInch = (standardizedLinesPerMm * 25.4f).roundToInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LabSurface)
            .border(1.dp, LabBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(
            text = "5.6 GRATING EQUATION SOLVER (e · sin θ = n · λ)",
            style = MaterialTheme.typography.labelMedium,
            color = LaserCyan
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(LabVoidBg)
                .border(1.dp, Color(0xFF22335A), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Aim 1 (Standardize Grating N from Hg Green 5461 Å): N = sin θ / (n · λ_std)",
                    style = MaterialTheme.typography.labelSmall,
                    color = SpectralEmerald
                )
                Text(
                    text = "If θ = ${"%.2f".format(meanDeg)}° is Hg Green (n=$nearestOrder) ⟹ N_exp = $standardizedLinesPerMm lines/mm ($standardizedLinesPerInch LPI)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Aim 2 (Unknown Wavelength λ from Grating e = 2.54 / N cm):",
                    style = MaterialTheme.typography.labelSmall,
                    color = SpectralAmber
                )
                Text(
                    text = "λ_exp = (e · sin θ) / n = $expLambdaAngstrom Å  (${"%.1f".format(expLambdaNm)} nm)",
                    style = MaterialTheme.typography.titleMedium,
                    color = SpectralAmber
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onRecordCurrent,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_log_current_trial"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LaserCyan,
                contentColor = Color(0xFF041326)
            )
        ) {
            Icon(
                imageVector = Icons.Default.PlaylistAddCheck,
                contentDescription = "Log current reading",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Log V₁ & V₂ Reading (θ = ${"%.2f".format(uiState.telescopeAngleDeg)}° • $expLambdaAngstrom Å)",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun PrincipalPeaksExplorerCard(
    uiState: DiffractionLabUiState,
    onSnapToPeak: (DiffractionPeak) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LabSurface)
            .border(1.dp, LabBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(
            text = "OBSERVABLE PRINCIPAL MAXIMA ORDERS (TAP TO AIM TELESCOPE)",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.principalPeaks.forEach { peak ->
                val col = wavelengthToColor(peak.spectralLine.wavelengthNm)
                val orderStr = if (peak.order > 0) "+${peak.order}" else "${peak.order}"
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSnapToPeak(peak) }
                        .testTag("peak_card_${peak.order}_${peak.spectralLine.wavelengthNm.toInt()}"),
                    color = LabSurfaceElevated,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, col.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(col)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Order n = $orderStr",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "θ = ${"%.2f".format(peak.angleDeg)}°  |  x = ${"%.1f".format(peak.screenYCm)} cm",
                            style = MaterialTheme.typography.labelSmall,
                            color = LaserCyan
                        )
                        Text(
                            text = "${peak.spectralLine.name} (${peak.spectralLine.wavelengthAngstrom} Å)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ObservationRowCard(
    record: ObservationRecord,
    onDelete: () -> Unit
) {
    val lineCol = wavelengthToColor(record.trueWavelengthNm)
    val v1L = record.v1LeftVernier
    val v1R = record.v1RightVernier
    val v2L = record.v2LeftVernier
    val v2R = record.v2RightVernier

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LabVoidBg)
            .border(1.dp, LabBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(lineCol)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "n = ${record.order} • ${record.sourceLabel}",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete observation",
                        tint = TextMuted
                    )
                }
            }

            Text(
                text = "V₁ Left(a): MS=${"%.1f".format(v1L.mainScaleDeg)}° VS=${v1L.vernierCoincidence}div (${"%.2f".format(record.v1LeftDeg)}°) | Right(b): MS=${"%.1f".format(v1R.mainScaleDeg)}° VS=${v1R.vernierCoincidence}div (+${"%.2f".format(record.v1RightDeg)}°) | 2θ₁=${"%.2f".format(record.v1TwoThetaDeg)}°",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = "V₂ Left(a): MS=${"%.1f".format(v2L.mainScaleDeg)}° VS=${v2L.vernierCoincidence}div (${"%.2f".format(record.v2LeftDeg)}°) | Right(b): MS=${"%.1f".format(v2R.mainScaleDeg)}° VS=${v2R.vernierCoincidence}div (${"%.2f".format(record.v2RightDeg)}°) | 2θ₂=${"%.2f".format(record.v2TwoThetaDeg)}°",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Avg (2θ) = ${"%.2f".format(record.twoThetaDeg)}°  ⟹  Angle θ = ${"%.2f".format(record.meanThetaDeg)}°",
                    style = MaterialTheme.typography.labelSmall,
                    color = SpectralAmber
                )
                Text(
                    text = "λ = ${record.calculatedAngstrom} Å (Std ${record.trueAngstrom} Å • Err ${"%.2f".format(record.percentError)}%)",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (record.percentError < 2.0f) SpectralEmerald else SpectralAmber
                )
            }
        }
    }
}

@Composable
fun TheoryAndVivaTabContent(
    uiState: DiffractionLabUiState,
    onToggleProcedureStep: (Int) -> Unit,
    onSelectQuizOption: (Int, Int) -> Unit,
    onResetQuiz: () -> Unit,
    onSelectPdfPage: (Int) -> Unit = {},
    onExecuteFlowchartStep: (FlowchartActionStep) -> Unit = {},
    onOpenPhotonicCadTab: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Embedded PDF Viewer
        EmbeddedPdfManualViewerCard(
            currentPageIndex = uiState.manualPdfPageIndex,
            onSelectPage = onSelectPdfPage
        )

        // 2. Interactive Exp. 5 Flowchart
        InteractiveExp5FlowchartCard(
            onExecuteStep = onExecuteFlowchartStep
        )

        // 3. Visible Color Wavelength Reference & Grating Engineering Guide (Thorlabs / Edmund Optics / UCSB)
        VisibleSpectrumAndGratingGuideCard()

        // 4. Optical Diffraction Simulation Software Directory (Ansys Lumerical LSWM, GD-Calc, S4, OghmaNano TMM, COMSOL)
        OpticalSimulationSoftwareDirectoryCard(
            onOpenPhotonicCadTab = onOpenPhotonicCadTab
        )

        // 4. Hero Banner Illustration
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, LabBorder, RoundedCornerShape(16.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_optics_banner_1792846191015),
                contentDescription = stringResource(id = R.string.desc_hero_banner),
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        }

        // 5. Core Theory, Precautions (5.8) & Applications (5.9) Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(LabSurface)
                .border(1.dp, LabBorder, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "5.8 PRECAUTIONARY MEASURES & 5.9 APPLICATIONS (PDF PAGES 8 & 10)",
                style = MaterialTheme.typography.labelMedium,
                color = LaserCyan
            )
            DiffractionPhysics.spectrometerPrecautions.forEach { prec ->
                Text(text = prec, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "5.9 APPLICATIONS OF SPECTROMETER EXPERIMENT:",
                style = MaterialTheme.typography.labelMedium,
                color = SpectralEmerald
            )
            DiffractionPhysics.spectrometerApplications.forEach { app ->
                Text(text = app, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }

        // 6. Interactive Lab Procedure Checklist
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(LabSurface)
                .border(1.dp, LabBorder, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "5.3 & 5.4 SPECTROMETER ADJUSTMENT & PROCEDURE CHECKLIST",
                style = MaterialTheme.typography.labelMedium,
                color = SpectralEmerald
            )
            DiffractionPhysics.procedureSteps.forEach { step ->
                val done = step.stepNumber in uiState.completedProcedureSteps
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LabVoidBg)
                        .border(
                            1.dp,
                            if (done) SpectralEmerald.copy(alpha = 0.5f) else LabBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onToggleProcedureStep(step.stepNumber) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Toggle step ${step.stepNumber}",
                        tint = if (done) SpectralEmerald else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Step ${step.stepNumber}: ${step.title}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = step.detail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = step.formulaHint,
                            style = MaterialTheme.typography.labelSmall,
                            color = LaserCyan
                        )
                    }
                }
            }
        }

        // 7. Interactive Viva Voce Quiz
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(LabSurface)
                .border(1.dp, LabBorder, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val totalAnswered = uiState.selectedQuizAnswers.size
            val correctCount = DiffractionPhysics.vivaQuestions.count { q ->
                uiState.selectedQuizAnswers[q.id] == q.correctIndex
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "5.10 SAMPLE VIVA-VOCE QUESTIONS (PDF PAGE 10)",
                        style = MaterialTheme.typography.labelMedium,
                        color = SpectralViolet
                    )
                    Text(
                        text = "Score: $correctCount / ${DiffractionPhysics.vivaQuestions.size} ($totalAnswered answered)",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }
                if (totalAnswered > 0) {
                    OutlinedButton(
                        onClick = onResetQuiz,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("btn_reset_viva_quiz"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset viva quiz",
                            tint = LaserCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Quiz", style = MaterialTheme.typography.labelMedium, color = LaserCyan)
                    }
                }
            }

            DiffractionPhysics.vivaQuestions.forEach { q ->
                val selectedIdx = uiState.selectedQuizAnswers[q.id]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LabVoidBg)
                        .border(1.dp, LabBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "[${q.category}] ${q.question}",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    q.options.forEachIndexed { idx, optText ->
                        val isChosen = selectedIdx == idx
                        val isCorrect = idx == q.correctIndex
                        val borderColor = when {
                            selectedIdx == null -> LabBorder
                            isCorrect -> SpectralEmerald
                            isChosen -> SpectralCrimson
                            else -> LabBorder
                        }
                        val bgColor = when {
                            selectedIdx == null -> LabSurfaceElevated
                            isCorrect -> SpectralEmerald.copy(alpha = 0.16f)
                            isChosen -> SpectralCrimson.copy(alpha = 0.16f)
                            else -> LabSurfaceElevated
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(bgColor)
                                .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                                .clickable { onSelectQuizOption(q.id, idx) }
                                .testTag("viva_q${q.id}_opt$idx")
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = optText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                    if (selectedIdx != null) {
                        Text(
                            text = if (selectedIdx == q.correctIndex) "✓ Correct! ${q.explanation}" else "✗ Explanation: ${q.explanation}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selectedIdx == q.correctIndex) SpectralEmerald else SpectralAmber
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VisibleSpectrumAndGratingGuideCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LabSurface)
            .border(1.dp, LabBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "VISIBLE SPECTRUM RANGES & GRATING SELECTION GUIDE",
            style = MaterialTheme.typography.labelMedium,
            color = LaserCyan
        )

        // Color Wavelength Bands Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DiffractionPhysics.visibleColorSpectrumBands.forEach { (colorName, rangeStr, sampleWl) ->
                val c = wavelengthToColor(sampleWl)
                Surface(
                    color = LabVoidBg,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.border(1.dp, c.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(c)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(colorName, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(rangeStr, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
            }
        }

        // FAQ / Engineering Reference Cards (Edmund Optics, Thorlabs, UCSB, Laser Screen Method)
        DiffractionPhysics.gratingFaqItems.forEach { (question, formula, answer) ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(LabVoidBg)
                    .border(1.dp, Color(0xFF1F2D4D), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(text = question, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(text = formula, style = MaterialTheme.typography.labelMedium, color = SpectralAmber)
                Text(text = answer, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun InteractiveExp5FlowchartCard(
    onExecuteStep: (FlowchartActionStep) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LabSurface)
            .border(1.dp, LabBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "EXP. 5 INTERACTIVE FLOW CHART (PDF PAGE 9 — TAP TO SIMULATE IN 3D)",
            style = MaterialTheme.typography.labelMedium,
            color = SpectralAmber
        )
        DiffractionPhysics.exp5FlowchartSteps.forEach { step ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (step.isDecisionNode) SpectralAmber.copy(alpha = 0.12f) else LabVoidBg)
                    .border(
                        1.dp,
                        if (step.isDecisionNode) SpectralAmber.copy(alpha = 0.6f) else LabBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onExecuteStep(step) }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (step.isDecisionNode) SpectralAmber else TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = step.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = LaserCyan.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Run step in 3D",
                            tint = LaserCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "3D",
                            style = MaterialTheme.typography.labelSmall,
                            color = LaserCyan
                        )
                    }
                }
            }
        }
    }
}
