package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CrosswireStyle
import com.example.ui.theme.JetBrainsMonoFamily
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
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun SpectrometerEyepieceAndTurntable(
    uiState: DiffractionLabUiState,
    onAngleChange: (Float) -> Unit,
    onCrosswireStyleChange: (CrosswireStyle) -> Unit = {},
    onSlitOpeningChange: (Float) -> Unit = {},
    onFocusChange: (Float) -> Unit = {},
    onAutoSchusterFocus: () -> Unit = {},
    onPrismTableAngleChange: (Float) -> Unit = {},
    onVernierDivisionsChange: (Int) -> Unit = {},
    onLockLeftReading: () -> Unit = {},
    onLockRightReading: () -> Unit = {},
    onRecordObservation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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
            Text(
                text = "PRAYOGSHALA & AMRITA VIRTUAL SPECTROMETER",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Text(
                text = "LC = ${if (uiState.vernierDivisions == 60) "30″" else "1′"} (${uiState.vernierDivisions} VSD)",
                style = MaterialTheme.typography.labelMedium,
                color = SpectralEmerald
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Telescope Eyepiece Crosswire View (Zoomed Field of View ±3.2°)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(168.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF03050C))
                        .border(3.dp, Color(0xFF334155), CircleShape)
                        .testTag("spectrometer_eyepiece_canvas")
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val deltaDeg = -dragAmount.x * 0.025f
                                onAngleChange(uiState.telescopeAngleDeg + deltaDeg)
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val r = size.minDimension / 2f
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val fovHalfDeg = 3.2f

                        if (uiState.lampPowerOn) {
                            // Defocus blur and slit opening width affect visual line thickness in eyepiece
                            val defocusBlurPx = uiState.opticalDefocusAmount * 28f
                            val slitWidthPx = (uiState.collimatorSlitOpeningMm * 14f).coerceIn(2.5f, 26f)

                            for (peak in uiState.principalPeaks) {
                                val relDeg = peak.angleDeg - uiState.telescopeAngleDeg
                                if (abs(relDeg) <= fovHalfDeg) {
                                    val lineX = center.x + (relDeg / fovHalfDeg) * r
                                    val col = wavelengthToColor(peak.spectralLine.wavelengthNm)
                                    val haloRadius = 18f + defocusBlurPx + slitWidthPx

                                    drawLine(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                col.copy(alpha = (0.65f * peak.relativeIntensity * (1f - 0.5f * uiState.opticalDefocusAmount)).coerceIn(0.1f, 0.9f)),
                                                Color.Transparent
                                            ),
                                            startX = lineX - haloRadius,
                                            endX = lineX + haloRadius
                                        ),
                                        start = Offset(lineX, 0f),
                                        end = Offset(lineX, size.height),
                                        strokeWidth = haloRadius * 1.6f
                                    )
                                    drawLine(
                                        color = col.copy(alpha = (1f - 0.55f * uiState.opticalDefocusAmount).coerceIn(0.25f, 1f)),
                                        start = Offset(lineX, 0f),
                                        end = Offset(lineX, size.height),
                                        strokeWidth = slitWidthPx + defocusBlurPx * 0.4f
                                    )
                                }
                            }
                        }

                        // Render selected Crosswire Reticle style (Manual Page 61 Precaution 4: Multiplication × mark)
                        when (uiState.crosswireStyle) {
                            CrosswireStyle.MULTIPLICATION_X -> {
                                drawLine(
                                    color = SpectralAmber.copy(alpha = 0.88f),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 1.6f
                                )
                                drawLine(
                                    color = SpectralAmber.copy(alpha = 0.88f),
                                    start = Offset(size.width, 0f),
                                    end = Offset(0f, size.height),
                                    strokeWidth = 1.6f
                                )
                                drawLine(
                                    color = SpectralAmber.copy(alpha = 0.45f),
                                    start = Offset(center.x, 0f),
                                    end = Offset(center.x, size.height),
                                    strokeWidth = 1f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                                )
                            }
                            CrosswireStyle.STANDARD_PLUS -> {
                                drawLine(
                                    color = SpectralAmber.copy(alpha = 0.9f),
                                    start = Offset(center.x, 0f),
                                    end = Offset(center.x, size.height),
                                    strokeWidth = 1.6f
                                )
                                drawLine(
                                    color = SpectralAmber.copy(alpha = 0.65f),
                                    start = Offset(0f, center.y),
                                    end = Offset(size.width, center.y),
                                    strokeWidth = 1.4f
                                )
                            }
                            CrosswireStyle.DOUBLE_HAIRLINE -> {
                                drawLine(
                                    color = SpectralAmber.copy(alpha = 0.9f),
                                    start = Offset(center.x - 4f, 0f),
                                    end = Offset(center.x - 4f, size.height),
                                    strokeWidth = 1.4f
                                )
                                drawLine(
                                    color = SpectralAmber.copy(alpha = 0.9f),
                                    start = Offset(center.x + 4f, 0f),
                                    end = Offset(center.x + 4f, size.height),
                                    strokeWidth = 1.4f
                                )
                                drawLine(
                                    color = SpectralAmber.copy(alpha = 0.5f),
                                    start = Offset(0f, center.y),
                                    end = Offset(size.width, center.y),
                                    strokeWidth = 1.2f
                                )
                            }
                        }

                        drawCircle(
                            color = Color(0xFF475569),
                            radius = r - 4f,
                            center = center,
                            style = Stroke(width = 2f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Eyepiece (${uiState.crosswireStyle.label})",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            // Right: Top-down Spectrometer Turntable
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(168.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(LabVoidBg)
                        .border(1.dp, LabBorder, RoundedCornerShape(14.dp))
                        .testTag("spectrometer_turntable_canvas")
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val dx = (change.position.x - cx).coerceAtLeast(8f)
                                val dy = cy - change.position.y
                                val deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                onAngleChange(deg.coerceIn(-75f, 75f))
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val outerR = size.minDimension * 0.42f
                        val prismTableR = size.minDimension * 0.19f

                        drawCircle(
                            color = Color(0xFF1E293B),
                            radius = outerR,
                            center = center,
                            style = Stroke(width = 12f)
                        )
                        for (degTick in 0 until 360 step 15) {
                            val rad = Math.toRadians(degTick.toDouble())
                            val r1 = outerR - 6f
                            val r2 = outerR + 6f
                            drawLine(
                                color = Color(0xFF64748B),
                                start = Offset(
                                    center.x + (r1 * cos(rad)).toFloat(),
                                    center.y + (r1 * sin(rad)).toFloat()
                                ),
                                end = Offset(
                                    center.x + (r2 * cos(rad)).toFloat(),
                                    center.y + (r2 * sin(rad)).toFloat()
                                ),
                                strokeWidth = if (degTick % 45 == 0) 2f else 1f
                            )
                        }

                        // Fixed Collimator Tube on the left (180°)
                        drawLine(
                            color = Color(0xFF475569),
                            start = Offset(4f, center.y),
                            end = Offset(center.x - prismTableR, center.y),
                            strokeWidth = 10f,
                            cap = StrokeCap.Butt
                        )
                        val beamCol = wavelengthToColor(uiState.wavelengthNm)
                        if (uiState.lampPowerOn) {
                            drawLine(
                                color = beamCol,
                                start = Offset(6f, center.y),
                                end = Offset(center.x, center.y),
                                strokeWidth = 2.5f
                            )
                        }

                        // Central Prism/Grating Table
                        drawCircle(
                            color = Color(0xFF0F172A),
                            radius = prismTableR,
                            center = center
                        )
                        drawCircle(
                            color = LaserCyan.copy(alpha = 0.7f),
                            radius = prismTableR,
                            center = center,
                            style = Stroke(width = 2f)
                        )
                        // Grating holder rotated by prismTableAngleDeg (45°/135° normal setting)
                        val tableVisualRot = if (uiState.prismTableAngleDeg == 45f || uiState.prismTableAngleDeg == 135f) 0f else (uiState.prismTableAngleDeg - 45f)
                        rotate(degrees = tableVisualRot, pivot = center) {
                            drawLine(
                                color = TextPrimary,
                                start = Offset(center.x, center.y - prismTableR * 0.75f),
                                end = Offset(center.x, center.y + prismTableR * 0.75f),
                                strokeWidth = 3f
                            )
                        }

                        // Rotating Telescope Arm at telescopeAngleDeg
                        rotate(degrees = -uiState.telescopeAngleDeg, pivot = center) {
                            if (uiState.lampPowerOn) {
                                drawLine(
                                    color = beamCol.copy(alpha = 0.85f),
                                    start = Offset(center.x, center.y),
                                    end = Offset(center.x + outerR + 10f, center.y),
                                    strokeWidth = 2.5f
                                )
                            }
                            drawLine(
                                color = SpectralAmber,
                                start = Offset(center.x + prismTableR + 2f, center.y),
                                end = Offset(center.x + outerR + 12f, center.y),
                                strokeWidth = 9f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Turntable (Table: ${uiState.prismTableAngleDeg.roundToInt()}°)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Left (a) & Right (b) Vernier V1/V2 Capture Bar (Medicaps Manual Page 60 Table)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(LabVoidBg)
                .border(1.dp, Color(0xFF22335A), RoundedCornerShape(12.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "LEFT (a): ${uiState.lockedLeftAngleDeg?.let { "${"%.2f".format(it)}°" } ?: "Not set"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = LaserCyan
                )
                Text(
                    text = "RIGHT (b): ${uiState.lockedRightAngleDeg?.let { "+${"%.2f".format(abs(it))}°" } ?: "Not set"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = SpectralEmerald
                )
                val diff2Theta = if (uiState.lockedLeftAngleDeg != null && uiState.lockedRightAngleDeg != null) {
                    abs(uiState.lockedRightAngleDeg - uiState.lockedLeftAngleDeg)
                } else {
                    abs(uiState.telescopeAngleDeg) * 2f
                }
                Text(
                    text = "2θ = ${"%.2f".format(diff2Theta)}°",
                    style = MaterialTheme.typography.labelLarge,
                    color = SpectralAmber
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onLockLeftReading,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_lock_left_a"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Lock Left (a)", style = MaterialTheme.typography.labelMedium, color = LaserCyan)
                }
                OutlinedButton(
                    onClick = onLockRightReading,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_lock_right_b"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Lock Right (b)", style = MaterialTheme.typography.labelMedium, color = SpectralEmerald)
                }
                Button(
                    onClick = onRecordObservation,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(48.dp)
                        .testTag("btn_log_v1_v2"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpectralAmber,
                        contentColor = Color(0xFF1F1200)
                    )
                ) {
                    Text("Log V₁/V₂ Row", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Spectrometer Calibration & Schuster Focus Controls (Manual 5.3 & 5.4)
        Text(
            text = "SPECTROMETER ALIGNMENT, FOCUS & CROSSWIRE OPTIONS",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Crosswire & Vernier Least Count Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CrosswireStyle.entries.forEach { style ->
                FilterChip(
                    selected = uiState.crosswireStyle == style,
                    onClick = { onCrosswireStyleChange(style) },
                    label = { Text(style.label, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpectralAmber.copy(alpha = 0.22f),
                        selectedLabelColor = SpectralAmber,
                        containerColor = LabSurfaceElevated,
                        labelColor = TextSecondary
                    )
                )
            }
            listOf(30 to "30 VSD (LC=1′)", 60 to "60 VSD (LC=30″)").forEach { (divs, label) ->
                FilterChip(
                    selected = uiState.vernierDivisions == divs,
                    onClick = { onVernierDivisionsChange(divs) },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpectralEmerald.copy(alpha = 0.22f),
                        selectedLabelColor = SpectralEmerald,
                        containerColor = LabSurfaceElevated,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        // 4-Step Normal Incidence Prism Table Presets (0° -> 90° -> 45° / 135°)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                0f to "Step 2: Direct (0°)",
                90f to "Step 3: 90° Mirror",
                45f to "Step 5: 45° Normal",
                135f to "Step 5: 135° Normal"
            ).forEach { (deg, label) ->
                FilterChip(
                    selected = uiState.prismTableAngleDeg == deg,
                    onClick = { onPrismTableAngleChange(deg) },
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

        // Collimator Slit Width & Telescope Focus Knob Sliders
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Slit Width: ${"%.2f".format(uiState.collimatorSlitOpeningMm)} mm",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary
                )
                Slider(
                    value = uiState.collimatorSlitOpeningMm,
                    onValueChange = onSlitOpeningChange,
                    valueRange = 0.10f..1.80f,
                    colors = SliderDefaults.colors(
                        thumbColor = LaserCyan,
                        activeTrackColor = LaserCyan,
                        inactiveTrackColor = LabSurfaceElevated
                    )
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Schuster Focus: ${uiState.telescopeFocusPercent.roundToInt()}% ${if (uiState.opticalDefocusAmount < 0.08f) "(Sharp)" else "(Blur)"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (uiState.opticalDefocusAmount < 0.08f) SpectralEmerald else SpectralAmber
                )
                Slider(
                    value = uiState.telescopeFocusPercent,
                    onValueChange = onFocusChange,
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = SpectralEmerald,
                        activeTrackColor = SpectralEmerald,
                        inactiveTrackColor = LabSurfaceElevated
                    )
                )
            }
        }

        OutlinedButton(
            onClick = onAutoSchusterFocus,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_auto_schuster_focus"),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "Auto-Run Schuster's Parallel Focus & Spirit Level Alignment",
                style = MaterialTheme.typography.labelMedium,
                color = SpectralEmerald
            )
        }
    }
}

@Composable
fun VernierScaleMagnifierCard(
    uiState: DiffractionLabUiState,
    modifier: Modifier = Modifier
) {
    val vernier = uiState.vernierWindowA
    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = modifier
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
            Text(
                text = "MAGNIFIED VERNIER SCALE (VERNIER V₁ & V₂)",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Text(
                text = "V₁ = ${vernier.formattedDMS}",
                style = MaterialTheme.typography.labelLarge,
                color = SpectralAmber
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF060A14))
                .border(1.dp, Color(0xFF24345A), RoundedCornerShape(10.dp))
                .testTag("vernier_scale_canvas")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val midY = h * 0.48f

                drawRect(
                    color = Color(0xFF111B30),
                    topLeft = Offset(0f, 0f),
                    size = Size(w, midY)
                )
                drawRect(
                    color = Color(0xFF0A1120),
                    topLeft = Offset(0f, midY),
                    size = Size(w, h - midY)
                )
                drawLine(
                    color = LaserCyan.copy(alpha = 0.6f),
                    start = Offset(0f, midY),
                    end = Offset(w, midY),
                    strokeWidth = 1.5f
                )

                val absDeg = abs(uiState.telescopeAngleDeg)
                val baseDeg = floor(absDeg).toInt()
                val pxPerHalfDeg = w / 12f
                val zeroVernierX = w * 0.22f

                val fractionalHalfDeg = (absDeg - baseDeg) * 2f
                for (i in -3..12) {
                    val tickHalfDeg = i
                    val tickDegVal = baseDeg + tickHalfDeg * 0.5f
                    if (tickDegVal >= 0f) {
                        val x = zeroVernierX + (tickHalfDeg - fractionalHalfDeg) * pxPerHalfDeg
                        if (x in 4f..(w - 4f)) {
                            val isFullDegree = (tickHalfDeg % 2 == 0)
                            val tickH = if (isFullDegree) 18f else 11f
                            drawLine(
                                color = TextPrimary,
                                start = Offset(x, midY - tickH),
                                end = Offset(x, midY),
                                strokeWidth = if (isFullDegree) 2f else 1.2f
                            )
                            if (isFullDegree) {
                                val lbl = textMeasurer.measure(
                                    text = "${tickDegVal.toInt()}°",
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                drawText(
                                    textLayoutResult = lbl,
                                    topLeft = Offset(x - lbl.size.width / 2f, 4f)
                                )
                            }
                        }
                    }
                }

                val maxVsd = uiState.vernierDivisions
                val vsdStepPx = pxPerHalfDeg * (29f / 30f) / (if (maxVsd == 60) 6.4f else 3.2f)
                val stepInc = if (maxVsd == 60) 4 else 2
                for (v in 0..maxVsd step stepInc) {
                    val vx = zeroVernierX + v * vsdStepPx
                    if (vx in 6f..(w - 6f)) {
                        val isCoincident = abs(v - vernier.vernierCoincidence) <= (stepInc / 2)
                        val isMajor = (v % 10 == 0)
                        val tickH = if (isMajor) 18f else 11f
                        drawLine(
                            color = if (isCoincident) SpectralAmber else LaserCyan,
                            start = Offset(vx, midY),
                            end = Offset(vx, midY + tickH),
                            strokeWidth = if (isCoincident) 2.6f else 1.3f
                        )
                        if (isMajor) {
                            val vLbl = textMeasurer.measure(
                                text = "$v",
                                style = TextStyle(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 9.sp,
                                    color = if (isCoincident) SpectralAmber else LaserCyan
                                )
                            )
                            drawText(
                                textLayoutResult = vLbl,
                                topLeft = Offset(vx - vLbl.size.width / 2f, midY + 20f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VernierStatPill(
                label = "MSR (Main Scale)",
                value = "${"%.1f".format(vernier.mainScaleDeg)}°",
                accent = LaserCyan,
                modifier = Modifier.weight(1f)
            )
            VernierStatPill(
                label = "VSC × LC",
                value = "${vernier.vernierCoincidence} × ${if (uiState.vernierDivisions == 60) "30″" else "1′"}",
                accent = SpectralAmber,
                modifier = Modifier.weight(1f)
            )
            VernierStatPill(
                label = "Vernier V₂ (+180°)",
                value = uiState.vernierWindowB.formattedDMS,
                accent = SpectralEmerald,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun VernierStatPill(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = LabSurfaceElevated,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = accent
            )
        }
    }
}
