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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.ui.theme.LaserCyan
import com.example.ui.theme.SpectralAmber
import com.example.ui.theme.SpectralCrimson
import com.example.ui.theme.SpectralEmerald
import com.example.ui.theme.SpectralViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.wavelengthToColor
import com.example.viewmodel.DiffractionLabUiState
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private data class Vec3(val x: Float, val y: Float, val z: Float)

private data class ProjectedPt(
    val x: Float,
    val y: Float,
    val zDepth: Float,
    val scale: Float
)

private data class Poly3D(
    val points: List<Vec3>,
    val fillColor: Color,
    val strokeColor: Color? = null,
    val strokeWidth: Float = 1f
)

private enum class DragControlMode(val label: String) {
    ORBIT_CAMERA("1-Finger: Free Orbit 360°"),
    PAN_CAMERA("1-Finger: Free Pan (X/Y)"),
    SWING_TELESCOPE("1-Finger: Swing Telescope (θ)")
}

@Composable
fun Prayogshala3DSpectrometerCard(
    uiState: DiffractionLabUiState,
    onAngleChange: (Float) -> Unit,
    onToggleLampPower: () -> Unit,
    modifier: Modifier = Modifier
) {
    var camYawDeg by remember { mutableFloatStateOf(-38f) }
    var camPitchDeg by remember { mutableFloatStateOf(26f) }
    var camRollDeg by remember { mutableFloatStateOf(0f) }
    var camZoom by remember { mutableFloatStateOf(1.12f) }
    var camPanX by remember { mutableFloatStateOf(0f) }
    var camPanY by remember { mutableFloatStateOf(0f) }
    var xRayMode by remember { mutableStateOf(true) }
    var showFig51bAnatomy by remember { mutableStateOf(true) }
    var showCurvedScreenArc by remember { mutableStateOf(true) }
    var showPipEyepiece by remember { mutableStateOf(true) }
    var autoOrbit by remember { mutableStateOf(false) }
    var dragMode by remember { mutableStateOf(DragControlMode.ORBIT_CAMERA) }

    val infiniteTransition = rememberInfiniteTransition(label = "orbit_3d")
    val orbitPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle"
    )

    val effectiveYawDeg = if (autoOrbit) (camYawDeg + orbitPhase) % 360f else camYawDeg
    val textMeasurer = rememberTextMeasurer()

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
            Column {
                Text(
                    text = "PRAYOGSHALA 3D SPECTROMETER (FIG 5.1b ANATOMY)",
                    style = MaterialTheme.typography.labelMedium,
                    color = LaserCyan
                )
                Text(
                    text = "360° Interactive 3D Apparatus • Drag to Orbit or Swing Telescope",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

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
                    .testTag("btn_3d_lamp_power")
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DragControlMode.entries.forEach { mode ->
                val selected = dragMode == mode
                FilterChip(
                    selected = selected,
                    onClick = { dragMode = mode },
                    label = { Text(mode.label, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpectralAmber.copy(alpha = 0.24f),
                        selectedLabelColor = SpectralAmber,
                        containerColor = LabSurfaceElevated,
                        labelColor = TextSecondary
                    )
                )
            }
            FilterChip(
                selected = showFig51bAnatomy,
                onClick = { showFig51bAnatomy = !showFig51bAnatomy },
                label = { Text("Fig 5.1(b) Callouts", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LaserCyan.copy(alpha = 0.22f),
                    selectedLabelColor = LaserCyan,
                    containerColor = LabSurfaceElevated,
                    labelColor = TextSecondary
                )
            )
            FilterChip(
                selected = xRayMode,
                onClick = { xRayMode = !xRayMode },
                label = { Text("X-Ray Optics", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LaserCyan.copy(alpha = 0.22f),
                    selectedLabelColor = LaserCyan,
                    containerColor = LabSurfaceElevated,
                    labelColor = TextSecondary
                )
            )
            FilterChip(
                selected = showPipEyepiece,
                onClick = { showPipEyepiece = !showPipEyepiece },
                label = { Text("PiP Eyepiece", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SpectralEmerald.copy(alpha = 0.22f),
                    selectedLabelColor = SpectralEmerald,
                    containerColor = LabSurfaceElevated,
                    labelColor = TextSecondary
                )
            )
            FilterChip(
                selected = showCurvedScreenArc,
                onClick = { showCurvedScreenArc = !showCurvedScreenArc },
                label = { Text("3D Spectrum Arc", style = MaterialTheme.typography.labelSmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SpectralViolet.copy(alpha = 0.22f),
                    selectedLabelColor = SpectralViolet,
                    containerColor = LabSurfaceElevated,
                    labelColor = TextSecondary
                )
            )
            FilterChip(
                selected = autoOrbit,
                onClick = { autoOrbit = !autoOrbit },
                label = { Text("Auto-Rotate 3D", style = MaterialTheme.typography.labelSmall) }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF050914), Color(0xFF0B1328), Color(0xFF060A17))
                    )
                )
                .border(1.dp, Color(0xFF24355C), RoundedCornerShape(14.dp))
                .testTag("prayogshala_3d_canvas")
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            autoOrbit = false
                            camYawDeg = -38f
                            camPitchDeg = 26f
                            camRollDeg = 0f
                            camZoom = 1.12f
                            camPanX = 0f
                            camPanY = 0f
                        }
                    )
                }
                .pointerInput(dragMode) {
                    // Immediate raw gesture handler that consumes all pointer events before parent
                    // verticalScroll can intercept vertical drags, supporting 1-finger Orbit/Pan/Swing
                    // and 2-finger Pinch-Zoom + Two-Finger Pan simultaneously.
                    awaitEachGesture {
                        val firstDown = awaitFirstDown(requireUnconsumed = false)
                        firstDown.consume()
                        do {
                            val event = awaitPointerEvent()
                            val pressedCount = event.changes.count { it.pressed }
                            if (pressedCount >= 2) {
                                autoOrbit = false
                                val zoomChange = event.calculateZoom()
                                val panChange = event.calculatePan()
                                if (zoomChange != 1f && zoomChange > 0f) {
                                    camZoom = (camZoom * zoomChange).coerceIn(0.45f, 3.20f)
                                }
                                camPanX = (camPanX + panChange.x).coerceIn(-280f, 280f)
                                camPanY = (camPanY + panChange.y).coerceIn(-220f, 220f)
                                event.changes.forEach { it.consume() }
                            } else if (pressedCount == 1) {
                                val change = event.changes.firstOrNull()
                                if (change != null) {
                                    val delta = change.position - change.previousPosition
                                    if (delta != Offset.Zero) {
                                        when (dragMode) {
                                            DragControlMode.ORBIT_CAMERA -> {
                                                autoOrbit = false
                                                camYawDeg = (camYawDeg + delta.x * 0.52f) % 360f
                                                camPitchDeg = (camPitchDeg + delta.y * 0.48f).coerceIn(-88f, 88f)
                                            }
                                            DragControlMode.PAN_CAMERA -> {
                                                autoOrbit = false
                                                camPanX = (camPanX + delta.x * 1.15f).coerceIn(-280f, 280f)
                                                camPanY = (camPanY + delta.y * 1.15f).coerceIn(-220f, 220f)
                                            }
                                            DragControlMode.SWING_TELESCOPE -> {
                                                val nextAngle = (uiState.telescopeAngleDeg - delta.x * 0.25f).coerceIn(-72f, 72f)
                                                onAngleChange(nextAngle)
                                            }
                                        }
                                    }
                                    change.consume()
                                }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w * 0.5f + camPanX
                val cy = h * 0.56f + camPanY

                val yawRad = Math.toRadians(effectiveYawDeg.toDouble()).toFloat()
                val pitchRad = Math.toRadians(camPitchDeg.toDouble()).toFloat()
                val rollRad = Math.toRadians(camRollDeg.toDouble()).toFloat()
                val cosY = cos(yawRad)
                val sinY = sin(yawRad)
                val cosP = cos(pitchRad)
                val sinP = sin(pitchRad)
                val cosR = cos(rollRad)
                val sinR = sin(rollRad)

                val fovScale = (w.coerceAtMost(h) * 0.92f) * camZoom
                val cameraDist = 420f

                fun project(v: Vec3): ProjectedPt {
                    val rx1 = v.x * cosY - v.z * sinY
                    val rz1 = v.x * sinY + v.z * cosY
                    val ry1 = v.y * cosP - rz1 * sinP
                    val rz = v.y * sinP + rz1 * cosP
                    val rx = rx1 * cosR - ry1 * sinR
                    val ry = rx1 * sinR + ry1 * cosR

                    val denom = (cameraDist + rz).coerceAtLeast(60f)
                    val persp = fovScale / denom
                    return ProjectedPt(
                        x = cx + rx * persp,
                        y = cy - ry * persp,
                        zDepth = rz,
                        scale = persp
                    )
                }

                // 1. 3D Optical Laboratory Table Grid (y = -72f)
                val tableY = -72f
                val gridExtent = 180f
                val gridStep = 30f
                var g = -gridExtent
                while (g <= gridExtent) {
                    val p1 = project(Vec3(g, tableY, -gridExtent))
                    val p2 = project(Vec3(g, tableY, gridExtent))
                    drawLine(
                        color = Color(0xFF16223D),
                        start = Offset(p1.x, p1.y),
                        end = Offset(p2.x, p2.y),
                        strokeWidth = 1f
                    )
                    val p3 = project(Vec3(-gridExtent, tableY, g))
                    val p4 = project(Vec3(gridExtent, tableY, g))
                    drawLine(
                        color = Color(0xFF16223D),
                        start = Offset(p3.x, p3.y),
                        end = Offset(p4.x, p4.y),
                        strokeWidth = 1f
                    )
                    g += gridStep
                }

                val polys = mutableListOf<Poly3D>()

                fun addCylinder3D(
                    start: Vec3,
                    end: Vec3,
                    rStart: Float,
                    rEnd: Float,
                    segments: Int,
                    baseColor: Color,
                    edgeColor: Color? = null
                ) {
                    val dx = end.x - start.x
                    val dy = end.y - start.y
                    val dz = end.z - start.z
                    val len = sqrt(dx * dx + dy * dy + dz * dz).coerceAtLeast(0.001f)
                    val ux = dx / len
                    val uy = dy / len
                    val uz = dz / len

                    val (vx, vy, vz) = if (abs(uy) < 0.9f) {
                        val l = sqrt(ux * ux + uz * uz).coerceAtLeast(0.001f)
                        Triple(-uz / l, 0f, ux / l)
                    } else {
                        val l = sqrt(ux * ux + uy * uy).coerceAtLeast(0.001f)
                        Triple(-uy / l, ux / l, 0f)
                    }
                    val wx = uy * vz - uz * vy
                    val wy = uz * vx - ux * vz
                    val wz = ux * vy - uy * vx

                    for (i in 0 until segments) {
                        val a0 = (2.0 * PI * i / segments).toFloat()
                        val a1 = (2.0 * PI * (i + 1) / segments).toFloat()
                        val c0 = cos(a0)
                        val s0 = sin(a0)
                        val c1 = cos(a1)
                        val s1 = sin(a1)

                        val p00 = Vec3(
                            start.x + rStart * (c0 * vx + s0 * wx),
                            start.y + rStart * (c0 * vy + s0 * wy),
                            start.z + rStart * (c0 * vz + s0 * wz)
                        )
                        val p01 = Vec3(
                            start.x + rStart * (c1 * vx + s1 * wx),
                            start.y + rStart * (c1 * vy + s1 * wy),
                            start.z + rStart * (c1 * vz + s1 * wz)
                        )
                        val p11 = Vec3(
                            end.x + rEnd * (c1 * vx + s1 * wx),
                            end.y + rEnd * (c1 * vy + s1 * wy),
                            end.z + rEnd * (c1 * vz + s1 * wz)
                        )
                        val p10 = Vec3(
                            end.x + rEnd * (c0 * vx + s0 * wx),
                            end.y + rEnd * (c0 * vy + s0 * wy),
                            end.z + rEnd * (c0 * vz + s0 * wz)
                        )

                        val shade = (0.68f + 0.32f * ((c0 + c1) * 0.5f)).coerceIn(0.45f, 1.1f)
                        val shadedCol = Color(
                            red = (baseColor.red * shade).coerceIn(0f, 1f),
                            green = (baseColor.green * shade).coerceIn(0f, 1f),
                            blue = (baseColor.blue * shade).coerceIn(0f, 1f),
                            alpha = baseColor.alpha
                        )
                        polys.add(Poly3D(listOf(p00, p01, p11, p10), shadedCol, edgeColor, 0.8f))
                    }
                }

                fun addHorizontalDisc3D(
                    y: Float,
                    rInner: Float,
                    rOuter: Float,
                    segments: Int,
                    fillColor: Color,
                    strokeColor: Color? = null
                ) {
                    for (i in 0 until segments) {
                        val a0 = (2.0 * PI * i / segments).toFloat()
                        val a1 = (2.0 * PI * (i + 1) / segments).toFloat()
                        val p0 = Vec3(rInner * cos(a0), y, rInner * sin(a0))
                        val p1 = Vec3(rOuter * cos(a0), y, rOuter * sin(a0))
                        val p2 = Vec3(rOuter * cos(a1), y, rOuter * sin(a1))
                        val p3 = Vec3(rInner * cos(a1), y, rInner * sin(a1))
                        polys.add(Poly3D(listOf(p0, p1, p2, p3), fillColor, strokeColor, 0.8f))
                    }
                }

                // 2. Spectrometer Base & Tripod Legs (Fig 5.1b: Telescope base & Spectrometer table base)
                addHorizontalDisc3D(
                    y = -70f,
                    rInner = 0f,
                    rOuter = 54f,
                    segments = 20,
                    fillColor = Color(0xFF1E293B),
                    strokeColor = Color(0xFF334155)
                )
                for (legAngle in listOf(0f, 120f, 240f)) {
                    val rad = Math.toRadians(legAngle.toDouble()).toFloat()
                    addCylinder3D(
                        start = Vec3(14f * cos(rad), -64f, 14f * sin(rad)),
                        end = Vec3(68f * cos(rad), -71f, 68f * sin(rad)),
                        rStart = 7f,
                        rEnd = 5f,
                        segments = 8,
                        baseColor = Color(0xFF1E293B),
                        edgeColor = Color(0xFF475569)
                    )
                }
                addCylinder3D(
                    start = Vec3(0f, -70f, 0f),
                    end = Vec3(0f, -22f, 0f),
                    rStart = 18f,
                    rEnd = 14f,
                    segments = 14,
                    baseColor = Color(0xFF334155)
                )
                // Table rotation Lock Screw & Fine Adjust Knob on pillar (Fig 5.1b)
                addCylinder3D(
                    start = Vec3(0f, -42f, 14f),
                    end = Vec3(0f, -42f, 32f),
                    rStart = 3.5f,
                    rEnd = 5.5f,
                    segments = 8,
                    baseColor = SpectralAmber
                )
                // 3D Magnifying Glass on stand for reading Vernier (Fig 5.1b)
                addCylinder3D(
                    start = Vec3(-36f, -71f, 68f),
                    end = Vec3(-36f, -38f, 68f),
                    rStart = 3f,
                    rEnd = 2f,
                    segments = 6,
                    baseColor = Color(0xFF64748B)
                )
                addCylinder3D(
                    start = Vec3(-36f, -38f, 66f),
                    end = Vec3(-36f, -38f, 70f),
                    rStart = 9f,
                    rEnd = 9f,
                    segments = 12,
                    baseColor = LaserCyan.copy(alpha = 0.45f),
                    edgeColor = SpectralAmber
                )

                // 3. Graduated Circular Main Scale & Vernier Scale (y = -22f, r = 64f)
                addHorizontalDisc3D(
                    y = -22f,
                    rInner = 14f,
                    rOuter = 64f,
                    segments = 28,
                    fillColor = Color(0xFF1E293B),
                    strokeColor = Color(0xFF475569)
                )
                addHorizontalDisc3D(
                    y = -21.2f,
                    rInner = 50f,
                    rOuter = 64f,
                    segments = 28,
                    fillColor = Color(0xFF334155),
                    strokeColor = LaserCyan.copy(alpha = 0.45f)
                )

                // 3 Leveling Screws supporting Spectrometer Table
                for (screwAng in listOf(30f, 150f, 270f)) {
                    val rad = Math.toRadians(screwAng.toDouble()).toFloat()
                    addCylinder3D(
                        start = Vec3(24f * cos(rad), -21f, 24f * sin(rad)),
                        end = Vec3(24f * cos(rad), -6f, 24f * sin(rad)),
                        rStart = 3.2f,
                        rEnd = 3.2f,
                        segments = 6,
                        baseColor = SpectralAmber
                    )
                }

                // 4. Spectrometer Table (Prism Table) (y = -6f, r = 34f)
                addHorizontalDisc3D(
                    y = -6f,
                    rInner = 0f,
                    rOuter = 34f,
                    segments = 22,
                    fillColor = Color(0xFF0F172A),
                    strokeColor = LaserCyan.copy(alpha = 0.7f)
                )

                // 5. Diffraction Grating and Mounting Clamp (Fig 5.1b)
                val gratingRotRad = Math.toRadians(
                    (if (uiState.prismTableAngleDeg == 45f || uiState.prismTableAngleDeg == 135f) 0f else (uiState.prismTableAngleDeg - 45f)).toDouble()
                ).toFloat()
                val gCos = cos(gratingRotRad)
                val gSin = sin(gratingRotRad)
                val gHalfW = 22f
                val gBottomY = -5f
                val gTopY = 26f
                val gp0 = Vec3(-gHalfW * gSin, gBottomY, -gHalfW * gCos)
                val gp1 = Vec3(gHalfW * gSin, gBottomY, gHalfW * gCos)
                val gp2 = Vec3(gHalfW * gSin, gTopY, gHalfW * gCos)
                val gp3 = Vec3(-gHalfW * gSin, gTopY, -gHalfW * gCos)
                polys.add(
                    Poly3D(
                        points = listOf(gp0, gp1, gp2, gp3),
                        fillColor = LaserCyan.copy(alpha = 0.28f),
                        strokeColor = LaserCyan,
                        strokeWidth = 2f
                    )
                )
                val gpTop0 = Vec3(-gHalfW * gSin, gTopY, -gHalfW * gCos)
                val gpTop1 = Vec3(gHalfW * gSin, gTopY, gHalfW * gCos)
                val gpTop2 = Vec3(gHalfW * gSin, gTopY + 5f, gHalfW * gCos)
                val gpTop3 = Vec3(-gHalfW * gSin, gTopY + 5f, -gHalfW * gCos)
                polys.add(
                    Poly3D(
                        points = listOf(gpTop0, gpTop1, gpTop2, gpTop3),
                        fillColor = Color(0xFF475569),
                        strokeColor = SpectralAmber,
                        strokeWidth = 1.2f
                    )
                )

                // 6. Collimator Tube + Slit Plate + Slit Width Adjust Screw + Focus Knob (Fig 5.1b)
                val tubeAlpha = if (xRayMode) 0.45f else 0.95f
                addCylinder3D(
                    start = Vec3(-130f, 10f, 0f),
                    end = Vec3(-38f, 10f, 0f),
                    rStart = 9.5f,
                    rEnd = 10.5f,
                    segments = 12,
                    baseColor = Color(0xFF334155).copy(alpha = tubeAlpha),
                    edgeColor = Color(0xFF64748B).copy(alpha = 0.6f)
                )
                // Collimator Focus Knob (rack & pinion side wheel)
                addCylinder3D(
                    start = Vec3(-96f, 10f, 9f),
                    end = Vec3(-96f, 10f, 19f),
                    rStart = 4.5f,
                    rEnd = 5.5f,
                    segments = 8,
                    baseColor = SpectralAmber
                )
                // Slit Width Adjust Screw near Slit Plate (-128f)
                addCylinder3D(
                    start = Vec3(-126f, 10f, 9f),
                    end = Vec3(-126f, 10f, 16f),
                    rStart = 2.8f,
                    rEnd = 3.8f,
                    segments = 8,
                    baseColor = LaserCyan
                )
                addCylinder3D(
                    start = Vec3(-82f, -22f, 0f),
                    end = Vec3(-82f, 2f, 0f),
                    rStart = 4.5f,
                    rEnd = 4.5f,
                    segments = 8,
                    baseColor = Color(0xFF475569)
                )

                // 7. Mercury Vapor Lamp Housing
                val lampCol = if (uiState.lampPowerOn) wavelengthToColor(uiState.wavelengthNm) else Color(0xFF475569)
                addCylinder3D(
                    start = Vec3(-168f, -68f, 0f),
                    end = Vec3(-168f, 34f, 0f),
                    rStart = 18f,
                    rEnd = 16f,
                    segments = 12,
                    baseColor = Color(0xFF1E293B),
                    edgeColor = if (uiState.lampPowerOn) lampCol.copy(alpha = 0.8f) else Color(0xFF334155)
                )

                // 8. Swinging 3D Telescope + Focus Knob + Graticule Lock Ring + Eyepiece (Fig 5.1b)
                val teleRad = Math.toRadians(uiState.telescopeAngleDeg.toDouble()).toFloat()
                val tCos = cos(teleRad)
                val tSin = sin(teleRad)

                val teleStart = Vec3(40f * tCos, 10f, -40f * tSin)
                val teleEnd = Vec3(124f * tCos, 10f, -124f * tSin)
                val graticuleEnd = Vec3(132f * tCos, 10f, -132f * tSin)
                val eyepieceEnd = Vec3(148f * tCos, 10f, -148f * tSin)

                addCylinder3D(
                    start = teleStart,
                    end = teleEnd,
                    rStart = 11f,
                    rEnd = 9.5f,
                    segments = 12,
                    baseColor = Color(0xFF334155).copy(alpha = tubeAlpha),
                    edgeColor = SpectralAmber.copy(alpha = 0.75f)
                )
                // Graticule Lock Ring (Fig 5.1b)
                addCylinder3D(
                    start = teleEnd,
                    end = graticuleEnd,
                    rStart = 8.2f,
                    rEnd = 8.2f,
                    segments = 10,
                    baseColor = Color(0xFF64748B),
                    edgeColor = LaserCyan
                )
                // Eyepiece barrel
                addCylinder3D(
                    start = graticuleEnd,
                    end = eyepieceEnd,
                    rStart = 6.5f,
                    rEnd = 5.5f,
                    segments = 10,
                    baseColor = SpectralAmber.copy(alpha = 0.9f)
                )
                // Telescope Focus Knob sticking out perpendicular to telescope barrel
                val knobAxisCenter = Vec3(98f * tCos, 10f, -98f * tSin)
                val knobTip = Vec3(98f * tCos + 16f * tSin, 10f, -98f * tSin + 16f * tCos)
                addCylinder3D(
                    start = knobAxisCenter,
                    end = knobTip,
                    rStart = 3.5f,
                    rEnd = 5.5f,
                    segments = 8,
                    baseColor = SpectralAmber
                )
                // Telescope support arm & Fine Adjust Knob at Vernier base
                addCylinder3D(
                    start = Vec3(74f * tCos, -21f, -74f * tSin),
                    end = Vec3(74f * tCos, 1f, -74f * tSin),
                    rStart = 5f,
                    rEnd = 5f,
                    segments = 8,
                    baseColor = SpectralAmber.copy(alpha = 0.8f)
                )

                val sortedPolys = polys.map { poly ->
                    val projPts = poly.points.map { project(it) }
                    val avgZ = projPts.sumOf { it.zDepth.toDouble() }.toFloat() / projPts.size
                    Triple(poly, projPts, avgZ)
                }.sortedByDescending { it.third }

                for ((poly, projPts, _) in sortedPolys) {
                    val path = Path().apply {
                        projPts.forEachIndexed { idx, pt ->
                            if (idx == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                        }
                        close()
                    }
                    drawPath(path = path, color = poly.fillColor)
                    if (poly.strokeColor != null) {
                        drawPath(
                            path = path,
                            color = poly.strokeColor,
                            style = Stroke(width = poly.strokeWidth)
                        )
                    }
                }

                // 9. 3D Curved Spectrum Arc & Volumetric Diffracted Beams
                val arcRadius3D = 152f
                if (showCurvedScreenArc) {
                    val arcSteps = 44
                    val arcTopPath = Path()
                    val arcBotPath = Path()
                    for (i in 0..arcSteps) {
                        val deg = -68f + (136f * i / arcSteps)
                        val r = Math.toRadians(deg.toDouble()).toFloat()
                        val ptTop = project(Vec3(arcRadius3D * cos(r), 26f, -arcRadius3D * sin(r)))
                        val ptBot = project(Vec3(arcRadius3D * cos(r), -4f, -arcRadius3D * sin(r)))
                        if (i == 0) {
                            arcTopPath.moveTo(ptTop.x, ptTop.y)
                            arcBotPath.moveTo(ptBot.x, ptBot.y)
                        } else {
                            arcTopPath.lineTo(ptTop.x, ptTop.y)
                            arcBotPath.lineTo(ptBot.x, ptBot.y)
                        }
                    }
                    drawPath(arcTopPath, color = Color(0xFF334155), style = Stroke(width = 1.5f))
                    drawPath(arcBotPath, color = Color(0xFF334155), style = Stroke(width = 1.5f))
                }

                for (s in -4..4) {
                    val offsetZ = s * 3.8f
                    val spBot = project(Vec3(-offsetZ * gSin, gBottomY + 2f, offsetZ * gCos))
                    val spTop = project(Vec3(-offsetZ * gSin, gTopY - 2f, offsetZ * gCos))
                    drawLine(
                        color = Color.White.copy(alpha = 0.35f),
                        start = Offset(spBot.x, spBot.y),
                        end = Offset(spTop.x, spTop.y),
                        strokeWidth = 1f
                    )
                }

                if (uiState.lampPowerOn) {
                    val incidentColor = if (uiState.activeSpectralLines.size == 1) {
                        wavelengthToColor(uiState.wavelengthNm)
                    } else {
                        Color(0xFFE0F2FE)
                    }
                    val pLamp = project(Vec3(-152f, 10f, 0f))
                    val pGratCenter = project(Vec3(0f, 10f, 0f))
                    drawLine(
                        color = incidentColor.copy(alpha = 0.35f),
                        start = Offset(pLamp.x, pLamp.y),
                        end = Offset(pGratCenter.x, pGratCenter.y),
                        strokeWidth = 10f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = incidentColor,
                        start = Offset(pLamp.x, pLamp.y),
                        end = Offset(pGratCenter.x, pGratCenter.y),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )

                    for (peak in uiState.principalPeaks) {
                        if (abs(peak.angleDeg) <= 68f) {
                            val pr = Math.toRadians(peak.angleDeg.toDouble()).toFloat()
                            val hit3D = Vec3(arcRadius3D * cos(pr), 10f, -arcRadius3D * sin(pr))
                            val pHit = project(hit3D)
                            val col = wavelengthToColor(peak.spectralLine.wavelengthNm)
                            val beamAlpha = (0.35f + 0.65f * peak.relativeIntensity).coerceIn(0.28f, 0.95f)

                            drawLine(
                                color = col.copy(alpha = beamAlpha * 0.28f),
                                start = Offset(pGratCenter.x, pGratCenter.y),
                                end = Offset(pHit.x, pHit.y),
                                strokeWidth = if (peak.order == 0) 9f else 6f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = col.copy(alpha = beamAlpha),
                                start = Offset(pGratCenter.x, pGratCenter.y),
                                end = Offset(pHit.x, pHit.y),
                                strokeWidth = if (peak.order == 0) 3.2f else 2.0f,
                                cap = StrokeCap.Round
                            )

                            if (showCurvedScreenArc) {
                                val pTop = project(Vec3(arcRadius3D * cos(pr), 24f, -arcRadius3D * sin(pr)))
                                val pBot = project(Vec3(arcRadius3D * cos(pr), -2f, -arcRadius3D * sin(pr)))
                                drawLine(
                                    color = col.copy(alpha = beamAlpha),
                                    start = Offset(pTop.x, pTop.y),
                                    end = Offset(pBot.x, pBot.y),
                                    strokeWidth = if (peak.order == 0) 4.5f else 3.0f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }

                // 10. Draw Figure 5.1(b) Grating Spectrometer Anatomy Callouts (from Page 6 of PDF)
                val labelStyle = TextStyle(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                fun drawAnatomyCallout(anchor3D: Vec3, labelOffset: Offset, text: String, accent: Color) {
                    val pAnchor = project(anchor3D)
                    val targetX = (pAnchor.x + labelOffset.x).coerceIn(8f, w - 115f)
                    val targetY = (pAnchor.y + labelOffset.y).coerceIn(8f, h - 22f)
                    drawLine(
                        color = accent.copy(alpha = 0.75f),
                        start = Offset(pAnchor.x, pAnchor.y),
                        end = Offset(targetX + 12f, targetY + 6f),
                        strokeWidth = 1.2f
                    )
                    drawCircle(color = accent, radius = 2.8f, center = Offset(pAnchor.x, pAnchor.y))
                    val measured = textMeasurer.measure(text, labelStyle.copy(color = accent))
                    drawText(measured, topLeft = Offset(targetX, targetY))
                }

                if (showFig51bAnatomy) {
                    drawAnatomyCallout(Vec3(-130f, 14f, 0f), Offset(-36f, -26f), "Slit plate & screw", LaserCyan)
                    drawAnatomyCallout(Vec3(-85f, 18f, 0f), Offset(-24f, -32f), "Collimator + Focus", LaserCyan)
                    drawAnatomyCallout(Vec3(0f, 29f, 0f), Offset(-42f, -26f), "Grating & Clamp", SpectralEmerald)
                    drawAnatomyCallout(Vec3(0f, -6f, 28f), Offset(-38f, 12f), "Spectrometer table", SpectralEmerald)
                    drawAnatomyCallout(Vec3(-36f, -38f, 68f), Offset(-45f, 16f), "Magnifying glass", LaserCyan)
                    drawAnatomyCallout(Vec3(0f, -42f, 26f), Offset(14f, 18f), "Table lock & fine knob", SpectralAmber)
                    drawAnatomyCallout(Vec3(88f * tCos, 16f, -88f * tSin), Offset(10f, -26f), "Telescope + Focus knob", SpectralAmber)
                    drawAnatomyCallout(Vec3(142f * tCos, 12f, -142f * tSin), Offset(8f, 14f), "Graticule & Eyepiece", SpectralAmber)
                }

                val pV1 = project(Vec3(62f * tCos, -20f, -62f * tSin))
                val pV2 = project(Vec3(-62f * tCos, -20f, 62f * tSin))
                drawCircle(color = SpectralAmber, radius = 4f, center = Offset(pV1.x, pV1.y))
                drawCircle(color = SpectralEmerald, radius = 4f, center = Offset(pV2.x, pV2.y))
            }

            if (showPipEyepiece) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF03050C).copy(alpha = 0.92f))
                        .border(2.dp, SpectralAmber.copy(alpha = 0.8f), CircleShape)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawMiniEyepieceReticle(uiState)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF070C1A).copy(alpha = 0.86f))
                    .border(1.dp, LabBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "V₁: ${uiState.vernierWindowA.formattedDMS}",
                    style = MaterialTheme.typography.labelMedium,
                    color = SpectralAmber
                )
                Text(
                    text = "V₂: ${uiState.vernierWindowB.formattedDMS}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SpectralEmerald
                )
            }

            // Top-Left Free Camera Telemetry & Quick Reset Pill
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF070C1A).copy(alpha = 0.86f))
                    .border(1.dp, LabBorder, RoundedCornerShape(10.dp))
                    .clickable {
                        autoOrbit = false
                        camYawDeg = -38f
                        camPitchDeg = 26f
                        camRollDeg = 0f
                        camZoom = 1.12f
                        camPanX = 0f
                        camPanY = 0f
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Yaw ${camYawDeg.toInt()}° • Pitch ${camPitchDeg.toInt()}° • Zoom ${"%.1f".format(camZoom)}x",
                    style = MaterialTheme.typography.labelSmall,
                    color = LaserCyan
                )
                Text(
                    text = "↺ Reset 3D",
                    style = MaterialTheme.typography.labelSmall,
                    color = SpectralAmber
                )
            }

            // Bottom-Right Quick On-Screen 3D Pan / Zoom / Orbit Gimbal Buttons
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF070C1A).copy(alpha = 0.88f))
                    .border(1.dp, LabBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    "◀" to { camPanX -= 24f },
                    "▲" to { camPanY -= 20f },
                    "▼" to { camPanY += 20f },
                    "▶" to { camPanX += 24f },
                    "−" to { camZoom = (camZoom - 0.18f).coerceIn(0.45f, 3.20f) },
                    "+" to { camZoom = (camZoom + 0.18f).coerceIn(0.45f, 3.20f) }
                ).forEach { (sym, action) ->
                    Surface(
                        color = LabSurfaceElevated,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { action() }
                    ) {
                        Text(
                            text = sym,
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Triple("Iso 3D (Fig 5.1b)", -38f, 26f),
                Triple("Top Turntable (90°)", 0f, 85f),
                Triple("Bottom Up (-45°)", -30f, -45f),
                Triple("Grating Close-Up", -85f, 14f),
                Triple("Telescope POV", -uiState.telescopeAngleDeg - 12f, 18f),
                Triple("Slit & Collimator", 15f, 22f),
                Triple("Rear Lamp View", 180f, 20f)
            ).forEach { (label, presetYaw, presetPitch) ->
                Surface(
                    color = LabSurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .border(1.dp, LabBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            autoOrbit = false
                            camYawDeg = presetYaw
                            camPitchDeg = presetPitch
                            camPanX = 0f
                            camPanY = 0f
                            if (label == "Grating Close-Up") camZoom = 1.65f else camZoom = 1.12f
                        }
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = LaserCyan,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Direct 3D Camera Sliders (Yaw 360°, Pitch ±88°, Telescope Swing, and Zoom 0.45x..3.2x)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "3D Yaw (${camYawDeg.toInt()}°) & Pitch (${camPitchDeg.toInt()}°)",
                    style = MaterialTheme.typography.labelSmall,
                    color = LaserCyan
                )
                Slider(
                    value = camYawDeg,
                    onValueChange = {
                        autoOrbit = false
                        camYawDeg = it
                    },
                    valueRange = -180f..180f,
                    colors = SliderDefaults.colors(
                        thumbColor = LaserCyan,
                        activeTrackColor = LaserCyan,
                        inactiveTrackColor = LabSurfaceElevated
                    )
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "3D Pitch Angle (${camPitchDeg.toInt()}°)",
                    style = MaterialTheme.typography.labelSmall,
                    color = SpectralEmerald
                )
                Slider(
                    value = camPitchDeg,
                    onValueChange = {
                        autoOrbit = false
                        camPitchDeg = it
                    },
                    valueRange = -88f..88f,
                    colors = SliderDefaults.colors(
                        thumbColor = SpectralEmerald,
                        activeTrackColor = SpectralEmerald,
                        inactiveTrackColor = LabSurfaceElevated
                    )
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Swing 3D Telescope (θ = ${"%.1f".format(uiState.telescopeAngleDeg)}°)",
                    style = MaterialTheme.typography.labelSmall,
                    color = SpectralAmber
                )
                Slider(
                    value = uiState.telescopeAngleDeg,
                    onValueChange = onAngleChange,
                    valueRange = -68f..68f,
                    colors = SliderDefaults.colors(
                        thumbColor = SpectralAmber,
                        activeTrackColor = SpectralAmber,
                        inactiveTrackColor = LabSurfaceElevated
                    )
                )
            }
            Column(modifier = Modifier.weight(0.85f)) {
                Text(
                    text = "3D Zoom (${"%.2f".format(camZoom)}x)",
                    style = MaterialTheme.typography.labelSmall,
                    color = LaserCyan
                )
                Slider(
                    value = camZoom,
                    onValueChange = { camZoom = it },
                    valueRange = 0.45f..3.20f,
                    colors = SliderDefaults.colors(
                        thumbColor = LaserCyan,
                        activeTrackColor = LaserCyan,
                        inactiveTrackColor = LabSurfaceElevated
                    )
                )
            }
        }
    }
}

