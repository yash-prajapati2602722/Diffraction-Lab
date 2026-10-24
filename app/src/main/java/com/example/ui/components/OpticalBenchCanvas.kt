package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BenchExperimentMode
import com.example.model.BenchVisualStyle
import com.example.model.DiffractionPhysics
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LabBorder
import com.example.ui.theme.LabSurface
import com.example.ui.theme.LabSurfaceElevated
import com.example.ui.theme.LabVoidBg
import com.example.ui.theme.LaserCyan
import com.example.ui.theme.SpectralAmber
import com.example.ui.theme.SpectralCrimson
import com.example.ui.theme.SpectralEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.wavelengthToColor
import com.example.viewmodel.DiffractionLabUiState
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

private const val MAX_DISPLAY_ANGLE_DEG = 68f

@Composable
fun InterferenceFringeScreenStrip(
    uiState: DiffractionLabUiState,
    onAngleChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val linearXCm = uiState.screenDistanceM * 100f * tan(Math.toRadians(uiState.telescopeAngleDeg.toDouble())).toFloat()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LabVoidBg)
            .border(1.dp, LabBorder, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "OBSERVATION SCREEN & METRIC RULER (DRAG θ / x)",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Text(
                text = "θ = ${uiState.vernierWindowA.formattedDMS}  |  x = ${"%.1f".format(linearXCm)} cm",
                style = MaterialTheme.typography.labelMedium,
                color = LaserCyan
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF04060E))
                .border(1.dp, Color(0xFF1F2D4D), RoundedCornerShape(10.dp))
                .testTag("fringe_screen_canvas")
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val frac = (offset.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                        val deg = (frac * 2f - 1f) * MAX_DISPLAY_ANGLE_DEG
                        onAngleChange(deg)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val frac = (change.position.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                        val deg = (frac * 2f - 1f) * MAX_DISPLAY_ANGLE_DEG
                        onAngleChange(deg)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val centerX = w / 2f

                if (!uiState.lampPowerOn) {
                    val offText = textMeasurer.measure(
                        text = "LAMP POWER OFF — SWITCH ON SOURCE TO OBSERVE FRINGES",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpectralCrimson
                        )
                    )
                    drawText(
                        textLayoutResult = offText,
                        topLeft = Offset((w - offText.size.width) / 2f, (h - offText.size.height) / 2f)
                    )
                    return@Canvas
                }

                // Sample continuous vertical stripes across [-68°, +68°]
                val columns = 180
                val colWidth = w / columns
                val effectiveSlits = if (uiState.benchMode == BenchExperimentMode.DIFFRACTION_GRATING) uiState.numSlits else 1
                val effectiveWidthUm = when (uiState.benchMode) {
                    BenchExperimentMode.WIRE_THICKNESS_LASER -> (uiState.wireThicknessMm * 15f).coerceIn(0.6f, 4.0f)
                    else -> uiState.slitWidthUm
                }

                for (i in 0 until columns) {
                    val norm = (i + 0.5f) / columns
                    val deg = (norm * 2f - 1f) * MAX_DISPLAY_ANGLE_DEG
                    val rad = Math.toRadians(deg.toDouble()).toFloat()

                    for (line in uiState.activeSpectralLines) {
                        val intensity = if (!uiState.isGratingInsertedOnBench) {
                            if (abs(deg) < 2.2f) line.relativeIntensity * (1f - abs(deg) / 2.2f) else 0f
                        } else {
                            line.relativeIntensity * DiffractionPhysics.calculateIntensityAtAngle(
                                angleRad = rad,
                                wavelengthNm = line.wavelengthNm,
                                slitSpacingUm = uiState.slitSpacingUm,
                                slitWidthUm = effectiveWidthUm,
                                numSlits = effectiveSlits,
                                includeEnvelope = uiState.showEnvelope || uiState.benchMode != BenchExperimentMode.DIFFRACTION_GRATING
                            )
                        }
                        if (intensity > 0.012f) {
                            val visualAlpha = sqrt(intensity.toDouble()).toFloat().coerceIn(0f, 1f)
                            val col = wavelengthToColor(line.wavelengthNm, alpha = visualAlpha * 0.88f)
                            drawRect(
                                color = col,
                                topLeft = Offset(i * colWidth, 6f),
                                size = Size(colWidth + 1.5f, h - 26f)
                            )
                        }
                    }
                }

                // Draw sharp principal peak cores & order badges when in Grating Mode
                if (uiState.benchMode == BenchExperimentMode.DIFFRACTION_GRATING) {
                    for (peak in uiState.principalPeaks) {
                        if (abs(peak.angleDeg) <= MAX_DISPLAY_ANGLE_DEG) {
                            val x = centerX + (peak.angleDeg / MAX_DISPLAY_ANGLE_DEG) * (w / 2f)
                            val lineCol = wavelengthToColor(peak.spectralLine.wavelengthNm)

                            drawLine(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        lineCol.copy(alpha = 0.55f * peak.relativeIntensity),
                                        Color.Transparent
                                    ),
                                    startX = x - 14f,
                                    endX = x + 14f
                                ),
                                start = Offset(x, 4f),
                                end = Offset(x, h - 24f),
                                strokeWidth = 16f
                            )
                            drawLine(
                                color = lineCol.copy(alpha = (0.4f + 0.6f * peak.relativeIntensity).coerceIn(0f, 1f)),
                                start = Offset(x, 4f),
                                end = Offset(x, h - 24f),
                                strokeWidth = if (peak.order == 0) 3.5f else 2.5f
                            )

                            if (uiState.activeSpectralLines.size == 1 || peak.spectralLine.wavelengthNm == 546.1f || peak.order == 0) {
                                val labelStr = if (peak.order > 0) "+${peak.order}" else "${peak.order}"
                                val textLayout = textMeasurer.measure(
                                    text = labelStr,
                                    style = TextStyle(
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                val tx = (x - textLayout.size.width / 2f).coerceIn(2f, w - textLayout.size.width - 2f)
                                drawText(
                                    textLayoutResult = textLayout,
                                    topLeft = Offset(tx, h - 20f)
                                )
                            }
                        }
                    }
                } else {
                    // Single Slit or Wire Diffraction: mark 1st minima (±b) on screen
                    val firstMinDeg = if (uiState.benchMode == BenchExperimentMode.SINGLE_SLIT_HEISENBERG) {
                        uiState.heisenbergFirstMinAngleDeg
                    } else {
                        (18f / (uiState.wireThicknessMm * 10f)).coerceIn(8f, 45f)
                    }
                    for (sign in listOf(-1f, 1f)) {
                        val mx = centerX + ((sign * firstMinDeg) / MAX_DISPLAY_ANGLE_DEG) * (w / 2f)
                        drawLine(
                            color = LaserCyan,
                            start = Offset(mx, 4f),
                            end = Offset(mx, h - 20f),
                            strokeWidth = 1.8f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                        )
                    }
                }

                // Draw IIT-B / oPhysics metric ruler ticks along bottom edge
                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(0f, h - 4f),
                    end = Offset(w, h - 4f),
                    strokeWidth = 2f
                )
                for (t in -6..6) {
                    val tx = centerX + (t / 6f) * (w * 0.46f)
                    drawLine(
                        color = Color(0xFF64748B),
                        start = Offset(tx, h - 9f),
                        end = Offset(tx, h - 2f),
                        strokeWidth = if (t == 0) 2f else 1f
                    )
                }

                // Draw active detector crosshair indicator
                val cursorX = (centerX + (uiState.telescopeAngleDeg / MAX_DISPLAY_ANGLE_DEG) * (w / 2f))
                    .coerceIn(4f, w - 4f)
                drawLine(
                    color = SpectralAmber,
                    start = Offset(cursorX, 0f),
                    end = Offset(cursorX, h),
                    strokeWidth = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f), 0f)
                )
                drawCircle(
                    color = SpectralAmber,
                    radius = 5f,
                    center = Offset(cursorX, 10f)
                )
            }
        }
    }
}

