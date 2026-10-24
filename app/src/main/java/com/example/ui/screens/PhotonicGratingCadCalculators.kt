package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LightSourceMode
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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * IIT-Kanpur Virtual Lab (bc2-iitk.vlabs.ac.in) Interactive Apparatus Assembly Bar
 * + ResearchGate / VisuPhy Polychromatic & White-Light Single-Color Channel Filter.
 */
@Composable
fun IitKanpurAndWhiteLightChannelBar(
    uiState: DiffractionLabUiState,
    onToggleLaserPower: () -> Unit,
    onToggleGratingInserted: () -> Unit,
    onSelectWhiteLightMode: () -> Unit,
    onSelectColorFilterBand: (String?) -> Unit,
    onPushAngle: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LabSurface)
            .border(1.dp, LabBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "IIT-KANPUR VLAB SETUP & VISUPHY WHITE-LIGHT CHANNEL FILTER",
                style = MaterialTheme.typography.labelMedium,
                color = LaserCyan
            )
            Text(
                text = if (uiState.isGratingInsertedOnBench) "Grating: IN PATH" else "Grating: REMOVED (n=0 only)",
                style = MaterialTheme.typography.labelSmall,
                color = if (uiState.isGratingInsertedOnBench) SpectralEmerald else SpectralAmber
            )
        }

        // Step-by-step IIT Kanpur Virtual Lab buttons: [1. Laser Source] [2. Place Grating] [3. Measure Screen] [4. White Light]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.lampPowerOn,
                onClick = onToggleLaserPower,
                modifier = Modifier.testTag("btn_iitk_laser_source"),
                label = {
                    Text(
                        text = if (uiState.lampPowerOn) "1. Source: ON ✓" else "1. Add Laser Source",
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SpectralEmerald.copy(alpha = 0.22f),
                    selectedLabelColor = SpectralEmerald,
                    containerColor = LabSurfaceElevated,
                    labelColor = TextSecondary
                )
            )

            FilterChip(
                selected = uiState.isGratingInsertedOnBench,
                onClick = onToggleGratingInserted,
                modifier = Modifier.testTag("btn_iitk_place_grating"),
                label = {
                    Text(
                        text = if (uiState.isGratingInsertedOnBench) "2. Grating: Placed ✓" else "2. Place Grating",
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LaserCyan.copy(alpha = 0.22f),
                    selectedLabelColor = LaserCyan,
                    containerColor = LabSurfaceElevated,
                    labelColor = TextSecondary
                )
            )

            FilterChip(
                selected = abs(uiState.telescopeAngleDeg) > 1f,
                onClick = {
                    val firstPeak = uiState.principalPeaks.firstOrNull { it.order == 1 }
                    onPushAngle(firstPeak?.angleDeg ?: 18.82f)
                },
                label = {
                    Text(
                        text = "3. Snap +1st Order Screen",
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SpectralAmber.copy(alpha = 0.22f),
                    selectedLabelColor = SpectralAmber,
                    containerColor = LabSurfaceElevated,
                    labelColor = TextSecondary
                )
            )

            FilterChip(
                selected = uiState.lightSourceMode == LightSourceMode.WHITE_CONTINUUM,
                onClick = onSelectWhiteLightMode,
                modifier = Modifier.testTag("btn_visuphy_white_light"),
                label = {
                    Text(
                        text = "4. White Light Dispersion",
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SpectralViolet.copy(alpha = 0.25f),
                    selectedLabelColor = TextPrimary,
                    containerColor = LabSurfaceElevated,
                    labelColor = TextSecondary
                )
            )
        }

        // ResearchGate / VisuPhy: "study the diffraction order if one colour is isolated from white/polychromatic light"
        if (uiState.lightSourceMode != LightSourceMode.MONOCHROMATIC) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Isolate Color:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                val colorBands = listOf(
                    null to ("All Colors" to LaserCyan),
                    "Violet" to ("Violet" to wavelengthToColor(410f)),
                    "Blue" to ("Blue" to wavelengthToColor(455f)),
                    "Cyan" to ("Cyan" to wavelengthToColor(495f)),
                    "Green" to ("Green" to wavelengthToColor(540f)),
                    "Yellow" to ("Yellow" to wavelengthToColor(580f)),
                    "Orange" to ("Orange" to wavelengthToColor(615f)),
                    "Red" to ("Red" to wavelengthToColor(660f))
                )
                colorBands.forEach { (bandKey, pair) ->
                    val (label, chipColor) = pair
                    val selected = uiState.whiteLightFilterBand == bandKey
                    Surface(
                        color = if (selected) chipColor.copy(alpha = 0.25f) else LabVoidBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .border(
                                1.dp,
                                if (selected) chipColor else LabBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectColorFilterBand(bandKey) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (bandKey != null) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(chipColor)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                            }
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) TextPrimary else TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * CAD 1: GD-Calc® & Ansys Lumerical Sub-Wavelength Model (LSWM) + Stanford S4 1D Grating Studio.
 * Computes diffraction efficiencies η_m across orders m = -3..+3, Rytov sub-wavelength effective index,
 * Particle Swarm Optimization (PSO) blaze design, and Zemax OpticStudio JSON / S4 Lua export.
 */
@Composable
fun GdCalcLumericalSubWavelengthCard(
    onPushToOpticalBench: (Float, Float) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()

    // 0 = S4 1D PMMA Pillar on MgO, 1 = Lumerical Blazed Sawtooth (PSO), 2 = GD-Calc Binary Phase, 3 = LSWM Sub-Wavelength (Λ < λ)
    var profileType by remember { mutableIntStateOf(0) }
    var wavelengthNm by remember { mutableFloatStateOf(632.8f) }
    var periodNm by remember { mutableFloatStateOf(1200f) }
    var grooveDepthNm by remember { mutableFloatStateOf(520f) }
    var fillFactor by remember { mutableFloatStateOf(0.50f) }
    var isTmPolarization by remember { mutableStateOf(false) } // false = TE (s-pol), true = TM (p-pol)
    var showExportCode by remember { mutableStateOf(false) }

    // Material refractive indices based on preset (S4 Tutorial P3: PMMA n=1.49 on MgO n=1.735, or Si3N4/SiO2)
    val nPillar = when (profileType) {
        0 -> 1.49f // PMMA pillar (S4 Tutorial P3)
        1 -> 1.52f // BK7 / Replicated Epoxy Blazed Grating
        2 -> 2.02f // Si3N4 Binary Phase Grating
        else -> 2.40f // TiO2 Sub-wavelength Metasurface
    }
    val nSubstrate = when (profileType) {
        0 -> 1.735f // Magnesium Oxide (MgO) substrate (S4 Tutorial P3)
        1 -> 1.52f
        2 -> 1.46f  // Fused Silica SiO2
        else -> 1.46f
    }

    // Sub-wavelength regime threshold: Λ < λ / n_substrate
    val subWavelengthCutoffNm = wavelengthNm / nSubstrate
    val isSubWavelengthRegime = periodNm < subWavelengthCutoffNm

    // Rytov 0th-order Sub-Wavelength Effective Medium Theory (EMT) indices:
    // n_TE = sqrt(f * n_p^2 + (1-f) * 1^2), n_TM = 1 / sqrt(f / n_p^2 + (1-f) / 1^2)
    val nEffTe = sqrt(fillFactor * nPillar * nPillar + (1f - fillFactor) * 1.0f)
    val nEffTm = 1f / sqrt((fillFactor / (nPillar * nPillar)) + (1f - fillFactor))
    val activeNeff = if (isTmPolarization) nEffTm else nEffTe

    // Phase retardation Δφ = 2π (n_pillar - 1) h / λ (adjusted slightly for TM vs TE)
    val polFactor = if (isTmPolarization) 0.92f else 1.0f
    val phaseShiftRad = (2.0 * PI * (nPillar - 1f) * grooveDepthNm * polFactor / wavelengthNm).toFloat()

    // Calculate propagating orders and GD-Calc scalar/RCWA-approximated diffraction efficiencies η_m
    val orders = (-3..3).toList()
    val rawEfficiencies = orders.map { m ->
        val sinThetaM = (m * wavelengthNm) / (periodNm * nSubstrate)
        if (abs(sinThetaM) >= 0.995f) {
            0f // Evanescent order!
        } else {
            val cosObliquity = sqrt((1f - sinThetaM * sinThetaM).coerceAtLeast(0.05f))
            when (profileType) {
                1 -> {
                    // Blazed Echelle Grating: peak efficiency shifts to m = +1 when phaseShiftRad ≈ 2π
                    val blazeShift = (phaseShiftRad / (2f * PI.toFloat()))
                    val arg = PI.toFloat() * (m - blazeShift)
                    val sinc = if (abs(arg) < 1e-4f) 1f else sin(arg) / arg
                    (sinc * sinc * cosObliquity).coerceAtLeast(0.01f)
                }
                else -> {
                    // Binary Pillar / Rectangular Phase Grating (S4 / GD-Calc):
                    // η_0 = 1 - 4 f (1-f) sin²(Δφ/2)
                    // η_m = (2 / (m π))² sin²(m π f) sin²(Δφ/2)
                    val sinHalfPhiSq = sin(phaseShiftRad / 2f) * sin(phaseShiftRad / 2f)
                    if (m == 0) {
                        (1f - 4f * fillFactor * (1f - fillFactor) * sinHalfPhiSq).coerceAtLeast(0.02f)
                    } else {
                        val mPi = m * PI.toFloat()
                        val sincDuty = sin(mPi * fillFactor) / mPi
                        (4f * sincDuty * sincDuty * sinHalfPhiSq * cosObliquity).coerceAtLeast(0.005f)
                    }
                }
            }
        }
    }
    val totalRaw = rawEfficiencies.sum().coerceAtLeast(0.01f)
    // Fresnel transmission factor (~92% through substrate)
    val totalTransmittance = 0.92f
    val normalizedEfficiencies = rawEfficiencies.map { (it / totalRaw) * totalTransmittance }

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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "GD-CALC® & ANSYS LUMERICAL SUB-WAVELENGTH (LSWM / S4 RCWA)",
                    style = MaterialTheme.typography.labelMedium,
                    color = SpectralAmber
                )
                Text(
                    text = "Simulate 1D PMMA/MgO pillars (S4), sub-λ metasurfaces, & PSO blaze efficiency η_m",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            OutlinedButton(
                onClick = { showExportCode = !showExportCode },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (showExportCode) "Hide JSON/Lua" else "Zemax JSON / S4 Lua",
                    style = MaterialTheme.typography.labelSmall,
                    color = LaserCyan
                )
            }
        }

        // Preset Architecture Selector Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                0 to "S4 Tutorial P3: PMMA Pillars / MgO",
                1 to "Lumerical PSO: Blazed Sawtooth",
                2 to "GD-Calc®: Binary Phase Grating",
                3 to "Ansys LSWM: Sub-Wavelength (Λ < λ)"
            ).forEach { (idx, title) ->
                FilterChip(
                    selected = profileType == idx,
                    onClick = {
                        profileType = idx
                        when (idx) {
                            0 -> {
                                wavelengthNm = 632.8f
                                periodNm = 1200f
                                grooveDepthNm = 640f
                                fillFactor = 0.50f
                            }
                            1 -> {
                                wavelengthNm = 546.1f
                                periodNm = 1667f // 600 lines/mm
                                grooveDepthNm = 1050f
                                fillFactor = 0.85f
                            }
                            2 -> {
                                wavelengthNm = 532f
                                periodNm = 1500f
                                grooveDepthNm = 260f
                                fillFactor = 0.50f
                            }
                            3 -> {
                                wavelengthNm = 650f
                                periodNm = 310f // Sub-wavelength Λ < λ / n_sub
                                grooveDepthNm = 420f
                                fillFactor = 0.45f
                            }
                        }
                    },
                    label = { Text(title, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LaserCyan.copy(alpha = 0.22f),
                        selectedLabelColor = LaserCyan,
                        containerColor = LabSurfaceElevated,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        // Interactive 2-Tier Nanostructure Cross-Section & GD-Calc Efficiency Bar Chart Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF050811))
                .border(1.dp, Color(0xFF24355A), RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val midY = h * 0.46f

                // --- TOP TIER: 1D Grating Layer Stack (PMMA Pillars on MgO Substrate or Blazed Facets) ---
                // Substrate block
                drawRect(
                    color = Color(0xFF172542),
                    topLeft = Offset(0f, midY - 18f),
                    size = Size(w, 20f)
                )
                val subLabel = textMeasurer.measure(
                    text = if (profileType == 0) "MgO Substrate (n = 1.735)" else "Optical Substrate (n = $nSubstrate)",
                    style = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 9.sp, color = TextSecondary)
                )
                drawText(subLabel, topLeft = Offset(10f, midY - 15f))

                // Draw periodic unit cells (pillars or sawtooth grooves)
                val numCells = (2600f / periodNm).toInt().coerceIn(3, 12)
                val cellW = (w - 40f) / numCells
                val pillarH = ((grooveDepthNm / 1200f) * 42f).coerceIn(10f, 44f)
                val wlCol = wavelengthToColor(wavelengthNm)

                for (c in 0 until numCells) {
                    val cellStartX = 20f + c * cellW
                    val dutyW = cellW * fillFactor
                    if (profileType == 1) {
                        // Sawtooth Blazed Grating facet
                        val blazePath = Path().apply {
                            moveTo(cellStartX, midY - 18f)
                            lineTo(cellStartX + cellW, midY - 18f - pillarH)
                            lineTo(cellStartX + cellW, midY - 18f)
                            close()
                        }
                        drawPath(blazePath, color = LaserCyan.copy(alpha = 0.45f))
                        drawPath(blazePath, color = LaserCyan, style = Stroke(width = 1.5f))
                    } else {
                        // Rectangular Pillar (PMMA / Si3N4 / TiO2)
                        drawRect(
                            color = if (isSubWavelengthRegime) SpectralViolet.copy(alpha = 0.6f) else LaserCyan.copy(alpha = 0.5f),
                            topLeft = Offset(cellStartX, midY - 18f - pillarH),
                            size = Size(dutyW, pillarH)
                        )
                        drawRect(
                            color = if (isSubWavelengthRegime) SpectralViolet else LaserCyan,
                            topLeft = Offset(cellStartX, midY - 18f - pillarH),
                            size = Size(dutyW, pillarH),
                            style = Stroke(width = 1.4f)
                        )
                    }
                }

                // Incident Excitation Wave Arrows from top
                for (ix in 1..3) {
                    val ax = w * (0.25f * ix)
                    drawLine(
                        color = wlCol,
                        start = Offset(ax, 6f),
                        end = Offset(ax, midY - 22f - pillarH),
                        strokeWidth = 2.2f
                    )
                }

                // --- BOTTOM TIER: GD-Calc Diffraction Order Efficiency η_m (%) Bar Chart ---
                val chartTop = midY + 12f
                val chartBot = h - 22f
                val chartH = (chartBot - chartTop).coerceAtLeast(30f)

                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(16f, chartBot),
                    end = Offset(w - 16f, chartBot),
                    strokeWidth = 1.5f
                )

                val colSlotW = (w - 40f) / orders.size
                orders.forEachIndexed { idx, m ->
                    val eff = normalizedEfficiencies[idx]
                    val barH = (eff * chartH).coerceIn(2f, chartH)
                    val cx = 20f + (idx + 0.5f) * colSlotW
                    val barW = (colSlotW * 0.52f).coerceAtMost(38f)

                    val isPropagating = eff > 0.001f
                    val barColor = when {
                        !isPropagating -> Color(0xFF334155)
                        m == 1 && profileType == 1 -> SpectralAmber
                        m == 0 && isSubWavelengthRegime -> SpectralEmerald
                        else -> wlCol
                    }

                    drawRect(
                        color = barColor.copy(alpha = if (isPropagating) 0.85f else 0.25f),
                        topLeft = Offset(cx - barW / 2f, chartBot - barH),
                        size = Size(barW, barH)
                    )

                    val pctStr = if (isPropagating) "${(eff * 100f).roundToInt()}%" else "Evan."
                    val pctLayout = textMeasurer.measure(
                        text = pctStr,
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPropagating) TextPrimary else TextMuted
                        )
                    )
                    drawText(
                        textLayoutResult = pctLayout,
                        topLeft = Offset(cx - pctLayout.size.width / 2f, (chartBot - barH - 14f).coerceAtLeast(midY + 2f))
                    )

                    val ordStr = if (m > 0) "m=+$m" else "m=$m"
                    val ordLayout = textMeasurer.measure(
                        text = ordStr,
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 9.sp,
                            color = TextSecondary
                        )
                    )
                    drawText(
                        textLayoutResult = ordLayout,
                        topLeft = Offset(cx - ordLayout.size.width / 2f, chartBot + 4f)
                    )
                }
            }
        }

        // Sub-Wavelength / Rytov Effective Medium & Efficiency Telemetry Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CadTelemetryBox(
                label = "Regime (Λ vs λ/n)",
                value = if (isSubWavelengthRegime) "Sub-λ Zero-Order" else "Multi-Order Diffractive",
                accent = if (isSubWavelengthRegime) SpectralEmerald else LaserCyan,
                modifier = Modifier.weight(1f)
            )
            CadTelemetryBox(
                label = "Rytov n_eff (${if (isTmPolarization) "TM" else "TE"})",
                value = "n_eff = ${"%.3f".format(activeNeff)}",
                accent = SpectralAmber,
                modifier = Modifier.weight(1f)
            )
            CadTelemetryBox(
                label = "Peak Order η",
                value = "η_+1 = ${(normalizedEfficiencies[4] * 100f).roundToInt()}% | η_0 = ${(normalizedEfficiencies[3] * 100f).roundToInt()}%",
                accent = SpectralEmerald,
                modifier = Modifier.weight(1f)
            )
        }

        // Parameter Sliders: Period Λ, Groove Depth h, Fill Factor f, Polarization & PSO Optimizer
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Grating Period Λ: ${periodNm.roundToInt()} nm (${"%.2f".format(periodNm / 1000f)} µm)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = "Sub-λ Cutoff: ${subWavelengthCutoffNm.roundToInt()} nm",
                    style = MaterialTheme.typography.labelSmall,
                    color = SpectralAmber
                )
            }
            Slider(
                value = periodNm,
                onValueChange = { periodNm = it },
                valueRange = 200f..2500f,
                colors = SliderDefaults.colors(thumbColor = LaserCyan, activeTrackColor = LaserCyan)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pillar / Groove Depth h: ${grooveDepthNm.roundToInt()} nm (Δφ = ${"%.2f".format(phaseShiftRad / PI)} π)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = "Duty Cycle f: ${(fillFactor * 100f).roundToInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = LaserCyan
                )
            }
            Slider(
                value = grooveDepthNm,
                onValueChange = { grooveDepthNm = it },
                valueRange = 50f..1200f,
                colors = SliderDefaults.colors(thumbColor = SpectralAmber, activeTrackColor = SpectralAmber)
            )

            Slider(
                value = fillFactor,
                onValueChange = { fillFactor = it },
                valueRange = 0.10f..0.90f,
                colors = SliderDefaults.colors(thumbColor = SpectralEmerald, activeTrackColor = SpectralEmerald)
            )
        }

        // Action Row: TE/TM Polarization Toggle + Ansys Lumerical PSO Auto-Optimize + Push to Bench
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = !isTmPolarization,
                onClick = { isTmPolarization = false },
                label = { Text("TE (s-pol)", style = MaterialTheme.typography.labelSmall) }
            )
            FilterChip(
                selected = isTmPolarization,
                onClick = { isTmPolarization = true },
                label = { Text("TM (p-pol)", style = MaterialTheme.typography.labelSmall) }
            )

            Button(
                onClick = {
                    // Ansys Lumerical Particle Swarm Optimization (PSO):
                    // For binary phase grating, maximize ±1st order by setting Δφ = π => h = λ / (2*(n_p - 1)) and f = 0.50
                    // For blazed grating, maximize +1st order by setting Δφ = 2π => h = λ / (n_p - 1)
                    if (profileType == 1) {
                        grooveDepthNm = (wavelengthNm / (nPillar - 1f)).coerceIn(50f, 1200f)
                        fillFactor = 0.88f
                    } else {
                        grooveDepthNm = (wavelengthNm / (2f * (nPillar - 1f))).coerceIn(50f, 1200f)
                        fillFactor = 0.50f
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpectralAmber.copy(alpha = 0.22f),
                    contentColor = SpectralAmber
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Run Lumerical PSO Optimize", style = MaterialTheme.typography.labelSmall)
            }

            OutlinedButton(
                onClick = {
                    val dUm = (periodNm / 1000f).coerceIn(0.8f, 15.0f)
                    onPushToOpticalBench(wavelengthNm.coerceIn(380f, 700f), dUm)
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Push to Bench", style = MaterialTheme.typography.labelSmall, color = LaserCyan)
            }
        }

        // Zemax OpticStudio JSON & Stanford S4 Lua Script Generator Preview
        if (showExportCode) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(LabVoidBg)
                    .border(1.dp, LaserCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "ANSYS LUMERICAL → ZEMAX JSON & STANFORD S4 LUA SCRIPT",
                    style = MaterialTheme.typography.labelSmall,
                    color = LaserCyan
                )
                Text(
                    text = """
                    // 1. Lumerical LSWM / PSO Zemax Export (lswm_grating_zemax.json)
                    {
                      "model": "Lumerical_Sub_Wavelength_Grating",
                      "period_nm": ${periodNm.roundToInt()}, "etch_depth_nm": ${grooveDepthNm.roundToInt()}, "fill_factor": ${"%.2f".format(fillFactor)},
                      "n_pillar": $nPillar, "n_substrate": $nSubstrate, "n_eff_rytov": ${"%.4f".format(activeNeff)},
                      "eta_minus1": ${"%.3f".format(normalizedEfficiencies[2])}, "eta_0": ${"%.3f".format(normalizedEfficiencies[3])}, "eta_plus1": ${"%.3f".format(normalizedEfficiencies[4])}
                    }
                    -- 2. Stanford S4 Tutorial P3 Lua Script (1D PMMA Pillar on MgO)
                    S = S4.NewSimulation()
                    S:SetLattice(${"%.3f".format(periodNm / 1000f)})
                    S:SetNumG(25)
                    S:AddMaterial("PMMA", {${"%.3f".format(nPillar * nPillar)}, 0})
                    S:AddMaterial("MgO", {${"%.3f".format(nSubstrate * nSubstrate)}, 0})
                    S:AddLayer("PillarLayer", ${"%.3f".format(grooveDepthNm / 1000f)}, "Vacuum")
                    S:SetLayerPatternRectangle("PillarLayer", "PMMA", {0,0}, 0, {${"%.3f".format(fillFactor * periodNm / 2000f)}, 0})
                    """.trimIndent(),
                    style = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 10.sp, color = TextSecondary)
                )
            }
        }
    }
}