private fun DrawScope.drawMiniEyepieceReticle(uiState: DiffractionLabUiState) {
    val r = size.minDimension / 2f
    val center = Offset(size.width / 2f, size.height / 2f)
    val fovHalfDeg = 3.2f

    if (uiState.lampPowerOn) {
        val defocusBlurPx = uiState.opticalDefocusAmount * 16f
        val slitWidthPx = (uiState.collimatorSlitOpeningMm * 9f).coerceIn(2f, 16f)

        for (peak in uiState.principalPeaks) {
            val relDeg = peak.angleDeg - uiState.telescopeAngleDeg
            if (abs(relDeg) <= fovHalfDeg) {
                val lineX = center.x + (relDeg / fovHalfDeg) * r
                val col = wavelengthToColor(peak.spectralLine.wavelengthNm)
                drawLine(
                    color = col.copy(alpha = 0.45f),
                    start = Offset(lineX, 0f),
                    end = Offset(lineX, size.height),
                    strokeWidth = slitWidthPx + defocusBlurPx + 6f
                )
                drawLine(
                    color = col,
                    start = Offset(lineX, 0f),
                    end = Offset(lineX, size.height),
                    strokeWidth = slitWidthPx
                )
            }
        }
    }

    when (uiState.crosswireStyle) {
        CrosswireStyle.MULTIPLICATION_X -> {
            drawLine(
                color = SpectralAmber.copy(alpha = 0.85f),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height),
                strokeWidth = 1.3f
            )
            drawLine(
                color = SpectralAmber.copy(alpha = 0.85f),
                start = Offset(size.width, 0f),
                end = Offset(0f, size.height),
                strokeWidth = 1.3f
            )
        }
        else -> {
            drawLine(
                color = SpectralAmber.copy(alpha = 0.85f),
                start = Offset(center.x, 0f),
                end = Offset(center.x, size.height),
                strokeWidth = 1.3f
            )
            drawLine(
                color = SpectralAmber.copy(alpha = 0.6f),
                start = Offset(0f, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = 1.1f
            )
        }
    }
}