@Composable
fun OpticalBenchRayCanvas(
    uiState: DiffractionLabUiState,
    onAngleChange: (Float) -> Unit,
    onSelectBenchStyle: (BenchVisualStyle) -> Unit,
    onToggleLampPower: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wavefront_phase")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    val effectivePhase = if (uiState.animateWaves && uiState.lampPowerOn) wavePhase else 0f
    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LabSurface)
            .border(1.dp, LabBorder, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        // Top View Switcher Row: 2D Ray Bench vs 3D Perspective (oPhysics / PrayogShala) + Lamp Power Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BenchVisualStyle.entries.forEach { style ->
                    val selected = uiState.benchVisualStyle == style
                    Surface(
                        color = if (selected) LaserCyan.copy(alpha = 0.22f) else LabSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .border(
                                1.dp,
                                if (selected) LaserCyan else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectBenchStyle(style) }
                            .testTag("btn_style_${style.name.lowercase()}")
                    ) {
                        Text(
                            text = style.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) LaserCyan else TextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Lamp Power Toggle Pill
            Surface(
                color = if (uiState.lampPowerOn) SpectralEmerald.copy(alpha = 0.2f) else SpectralCrimson.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .border(
                        1.dp,
                        if (uiState.lampPowerOn) SpectralEmerald else SpectralCrimson,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onToggleLampPower() }
                    .testTag("btn_lamp_power")
            ) {
                Text(
                    text = if (uiState.lampPowerOn) "● LAMP ON" else "○ LAMP OFF",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (uiState.lampPowerOn) SpectralEmerald else SpectralCrimson,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(225.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LabVoidBg)
                .border(1.dp, Color(0xFF1B2848), RoundedCornerShape(12.dp))
                .testTag("optical_bench_ray_canvas")
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val gratingX = size.width * 0.36f
                        val centerY = size.height * 0.5f
                        if (offset.x > gratingX + 10f) {
                            val dx = offset.x - gratingX
                            val dy = centerY - offset.y
                            val deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            onAngleChange(deg.coerceIn(-MAX_DISPLAY_ANGLE_DEG, MAX_DISPLAY_ANGLE_DEG))
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val gratingX = size.width * 0.36f
                        val centerY = size.height * 0.5f
                        val dx = (change.position.x - gratingX).coerceAtLeast(12f)
                        val dy = centerY - change.position.y
                        val deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                        onAngleChange(deg.coerceIn(-MAX_DISPLAY_ANGLE_DEG, MAX_DISPLAY_ANGLE_DEG))
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                if (uiState.benchVisualStyle == BenchVisualStyle.PERSPECTIVE_3D_LAB) {
                    // 3D Isometric Perspective Optical Bench (oPhysics / IIT-B / PrayogShala 3D style)
                    val tablePath = Path().apply {
                        moveTo(w * 0.03f, h * 0.72f)
                        lineTo(w * 0.76f, h * 0.24f)
                        lineTo(w * 0.97f, h * 0.36f)
                        lineTo(w * 0.24f, h * 0.88f)
                        close()
                    }
                    drawPath(path = tablePath, color = Color(0xFF0E172A))
                    drawPath(path = tablePath, color = Color(0xFF283963), style = Stroke(width = 1.5f))

                    // 3D Optical Rail line from Laser -> Grating -> Screen
                    val laserPos = Offset(w * 0.14f, h * 0.68f)
                    val gratingPos = Offset(w * 0.44f, h * 0.48f)
                    val screenCenter = Offset(w * 0.78f, h * 0.26f)

                    drawLine(
                        color = Color(0xFF475569),
                        start = laserPos,
                        end = screenCenter,
                        strokeWidth = 4f
                    )

                    // 3D Observation Screen Backboard tilted along isometric axis
                    val screenHalfVec = Offset(w * 0.14f, h * 0.18f)
                    val screenTopLeft = Offset(screenCenter.x - screenHalfVec.x, screenCenter.y - screenHalfVec.y - 26f)
                    val screenTopRight = Offset(screenCenter.x + screenHalfVec.x, screenCenter.y + screenHalfVec.y - 26f)
                    val screenBotRight = Offset(screenCenter.x + screenHalfVec.x, screenCenter.y + screenHalfVec.y + 26f)
                    val screenBotLeft = Offset(screenCenter.x - screenHalfVec.x, screenCenter.y - screenHalfVec.y + 26f)

                    val screenPath = Path().apply {
                        moveTo(screenTopLeft.x, screenTopLeft.y)
                        lineTo(screenTopRight.x, screenTopRight.y)
                        lineTo(screenBotRight.x, screenBotRight.y)
                        lineTo(screenBotLeft.x, screenBotLeft.y)
                        close()
                    }
                    drawPath(path = screenPath, color = Color(0xFF050811))
                    drawPath(path = screenPath, color = Color(0xFF64748B), style = Stroke(width = 2.5f))

                    val primaryColor = wavelengthToColor(uiState.wavelengthNm)

                    if (uiState.lampPowerOn) {
                        // Incident 3D Laser Beam
                        drawLine(
                            color = primaryColor,
                            start = Offset(laserPos.x, laserPos.y - 16f),
                            end = Offset(gratingPos.x, gratingPos.y - 16f),
                            strokeWidth = 4.5f,
                            cap = StrokeCap.Round
                        )

                        // Diffracted 3D rays fanning onto angled screen
                        for (peak in uiState.principalPeaks) {
                            if (abs(peak.angleDeg) <= MAX_DISPLAY_ANGLE_DEG) {
                                val frac = (peak.angleDeg / MAX_DISPLAY_ANGLE_DEG).coerceIn(-1f, 1f)
                                val hitPoint = Offset(
                                    screenCenter.x + frac * screenHalfVec.x,
                                    screenCenter.y + frac * screenHalfVec.y - 16f
                                )
                                val col = wavelengthToColor(peak.spectralLine.wavelengthNm)
                                val alpha = (0.35f + 0.65f * peak.relativeIntensity).coerceIn(0.25f, 1f)
                                drawLine(
                                    color = col.copy(alpha = alpha),
                                    start = Offset(gratingPos.x, gratingPos.y - 16f),
                                    end = hitPoint,
                                    strokeWidth = if (peak.order == 0) 3.8f else 2.2f
                                )
                                drawCircle(
                                    color = col,
                                    radius = if (peak.order == 0) 6f else 4.2f,
                                    center = hitPoint
                                )
                            }
                        }
                    }

                    // 3D Grating Mount frame
                    val mountVec = Offset(w * 0.045f, h * 0.06f)
                    val mountPath = Path().apply {
                        moveTo(gratingPos.x - mountVec.x, gratingPos.y - mountVec.y - 32f)
                        lineTo(gratingPos.x + mountVec.x, gratingPos.y + mountVec.y - 32f)
                        lineTo(gratingPos.x + mountVec.x, gratingPos.y + mountVec.y + 4f)
                        lineTo(gratingPos.x - mountVec.x, gratingPos.y - mountVec.y + 4f)
                        close()
                    }
                    drawPath(path = mountPath, color = LaserCyan.copy(alpha = 0.22f))
                    drawPath(path = mountPath, color = LaserCyan, style = Stroke(width = 2f))

                    // 3D Laser Emitter Box
                    drawRoundRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(laserPos.x - 28f, laserPos.y - 28f),
                        size = Size(48f, 24f)
                    )
                    drawRoundRect(
                        color = if (uiState.lampPowerOn) primaryColor else Color(0xFF475569),
                        topLeft = Offset(laserPos.x - 28f, laserPos.y - 28f),
                        size = Size(48f, 24f),
                        style = Stroke(width = 2f)
                    )

                    // Active Detector Target on 3D Screen
                    val detFrac = (uiState.telescopeAngleDeg / MAX_DISPLAY_ANGLE_DEG).coerceIn(-1f, 1f)
                    val detHit = Offset(
                        screenCenter.x + detFrac * screenHalfVec.x,
                        screenCenter.y + detFrac * screenHalfVec.y - 16f
                    )
                    drawCircle(
                        color = SpectralAmber,
                        radius = 7f,
                        center = detHit,
                        style = Stroke(width = 2.5f)
                    )

                    val infoText = textMeasurer.measure(
                        text = "3D Optical Bench • D = ${"%.2f".format(uiState.screenDistanceM)} m • N = ${uiState.linesPerInch} LPI",
                        style = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 10.sp, color = TextSecondary)
                    )
                    drawText(textLayoutResult = infoText, topLeft = Offset(12f, 10f))
                } else {
                    // 2D Top-Down Wavefront & Ray Propagation Bench
                    val centerY = h / 2f
                    val sourceX = w * 0.06f
                    val gratingX = w * 0.34f
                    val screenX = w * 0.90f

                    drawLine(
                        color = Color(0xFF233256),
                        start = Offset(sourceX, centerY),
                        end = Offset(screenX, centerY),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                    )

                    val primaryBeamColor = if (uiState.activeSpectralLines.size == 1) {
                        wavelengthToColor(uiState.wavelengthNm)
                    } else {
                        Color(0xFFE0F2FE)
                    }

                    if (uiState.lampPowerOn) {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    primaryBeamColor.copy(alpha = 0.22f),
                                    primaryBeamColor.copy(alpha = 0.78f),
                                    primaryBeamColor.copy(alpha = 0.22f),
                                    Color.Transparent
                                ),
                                startY = centerY - 26f,
                                endY = centerY + 26f
                            ),
                            topLeft = Offset(sourceX + 24f, centerY - 26f),
                            size = Size(gratingX - sourceX - 24f, 52f)
                        )

                        val waveSpacingPx = (10f + (uiState.wavelengthNm - 380f) / 28f).coerceIn(10f, 24f)
                        var wx = sourceX + 28f + (effectivePhase * waveSpacingPx)
                        while (wx < gratingX - 4f) {
                            drawLine(
                                color = primaryBeamColor.copy(alpha = 0.55f),
                                start = Offset(wx, centerY - 22f),
                                end = Offset(wx, centerY + 22f),
                                strokeWidth = 2f,
                                cap = StrokeCap.Round
                            )
                            wx += waveSpacingPx
                        }

                        for (peak in uiState.principalPeaks) {
                            if (abs(peak.angleDeg) <= MAX_DISPLAY_ANGLE_DEG) {
                                val rayColor = wavelengthToColor(peak.spectralLine.wavelengthNm)
                                val screenY = centerY - (peak.angleDeg / MAX_DISPLAY_ANGLE_DEG) * (h * 0.44f)
                                val strokeW = if (peak.order == 0) 4.0f else (2.8f * peak.relativeIntensity).coerceAtLeast(1.4f)
                                val alpha = (0.30f + 0.65f * peak.relativeIntensity).coerceIn(0.25f, 0.95f)

                                drawLine(
                                    color = rayColor.copy(alpha = alpha),
                                    start = Offset(gratingX, centerY),
                                    end = Offset(screenX, screenY),
                                    strokeWidth = strokeW,
                                    cap = StrokeCap.Round
                                )
                                drawCircle(
                                    color = rayColor.copy(alpha = alpha),
                                    radius = if (peak.order == 0) 6f else 4.5f,
                                    center = Offset(screenX, screenY)
                                )
                            }
                        }

                        val numArcs = 5
                        val maxRadius = (screenX - gratingX) * 0.55f
                        for (i in 0 until numArcs) {
                            val r = ((i + effectivePhase) / numArcs) * maxRadius
                            if (r > 8f) {
                                val arcAlpha = (1f - (r / maxRadius)) * 0.25f
                                drawArc(
                                    color = primaryBeamColor.copy(alpha = arcAlpha),
                                    startAngle = -55f,
                                    sweepAngle = 110f,
                                    useCenter = false,
                                    topLeft = Offset(gratingX - r, centerY - r),
                                    size = Size(r * 2f, r * 2f),
                                    style = Stroke(width = 1.5f)
                                )
                            }
                        }
                    }

                    // Emitter housing
                    drawRoundRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(8f, centerY - 20f),
                        size = Size(sourceX + 18f, 40f),
                        style = Stroke(width = 2f)
                    )
                    drawRect(
                        color = if (uiState.lampPowerOn) primaryBeamColor else Color(0xFF475569),
                        topLeft = Offset(sourceX + 18f, centerY - 8f),
                        size = Size(6f, 16f)
                    )

                    // Grating / Single Slit / Wire obstacle at gratingX
                    val plateTop = centerY - h * 0.36f
                    val plateBottom = centerY + h * 0.36f
                    when (uiState.benchMode) {
                        BenchExperimentMode.DIFFRACTION_GRATING -> {
                            val visualSlits = uiState.numSlits.coerceIn(3, 18)
                            val totalPlateLen = plateBottom - plateTop
                            val step = totalPlateLen / visualSlits
                            val gapFraction = (uiState.slitWidthUm / uiState.slitSpacingUm).coerceIn(0.18f, 0.55f)
                            for (s in 0 until visualSlits) {
                                val segStart = plateTop + s * step
                                val solidLen = step * (1f - gapFraction)
                                drawLine(
                                    color = Color(0xFF94A3B8),
                                    start = Offset(gratingX, segStart),
                                    end = Offset(gratingX, segStart + solidLen),
                                    strokeWidth = 5f,
                                    cap = StrokeCap.Square
                                )
                            }
                        }
                        BenchExperimentMode.SINGLE_SLIT_HEISENBERG -> {
                            val gapHalf = (uiState.slitWidthUm * 6f).coerceIn(6f, 28f)
                            drawLine(
                                color = Color(0xFF94A3B8),
                                start = Offset(gratingX, plateTop),
                                end = Offset(gratingX, centerY - gapHalf),
                                strokeWidth = 6f
                            )
                            drawLine(
                                color = Color(0xFF94A3B8),
                                start = Offset(gratingX, centerY + gapHalf),
                                end = Offset(gratingX, plateBottom),
                                strokeWidth = 6f
                            )
                        }
                        BenchExperimentMode.WIRE_THICKNESS_LASER -> {
                            val wireRad = (uiState.wireThicknessMm * 45f).coerceIn(4f, 18f)
                            drawCircle(
                                color = SpectralAmber,
                                radius = wireRad,
                                center = Offset(gratingX, centerY)
                            )
                        }
                    }

                    // Observation Wall
                    drawLine(
                        color = Color(0xFF64748B),
                        start = Offset(screenX, h * 0.05f),
                        end = Offset(screenX, h * 0.95f),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )

                    // Detector Angle Vector
                    val targetScreenY = (centerY - (uiState.telescopeAngleDeg / MAX_DISPLAY_ANGLE_DEG) * (h * 0.44f))
                        .coerceIn(h * 0.06f, h * 0.94f)
                    drawLine(
                        color = SpectralAmber,
                        start = Offset(gratingX, centerY),
                        end = Offset(screenX, targetScreenY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f, 6f), 0f)
                    )
                    drawCircle(
                        color = SpectralAmber,
                        radius = 6f,
                        center = Offset(screenX, targetScreenY),
                        style = Stroke(width = 2.5f)
                    )

                    val apertureLabel = when (uiState.benchMode) {
                        BenchExperimentMode.DIFFRACTION_GRATING -> "Grating (M=${uiState.numSlits})"
                        BenchExperimentMode.SINGLE_SLIT_HEISENBERG -> "Slit (a=${"%.2f".format(uiState.slitWidthUm)}µm)"
                        BenchExperimentMode.WIRE_THICKNESS_LASER -> "Wire (d=${"%.2f".format(uiState.wireThicknessMm)}mm)"
                    }
                    val gratingLabel = textMeasurer.measure(
                        text = apertureLabel,
                        style = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 10.sp, color = TextSecondary)
                    )
                    drawText(
                        textLayoutResult = gratingLabel,
                        topLeft = Offset(gratingX - gratingLabel.size.width / 2f, h - 20f)
                    )

                    val screenLabel = textMeasurer.measure(
                        text = "D=${"%.2f".format(uiState.screenDistanceM)}m",
                        style = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 10.sp, color = TextSecondary)
                    )
                    drawText(
                        textLayoutResult = screenLabel,
                        topLeft = Offset(screenX - screenLabel.size.width - 6f, h - 20f)
                    )
                }
            }
        }
    }
}