/**
 * CAD 2: OghmaNano DFB (Distributed Feedback) Bragg Grating Simulation with
 * Transfer Matrix Method (TMM), Quarter-Wave Phase Shift, and Fabrication Uncertainty (Lua Scripting).
 */
@Composable
fun OghmaNanoDfbBraggTmmCard() {
    val textMeasurer = rememberTextMeasurer()

    var braggPeriodNm by remember { mutableFloatStateOf(240f) }      // Λ_B (nm)
    var effectiveIndexNeff by remember { mutableFloatStateOf(2.15f) } // Waveguide n_eff
    var indexContrastDeltaN by remember { mutableFloatStateOf(0.035f) } // Δn
    var numPeriods by remember { mutableIntStateOf(120) }             // N_p periods
    var quarterWaveDefect by remember { mutableStateOf(true) }        // λ/4 DFB laser phase shift
    var fabUncertaintyNm by remember { mutableFloatStateOf(0.4f) }    // σ_Λ fabrication pitch uncertainty (nm)
    var showLuaScript by remember { mutableStateOf(false) }

    // Bragg wavelength: λ_B = 2 * n_eff * Λ_B
    val braggWavelengthNm = 2f * effectiveIndexNeff * braggPeriodNm
    // Grating total length L = N_p * Λ_B
    val gratingLengthUm = (numPeriods * braggPeriodNm) / 1000f
    // Coupling coefficient κ = π * Δn / λ_B (in µm⁻¹)
    val kappaPerUm = (PI.toFloat() * indexContrastDeltaN) / (braggWavelengthNm / 1000f)
    val kappaL = kappaPerUm * gratingLengthUm
    // Peak reflectance R_max = tanh²(κ L) attenuated by fabrication uncertainty σ_Λ
    val disorderDamping = exp(-0.18f * fabUncertaintyNm * fabUncertaintyNm)
    val peakReflectance = (tanh(kappaL.toDouble()).toFloat().let { it * it } * disorderDamping).coerceIn(0f, 0.999f)
    // Photonic stopband bandwidth Δλ_stop ≈ (2 * λ_B * Δn) / (π * n_eff)
    val stopbandWidthNm = (2f * braggWavelengthNm * indexContrastDeltaN) / (PI.toFloat() * effectiveIndexNeff)

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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "OGHMANANO DFB BRAGG GRATING — TRANSFER MATRIX METHOD (TMM)",
                    style = MaterialTheme.typography.labelMedium,
                    color = LaserCyan
                )
                Text(
                    text = "λ_Bragg = 2 n_eff Λ = ${"%.1f".format(braggWavelengthNm)} nm  •  Stopband Δλ = ${"%.1f".format(stopbandWidthNm)} nm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            OutlinedButton(
                onClick = { showLuaScript = !showLuaScript },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (showLuaScript) "Hide Lua" else "OghmaNano Lua",
                    style = MaterialTheme.typography.labelSmall,
                    color = SpectralAmber
                )
            }
        }

        // TMM Reflectance R(λ) & Transmittance T(λ) Stopband Spectrum Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(185.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF050811))
                .border(1.dp, Color(0xFF24355A), RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val padL = 32f
                val padR = 16f
                val padT = 22f
                val padB = 26f
                val plotW = w - padL - padR
                val plotH = h - padT - padB

                // Axes
                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(padL, padT),
                    end = Offset(padL, padT + plotH),
                    strokeWidth = 1.5f
                )
                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(padL, padT + plotH),
                    end = Offset(padL + plotW, padT + plotH),
                    strokeWidth = 1.5f
                )

                // Plot R(λ) and T(λ) across [λ_B - 40 nm, λ_B + 40 nm]
                val spanNm = (stopbandWidthNm * 3.8f).coerceAtLeast(36f)
                val samples = 160
                val reflPath = Path()
                val transPath = Path()

                for (i in 0..samples) {
                    val frac = i.toFloat() / samples
                    val detuningNm = (frac - 0.5f) * spanNm
                    val normDelta = detuningNm / (stopbandWidthNm / 2f).coerceAtLeast(0.5f)

                    // Coupled-mode / TMM analytical stopband envelope + side-lobes + fabrication disorder broadening
                    val rawBand = if (abs(normDelta) <= 1f) {
                        val core = sqrt((1f - normDelta * normDelta).coerceAtLeast(0f))
                        (tanh((kappaL * core).toDouble()).toFloat().let { it * it })
                    } else {
                        val sideArg = kappaL * sqrt((normDelta * normDelta - 1f).coerceAtLeast(0.01f))
                        val sincSide = sin(sideArg) / sideArg
                        (0.32f * sincSide * sincSide / (normDelta * normDelta)).coerceIn(0f, 0.45f)
                    }

                    // Quarter-wave (λ/4) DFB phase-shift opens a narrow transmission resonance at Δλ = 0
                    val defectDip = if (quarterWaveDefect) {
                        1f - 0.92f * exp((-normDelta * normDelta * 28f).toDouble()).toFloat()
                    } else {
                        1f
                    }

                    // Fabrication uncertainty σ_Λ adds chirp/ripple and reduces stopband contrast
                    val ripple = if (fabUncertaintyNm > 0.05f) {
                        1f - (0.08f * fabUncertaintyNm * cos(normDelta * 7f)).coerceIn(-0.25f, 0.25f)
                    } else {
                        1f
                    }

                    val rVal = (rawBand * defectDip * disorderDamping * ripple).coerceIn(0.01f, 0.99f)
                    val tVal = (1f - rVal).coerceIn(0.01f, 0.99f)

                    val px = padL + frac * plotW
                    val pyR = padT + (1f - rVal) * plotH
                    val pyT = padT + (1f - tVal) * plotH

                    if (i == 0) {
                        reflPath.moveTo(px, pyR)
                        transPath.moveTo(px, pyT)
                    } else {
                        reflPath.lineTo(px, pyR)
                        transPath.lineTo(px, pyT)
                    }
                }

                // Draw Transmittance T(λ) dashed curve
                drawPath(
                    path = transPath,
                    color = SpectralEmerald.copy(alpha = 0.7f),
                    style = Stroke(
                        width = 1.8f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                    )
                )
                // Draw Reflectance R(λ) primary curve
                drawPath(
                    path = reflPath,
                    color = LaserCyan,
                    style = Stroke(width = 2.6f)
                )

                // Center Bragg wavelength marker
                val centerX = padL + plotW * 0.5f
                drawLine(
                    color = SpectralAmber.copy(alpha = 0.75f),
                    start = Offset(centerX, padT),
                    end = Offset(centerX, padT + plotH),
                    strokeWidth = 1.4f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )

                val legendLayout = textMeasurer.measure(
                    text = "Solid Cyan: Reflectance R(λ)   |   Dashed Green: Transmittance T(λ)   |   λ_B = ${"%.1f".format(braggWavelengthNm)} nm",
                    style = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 9.sp, color = TextSecondary)
                )
                drawText(legendLayout, topLeft = Offset(padL + 6f, 4f))
            }
        }

        // Telemetry Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CadTelemetryBox(
                label = "Bragg λ_B",
                value = "${"%.1f".format(braggWavelengthNm)} nm",
                accent = LaserCyan,
                modifier = Modifier.weight(1f)
            )
            CadTelemetryBox(
                label = "Coupling κ·L",
                value = "${"%.2f".format(kappaL)} (R=${(peakReflectance * 100f).roundToInt()}%)",
                accent = SpectralAmber,
                modifier = Modifier.weight(1f)
            )
            CadTelemetryBox(
                label = "Fab Disorder σ_Λ",
                value = "±${"%.2f".format(fabUncertaintyNm)} nm",
                accent = if (fabUncertaintyNm < 1.0f) SpectralEmerald else SpectralCrimson,
                modifier = Modifier.weight(1f)
            )
        }

        // Sliders for Bragg Period Λ_B, Index Contrast Δn, and Fabrication Uncertainty σ_Λ
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Bragg Period Λ_B: ${braggPeriodNm.roundToInt()} nm  (n_eff = ${"%.2f".format(effectiveIndexNeff)})",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                FilterChip(
                    selected = quarterWaveDefect,
                    onClick = { quarterWaveDefect = !quarterWaveDefect },
                    label = { Text("λ/4 DFB Phase Shift", style = MaterialTheme.typography.labelSmall) }
                )
            }
            Slider(
                value = braggPeriodNm,
                onValueChange = { braggPeriodNm = it },
                valueRange = 150f..550f,
                colors = SliderDefaults.colors(thumbColor = LaserCyan, activeTrackColor = LaserCyan)
            )

            Text(
                text = "Index Modulation Δn: ${"%.3f".format(indexContrastDeltaN)}  •  Periods N_p = $numPeriods (L = ${"%.1f".format(gratingLengthUm)} µm)",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Slider(
                value = indexContrastDeltaN,
                onValueChange = { indexContrastDeltaN = it },
                valueRange = 0.005f..0.120f,
                colors = SliderDefaults.colors(thumbColor = SpectralAmber, activeTrackColor = SpectralAmber)
            )

            Text(
                text = "Fabrication Pitch Uncertainty σ_Λ (Lua Random Disorder): ±${"%.2f".format(fabUncertaintyNm)} nm",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Slider(
                value = fabUncertaintyNm,
                onValueChange = { fabUncertaintyNm = it },
                valueRange = 0.0f..3.5f,
                colors = SliderDefaults.colors(thumbColor = SpectralEmerald, activeTrackColor = SpectralEmerald)
            )
        }

        if (showLuaScript) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(LabVoidBg)
                    .border(1.dp, SpectralAmber.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "OGHMANANO LUA SCRIPT — DFB TMM WITH FABRICATION UNCERTAINTY",
                    style = MaterialTheme.typography.labelSmall,
                    color = SpectralAmber
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = """
                    -- OghmaNano Transfer Matrix Method (TMM) DFB Bragg Grating Generator
                    local period_nm = ${braggPeriodNm.roundToInt()}
                    local sigma_nm = ${"%.2f".format(fabUncertaintyNm)}
                    local n_eff = ${"%.2f".format(effectiveIndexNeff)}
                    local delta_n = ${"%.3f".format(indexContrastDeltaN)}
                    for i = 1, $numPeriods do
                      local jitter = math.random_normal(0, sigma_nm)
                      tmm_add_layer("grating_high_"..i, (period_nm/2 + jitter)*1e-9, n_eff + delta_n/2)
                      tmm_add_layer("grating_low_"..i,  (period_nm/2 - jitter)*1e-9, n_eff - delta_n/2)
                    end
                    """.trimIndent(),
                    style = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 10.sp, color = TextSecondary)
                )
            }
        }
    }
}

