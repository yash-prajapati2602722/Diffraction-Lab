package com.example.ui.components

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
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.BenchExperimentMode
import com.example.model.DiffractionPhysics
import com.example.model.LightSourceMode
import com.example.ui.theme.LabBorder
import com.example.ui.theme.LabSurface
import com.example.ui.theme.LabSurfaceElevated
import com.example.ui.theme.LabVoidBg
import com.example.ui.theme.LaserCyan
import com.example.ui.theme.SpectralAmber
import com.example.ui.theme.SpectralEmerald
import com.example.ui.theme.SpectralViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.wavelengthToColor
import com.example.viewmodel.DiffractionLabUiState
import kotlin.math.roundToInt

@Composable
fun ParameterControlsDeck(
    uiState: DiffractionLabUiState,
    onSelectBenchMode: (BenchExperimentMode) -> Unit,
    onSelectSourceMode: (LightSourceMode) -> Unit,
    onWavelengthChange: (Float) -> Unit,
    onSlitSpacingChange: (Float) -> Unit,
    onGratingLpiSelect: (Int) -> Unit,
    onSlitWidthChange: (Float) -> Unit,
    onNumSlitsChange: (Int) -> Unit,
    onScreenDistanceChange: (Float) -> Unit,
    onAngleChange: (Float) -> Unit,
    onNudgeAngle: (Float) -> Unit,
    onSnapToPeak: () -> Unit,
    onLockLeftReading: () -> Unit,
    onLockRightReading: () -> Unit,
    onRecordReading: () -> Unit,
    onSelectWirePreset: (Int) -> Unit,
    onWireThicknessChange: (Float) -> Unit,
    onToggleEnvelope: () -> Unit,
    onToggleWaves: () -> Unit,
    onResetApparatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 0. Optical Bench Experiment Mode Selector (Exp 5 Grating vs Exp 9 Single-Slit Heisenberg vs Exp 1 Thin Wire)
        BenchExperimentModeCard(
            uiState = uiState,
            onSelectBenchMode = onSelectBenchMode,
            onSelectWirePreset = onSelectWirePreset,
            onWireThicknessChange = onWireThicknessChange
        )

        // 1. Telescope / Detector Angle & Vernier Fine Screw Card
        DetectorAngleControlCard(
            uiState = uiState,
            onAngleChange = onAngleChange,
            onNudgeAngle = onNudgeAngle,
            onSnapToPeak = onSnapToPeak,
            onLockLeftReading = onLockLeftReading,
            onLockRightReading = onLockRightReading,
            onRecordReading = onRecordReading
        )

        // 2. Slit Spacing (d) & Grating Density (N) Card (shown in Grating mode)
        if (uiState.benchMode == BenchExperimentMode.DIFFRACTION_GRATING) {
            SlitSpacingControlCard(
                uiState = uiState,
                onSlitSpacingChange = onSlitSpacingChange,
                onGratingLpiSelect = onGratingLpiSelect
            )
        }

        // 3. Light Source & Wavelength (λ) Card
        WavelengthAndSourceControlCard(
            uiState = uiState,
            onSelectSourceMode = onSelectSourceMode,
            onWavelengthChange = onWavelengthChange
        )

        // 4. Multi-Slit Count (M), Slit Width (a), & Screen Distance (L) Card
        SecondaryOpticsControlCard(
            uiState = uiState,
            onSlitWidthChange = onSlitWidthChange,
            onNumSlitsChange = onNumSlitsChange,
            onScreenDistanceChange = onScreenDistanceChange,
            onToggleEnvelope = onToggleEnvelope,
            onToggleWaves = onToggleWaves,
            onResetApparatus = onResetApparatus
        )
    }
}