@Composable
fun IntensityProfileCurveCanvas(
    uiState: DiffractionLabUiState,
    onAngleChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LabSurface)
            .border(1.dp, LabBorder, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "INTENSITY DISTRIBUTION I(θ) & ENVELOPE",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Text(
                text = "I(θ) = ${"%.1f".format(uiState.currentDetectorIntensity * 100f)}%",
                style = MaterialTheme.typography.labelLarge,
                color = SpectralAmber
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(LabVoidBg)
                .border(1.dp, Color(0xFF1B2848), RoundedCornerShape(10.dp))
                .testTag("intensity_profile_canvas")
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val frac = (offset.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                        val deg = (frac * 2f - 1f) * MAX_DISPLAY_ANGLE_DEG
                        onAngleChange(deg)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val frac = (change.position.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                        val deg = (frac * 2f - 1f) * MAX_DISPLAY_ANGLE_DEG
                        onAngleChange(deg)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val baseLineY = h - 18f
                val usableHeight = h - 28f
                val centerX = w / 2f

                for (frac in listOf(0.25f, 0.5f, 0.75f, 1.0f)) {
                    val gy = baseLineY - usableHeight * frac
                    drawLine(
                        color = Color(0xFF162038),
                        start = Offset(0f, gy),
                        end = Offset(w, gy),
                        strokeWidth = 1f
                    )
                }
                drawLine(
                    color = Color(0xFF283963),
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, baseLineY),
                    strokeWidth = 1f
                )

                if (!uiState.lampPowerOn) return@Canvas

                val samples = 360
                val effectiveSlits = if (uiState.benchMode == BenchExperimentMode.DIFFRACTION_GRATING) uiState.numSlits else 1
                val effectiveWidthUm = when (uiState.benchMode) {
                    BenchExperimentMode.WIRE_THICKNESS_LASER -> (uiState.wireThicknessMm * 15f).coerceIn(0.6f, 4.0f)
                    else -> uiState.slitWidthUm
                }

                if (uiState.showEnvelope || uiState.benchMode != BenchExperimentMode.DIFFRACTION_GRATING) {
                    val envPath = Path()
                    val refWl = uiState.activeSpectralLines.first().wavelengthNm
                    for (i in 0..samples) {
                        val frac = i.toFloat() / samples
                        val deg = (frac * 2f - 1f) * MAX_DISPLAY_ANGLE_DEG
                        val rad = Math.toRadians(deg.toDouble()).toFloat()
                        val env = DiffractionPhysics.calculateEnvelopeAtAngle(
                            angleRad = rad,
                            wavelengthNm = refWl,
                            slitWidthUm = effectiveWidthUm
                        )
                        val x = frac * w
                        val y = baseLineY - env * usableHeight
                        if (i == 0) envPath.moveTo(x, y) else envPath.lineTo(x, y)
                    }
                    drawPath(
                        path = envPath,
                        color = TextMuted.copy(alpha = 0.65f),
                        style = Stroke(
                            width = 1.8f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                    )
                }

                for (line in uiState.activeSpectralLines) {
                    val curvePath = Path()
                    val lineCol = wavelengthToColor(line.wavelengthNm)
                    for (i in 0..samples) {
                        val frac = i.toFloat() / samples
                        val deg = (frac * 2f - 1f) * MAX_DISPLAY_ANGLE_DEG
                        val rad = Math.toRadians(deg.toDouble()).toFloat()
                        val intensity = line.relativeIntensity * DiffractionPhysics.calculateIntensityAtAngle(
                            angleRad = rad,
                            wavelengthNm = line.wavelengthNm,
                            slitSpacingUm = uiState.slitSpacingUm,
                            slitWidthUm = effectiveWidthUm,
                            numSlits = effectiveSlits,
                            includeEnvelope = uiState.showEnvelope || uiState.benchMode != BenchExperimentMode.DIFFRACTION_GRATING
                        )
                        val x = frac * w
                        val y = baseLineY - intensity * usableHeight
                        if (i == 0) curvePath.moveTo(x, y) else curvePath.lineTo(x, y)
                    }
                    drawPath(
                        path = curvePath,
                        color = lineCol,
                        style = Stroke(width = 2.2f, cap = StrokeCap.Round)
                    )
                }

                val cursorX = (centerX + (uiState.telescopeAngleDeg / MAX_DISPLAY_ANGLE_DEG) * (w / 2f))
                    .coerceIn(2f, w - 2f)
                val cursorY = baseLineY - uiState.currentDetectorIntensity * usableHeight

                drawLine(
                    color = SpectralAmber,
                    start = Offset(cursorX, 4f),
                    end = Offset(cursorX, baseLineY),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                )
                drawCircle(
                    color = SpectralAmber,
                    radius = 5.5f,
                    center = Offset(cursorX, cursorY)
                )
            }
        }
    }
}