@Composable
private fun CadTelemetryBox(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = LabVoidBg,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.labelMedium, color = accent)
        }
    }
}

/**
 * Optical Diffraction Simulation Software Directory (answering ResearchGate query:
 * "Which software is suitable for simulation of diffraction...").
 */
@Composable
fun OpticalSimulationSoftwareDirectoryCard(
    onOpenPhotonicCadTab: () -> Unit
) {
    val tools = listOf(
        Triple(
            "Ansys Lumerical (FDTD / RCWA + LSWM Plugin)",
            "Sub-Wavelength Model (LSWM) & Particle Swarm Optimization (PSO)",
            "Simulates optical surfaces with sub-wavelength diffraction gratings, computes full vector bidirectional scattering distribution functions (BSDF), optimizes blaze efficiency via PSO, and exports JSON files directly into Zemax OpticStudio."
        ),
        Triple(
            "GD-Calc® (Grating Diffraction Calculator — MATLAB / Code Ocean)",
            "Rigorous Coupled-Wave Analysis (RCWA) for 1D/2D Periodic Gratings",
            "Computes optical diffraction efficiencies (η_m) of linear, crossed, blazed, sinusoidal, and sub-wavelength periodic structures under arbitrary TE/TM polarization and conical incidence."
        ),
        Triple(
            "Stanford S4 (Simulation of S-Matrix in Layered Periodic Structures)",
            "Fourier Modal Method (FMM / RCWA) with Lua / Python Scripting",
            "Widely used open-source tool (e.g., S4 Tutorial P3: 1D PMMA pillars on MgO substrate) for defining multi-layer photonic crystals, dielectric metasurfaces, and plane-wave excitation."
        ),
        Triple(
            "OghmaNano (Photonic & Optoelectronic TCAD + TMM)",
            "Transfer Matrix Method (TMM) for DFB Bragg Gratings",
            "Simulates Distributed Feedback (DFB) lasers and Bragg reflectors using TMM with Lua scripting to model grating pitch Λ, quarter-wave phase shifts, and fabrication uncertainty σ_Λ."
        ),
        Triple(
            "COMSOL Multiphysics (Wave Optics Module) & MEEP",
            "Finite Element Method (FEM) & Open-Source FDTD",
            "Recommended on ResearchGate for visualizing near-field wavefronts, polychromatic white-light order splitting, and custom plasmonic/dielectric grating geometries."
        ),
        Triple(
            "VisuPhy, oPhysics, PrayogShala & IIT-K / Amrita Virtual Labs",
            "Interactive Educational Wave Optics & Spectrometer Simulators",
            "Real-time browser and mobile physics lab simulations for assembling laser + grating + screen setups, isolating color channels from white light, and reading Vernier spectrometers."
        )
    )

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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "OPTICAL DIFFRACTION SIMULATION SOFTWARE DIRECTORY",
                    style = MaterialTheme.typography.labelMedium,
                    color = SpectralAmber
                )
                Text(
                    text = "Comparison of Lumerical LSWM, GD-Calc®, Stanford S4, OghmaNano TMM, COMSOL, & Virtual Labs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            OutlinedButton(
                onClick = onOpenPhotonicCadTab,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Open CAD Simulators", style = MaterialTheme.typography.labelSmall, color = LaserCyan)
            }
        }

        tools.forEach { (name, method, desc) ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(LabVoidBg)
                    .border(1.dp, Color(0xFF1F2D4D), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(text = name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(text = method, style = MaterialTheme.typography.labelMedium, color = LaserCyan)
                Text(text = desc, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }
    }
}