@Composable
private fun BenchExperimentModeCard(
    uiState: DiffractionLabUiState,
    onSelectBenchMode: (BenchExperimentMode) -> Unit,
    onSelectWirePreset: (Int) -> Unit,
    onWireThicknessChange: (Float) -> Unit
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
            text = "DIFFRACTION EXPERIMENT SETUP (PH3001 MANUAL & VLAB)",
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
            BenchExperimentMode.entries.forEach { mode ->
                val selected = uiState.benchMode == mode
                FilterChip(
                    selected = selected,
                    onClick = { onSelectBenchMode(mode) },
                    label = { Text(mode.shortTitle, style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("chip_bench_mode_${mode.name.lowercase()}"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LaserCyan.copy(alpha = 0.25f),
                        selectedLabelColor = LaserCyan,
                        containerColor = LabSurfaceElevated,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "${uiState.benchMode.manualExpRef}  •  ${uiState.benchMode.formulaSummary}",
            style = MaterialTheme.typography.labelSmall,
            color = SpectralAmber
        )

        // Mode-specific live telemetry box for Exp 1 (Wire) or Exp 9 (Heisenberg Single Slit)
        if (uiState.benchMode == BenchExperimentMode.WIRE_THICKNESS_LASER) {
            Spacer(modifier = Modifier.height(10.dp))
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
                        text = "EXP 1: THICKNESS OF THIN WIRE USING He-Ne LASER (6328 Å)",
                        style = MaterialTheme.typography.labelMedium,
                        color = LaserCyan
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            0 to "Wire 1 (Std 0.12 mm)",
                            1 to "Wire 2 (Std 0.18 mm)"
                        ).forEach { (idx, label) ->
                            FilterChip(
                                selected = uiState.wirePresetIndex == idx,
                                onClick = { onSelectWirePreset(idx) },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    Text(
                        text = "Wire Thickness d = ${"%.3f".format(uiState.wireThicknessMm)} mm  |  Screen D = ${(uiState.screenDistanceM * 1000).roundToInt()} mm",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Slider(
                        value = uiState.wireThicknessMm,
                        onValueChange = onWireThicknessChange,
                        valueRange = 0.05f..0.35f,
                        colors = SliderDefaults.colors(thumbColor = SpectralAmber, activeTrackColor = SpectralAmber)
                    )
                    val halfWidthB = uiState.wireCentralMaxHalfWidthMm
                    Text(
                        text = "Central Max Width W = 2b = ${"%.2f".format(halfWidthB * 2f)} mm  ⟹  Half-width b = ${"%.2f".format(halfWidthB)} mm",
                        style = MaterialTheme.typography.labelMedium,
                        color = SpectralEmerald
                    )
                    Text(
                        text = "Formula Check: d = (D · λ) / b = (${(uiState.screenDistanceM * 1000).roundToInt()} × 6.328×10⁻⁴) / ${"%.2f".format(halfWidthB)} = ${"%.3f".format(uiState.wireThicknessMm)} mm",
                        style = MaterialTheme.typography.labelSmall,
                        color = SpectralAmber
                    )
                }
            }
        } else if (uiState.benchMode == BenchExperimentMode.SINGLE_SLIT_HEISENBERG) {
            Spacer(modifier = Modifier.height(10.dp))
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
                        text = "EXP 9: HEISENBERG UNCERTAINTY PRINCIPLE (SINGLE SLIT)",
                        style = MaterialTheme.typography.labelMedium,
                        color = SpectralViolet
                    )
                    Text(
                        text = "Position Uncertainty Δy = a = ${"%.2f".format(uiState.slitWidthUm)} µm  |  1st Minima θ₁ = ${"%.2f".format(uiState.heisenbergFirstMinAngleDeg)}°",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Detector 1st Minima Position y₁ = D · tan θ₁ = ${"%.1f".format(uiState.heisenbergFirstMinScreenYmm)} mm",
                        style = MaterialTheme.typography.labelMedium,
                        color = LaserCyan
                    )
                    Text(
                        text = "Momentum Uncertainty Δp_y = (h / λ) sin θ₁  ⟹  Δy · Δp_y = a · (h / λ) · (λ / a) = h ≥ h / 4π (Verified!)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SpectralEmerald
                    )
                }
            }
        }
    }
}

@Composable
private fun DetectorAngleControlCard(
    uiState: DiffractionLabUiState,
    onAngleChange: (Float) -> Unit,
    onNudgeAngle: (Float) -> Unit,
    onSnapToPeak: () -> Unit,
    onLockLeftReading: () -> Unit,
    onLockRightReading: () -> Unit,
    onRecordReading: () -> Unit
) {
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
                    text = "TELESCOPE / DETECTOR ANGLE (θ)",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Text(
                    text = "${"%.2f".format(uiState.telescopeAngleDeg)}°  (${uiState.vernierWindowA.formattedDMS})",
                    style = MaterialTheme.typography.titleMedium,
                    color = SpectralAmber
                )
            }
            val nearest = uiState.nearestPeak
            if (nearest != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(LabSurfaceElevated)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Nearest: m=${if (nearest.order > 0) "+${nearest.order}" else nearest.order} (${"%.1f".format(nearest.angleDeg)}°)",
                        style = MaterialTheme.typography.labelSmall,
                        color = LaserCyan
                    )
                }
            }
        }

        Slider(
            value = uiState.telescopeAngleDeg,
            onValueChange = onAngleChange,
            valueRange = -68f..68f,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("slider_telescope_angle"),
            colors = SliderDefaults.colors(
                thumbColor = SpectralAmber,
                activeTrackColor = SpectralAmber,
                inactiveTrackColor = LabSurfaceElevated
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { onNudgeAngle(-0.5f) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_angle_minus_half"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("-30′", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
            }
            OutlinedButton(
                onClick = { onNudgeAngle(-1f / 60f) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_angle_minus_minute"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("-1′", style = MaterialTheme.typography.labelMedium, color = SpectralAmber)
            }
            OutlinedButton(
                onClick = { onNudgeAngle(1f / 60f) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_angle_plus_minute"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("+1′", style = MaterialTheme.typography.labelMedium, color = SpectralAmber)
            }
            OutlinedButton(
                onClick = { onNudgeAngle(0.5f) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_angle_plus_half"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("+30′", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Left (a) & Right (b) quick lock buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onLockLeftReading,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Lock Left (a)", style = MaterialTheme.typography.labelSmall, color = LaserCyan)
            }
            OutlinedButton(
                onClick = onLockRightReading,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Lock Right (b)", style = MaterialTheme.typography.labelSmall, color = SpectralEmerald)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onSnapToPeak,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_snap_nearest_peak"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CenterFocusStrong,
                    contentDescription = "Snap to nearest peak",
                    tint = LaserCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Snap Peak", style = MaterialTheme.typography.labelLarge, color = LaserCyan)
            }

            Button(
                onClick = onRecordReading,
                modifier = Modifier
                    .weight(1.15f)
                    .height(48.dp)
                    .testTag("btn_record_reading"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LaserCyan,
                    contentColor = Color(0xFF041326)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlaylistAddCheck,
                    contentDescription = "Record reading to lab log",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Record Reading", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun SlitSpacingControlCard(
    uiState: DiffractionLabUiState,
    onSlitSpacingChange: (Float) -> Unit,
    onGratingLpiSelect: (Int) -> Unit
) {
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
                    text = "SLIT SPACING (d) & GRATING ELEMENT (e = 2.54/N)",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Text(
                    text = "d = ${"%.3f".format(uiState.slitSpacingUm)} µm  (${"%.4e".format(uiState.slitSpacingUm * 1e-4f)} cm)",
                    style = MaterialTheme.typography.titleMedium,
                    color = LaserCyan
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${uiState.linesPerInch} lines/in",
                    style = MaterialTheme.typography.labelLarge,
                    color = SpectralEmerald
                )
                Text(
                    text = "${uiState.linesPerMm.roundToInt()} lines/mm",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        Slider(
            value = uiState.slitSpacingUm,
            onValueChange = onSlitSpacingChange,
            valueRange = 0.80f..15.0f,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("slider_slit_spacing"),
            colors = SliderDefaults.colors(
                thumbColor = LaserCyan,
                activeTrackColor = LaserCyan,
                inactiveTrackColor = LabSurfaceElevated
            )
        )

        Text(
            text = "Standard Laboratory Gratings (Tap to Load):",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DiffractionPhysics.presetGratingsLpi.forEach { (lpi, label) ->
                val isSelected = kotlin.math.abs(uiState.linesPerInch - lpi) < 120
                FilterChip(
                    selected = isSelected,
                    onClick = { onGratingLpiSelect(lpi) },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.testTag("chip_grating_$lpi"),
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
}

@Composable
private fun WavelengthAndSourceControlCard(
    uiState: DiffractionLabUiState,
    onSelectSourceMode: (LightSourceMode) -> Unit,
    onWavelengthChange: (Float) -> Unit
) {
    val currentColor = wavelengthToColor(uiState.wavelengthNm)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LabSurface)
            .border(1.dp, LabBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(
            text = "LIGHT SOURCE & WAVELENGTH (λ)",
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
            LightSourceMode.entries.forEach { mode ->
                val selected = uiState.lightSourceMode == mode
                FilterChip(
                    selected = selected,
                    onClick = { onSelectSourceMode(mode) },
                    label = { Text(mode.shortName, style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("chip_source_${mode.name.lowercase()}"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpectralViolet.copy(alpha = 0.28f),
                        selectedLabelColor = TextPrimary,
                        containerColor = LabSurfaceElevated,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = uiState.lightSourceMode.description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(currentColor)
                        .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "λ = ${"%.1f".format(uiState.wavelengthNm)} nm  (${(uiState.wavelengthNm * 10f).roundToInt()} Å)",
                    style = MaterialTheme.typography.titleMedium,
                    color = currentColor
                )
            }
            Text(
                text = "Max Order n_max = ±${uiState.maxObservableOrder}",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            wavelengthToColor(380f),
                            wavelengthToColor(440f),
                            wavelengthToColor(490f),
                            wavelengthToColor(532f),
                            wavelengthToColor(580f),
                            wavelengthToColor(635f),
                            wavelengthToColor(700f)
                        )
                    )
                )
        )

        Slider(
            value = uiState.wavelengthNm,
            onValueChange = onWavelengthChange,
            valueRange = 380f..700f,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("slider_wavelength"),
            colors = SliderDefaults.colors(
                thumbColor = currentColor,
                activeTrackColor = currentColor,
                inactiveTrackColor = LabSurfaceElevated
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val presetsToShow = if (uiState.lightSourceMode == LightSourceMode.MONOCHROMATIC) {
                DiffractionPhysics.presetLaserWavelengths
            } else {
                uiState.activeSpectralLines
            }
            presetsToShow.forEach { line ->
                val lineCol = wavelengthToColor(line.wavelengthNm)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(LabSurfaceElevated)
                        .border(
                            width = 1.dp,
                            color = if (kotlin.math.abs(uiState.wavelengthNm - line.wavelengthNm) < 2f) lineCol else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onWavelengthChange(line.wavelengthNm) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(lineCol)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${line.name} (${line.wavelengthAngstrom} Å)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SecondaryOpticsControlCard(
    uiState: DiffractionLabUiState,
    onSlitWidthChange: (Float) -> Unit,
    onNumSlitsChange: (Int) -> Unit,
    onScreenDistanceChange: (Float) -> Unit,
    onToggleEnvelope: () -> Unit,
    onToggleWaves: () -> Unit,
    onResetApparatus: () -> Unit
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
            text = "APERTURE GEOMETRY & BENCH SETTINGS",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.benchMode == BenchExperimentMode.DIFFRACTION_GRATING) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Illuminated Slits (M): ${uiState.numSlits}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Resolving Power R₁ = ${uiState.resolvingPowerFirstOrder}",
                    style = MaterialTheme.typography.labelMedium,
                    color = SpectralViolet
                )
            }
            Slider(
                value = uiState.numSlits.toFloat(),
                onValueChange = { onNumSlitsChange(it.roundToInt()) },
                valueRange = 1f..60f,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("slider_num_slits"),
                colors = SliderDefaults.colors(
                    thumbColor = SpectralViolet,
                    activeTrackColor = SpectralViolet,
                    inactiveTrackColor = LabSurfaceElevated
                )
            )
        }

        val maxSlitWidth = if (uiState.benchMode == BenchExperimentMode.SINGLE_SLIT_HEISENBERG) {
            4.5f
        } else {
            (uiState.slitSpacingUm * 0.85f).coerceAtLeast(0.25f)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Single Slit Width (a): ${"%.2f".format(uiState.slitWidthUm)} µm",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Text(
                text = "Ratio d/a = ${"%.1f".format(uiState.slitSpacingUm / uiState.slitWidthUm)}",
                style = MaterialTheme.typography.labelMedium,
                color = SpectralEmerald
            )
        }
        Slider(
            value = uiState.slitWidthUm.coerceAtMost(maxSlitWidth),
            onValueChange = onSlitWidthChange,
            valueRange = 0.15f..maxSlitWidth,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("slider_slit_width"),
            colors = SliderDefaults.colors(
                thumbColor = SpectralEmerald,
                activeTrackColor = SpectralEmerald,
                inactiveTrackColor = LabSurfaceElevated
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Screen Distance (D): ${"%.2f".format(uiState.screenDistanceM)} m",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Text(
                text = "${(uiState.screenDistanceM * 100f).roundToInt()} cm",
                style = MaterialTheme.typography.labelMedium,
                color = LaserCyan
            )
        }
        Slider(
            value = uiState.screenDistanceM,
            onValueChange = onScreenDistanceChange,
            valueRange = 0.30f..2.00f,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("slider_screen_distance"),
            colors = SliderDefaults.colors(
                thumbColor = LaserCyan,
                activeTrackColor = LaserCyan,
                inactiveTrackColor = LabSurfaceElevated
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = uiState.showEnvelope,
                    onCheckedChange = { onToggleEnvelope() },
                    modifier = Modifier.testTag("switch_envelope"),
                    colors = SwitchDefaults.colors(checkedThumbColor = LaserCyan)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Slit Envelope", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = uiState.animateWaves,
                    onCheckedChange = { onToggleWaves() },
                    modifier = Modifier.testTag("switch_waves"),
                    colors = SwitchDefaults.colors(checkedThumbColor = SpectralEmerald)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Wavefronts", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onResetApparatus,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_reset_apparatus"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset apparatus",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reset Apparatus to Standard Defaults", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
        }
    }
}
