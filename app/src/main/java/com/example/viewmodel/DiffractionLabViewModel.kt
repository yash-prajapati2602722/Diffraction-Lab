package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.model.BenchExperimentMode
import com.example.model.BenchVisualStyle
import com.example.model.CrosswireStyle
import com.example.model.DiffractionPeak
import com.example.model.DiffractionPhysics
import com.example.model.LabTab
import com.example.model.LightSourceMode
import com.example.model.ObservationRecord
import com.example.model.PH3001Module
import com.example.model.SpectralLine
import com.example.model.VernierReading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class DiffractionLabUiState(
    val selectedTab: LabTab = LabTab.OPTICAL_BENCH,
    val benchMode: BenchExperimentMode = BenchExperimentMode.DIFFRACTION_GRATING,
    val benchVisualStyle: BenchVisualStyle = BenchVisualStyle.RAY_BENCH_2D,
    val lightSourceMode: LightSourceMode = LightSourceMode.MERCURY_VAPOR,
    val lampPowerOn: Boolean = true,
    val wavelengthNm: Float = 546.1f,          // Default Hg Green (380 - 700 nm)
    val slitSpacingUm: Float = 1.693f,         // Default 15,000 LPI grating (0.8 - 10.0 µm)
    val slitWidthUm: Float = 0.45f,            // Single slit width a (0.15 - 3.0 µm)
    val numSlits: Int = 24,                    // Active slits M (1 - 60)
    val screenDistanceM: Float = 1.0f,         // Distance L or D to screen (0.3 - 2.0 m)
    val telescopeAngleDeg: Float = 18.82f,     // Current telescope angle (-75° to +75°)
    val showEnvelope: Boolean = true,
    val animateWaves: Boolean = true,

    // PrayogShala Spectrometer Web Simulation Options
    val crosswireStyle: CrosswireStyle = CrosswireStyle.MULTIPLICATION_X,
    val collimatorSlitOpeningMm: Float = 0.25f, // 0.10 mm (razor sharp) to 1.80 mm (wide)
    val telescopeFocusPercent: Float = 50f,     // 50% = optimal Schuster parallel-ray focus
    val prismTableAngleDeg: Float = 45f,        // 0°, 45°, 90°, 135° normal alignment
    val isPrismTableLeveled: Boolean = true,
    val vernierDivisions: Int = 30,             // 30 VSD (LC = 1') or 60 VSD (LC = 30")
    val lockedLeftAngleDeg: Float? = -18.82f,   // Captured Left reading (a)
    val lockedRightAngleDeg: Float? = 18.82f,   // Captured Right reading (b)

    // Exp 1: Thin Wire Laser Diffraction Options (He-Ne 6328 Å)
    val wirePresetIndex: Int = 0,               // 0 = Wire 1 (0.12 mm), 1 = Wire 2 (0.18 mm), 2 = Custom
    val wireThicknessMm: Float = 0.12f,         // 0.05 mm to 0.35 mm

    // Companion PH3001(P) Virtual Labs & Photonic CAD State
    val selectedPh3001Module: PH3001Module = PH3001Module.GD_CALC_LUMERICAL,
    val isGratingInsertedOnBench: Boolean = true, // IIT-Kanpur bc2-iitk.vlabs.ac.in grating placement toggle
    val whiteLightFilterBand: String? = null,     // ResearchGate / VisuPhy single-color filter in white/polychromatic light
    val newtonLensRadiusMm: Float = 550f,       // Manual page 50: R = 550 mm
    val newtonMediumRefractiveIndex: Float = 1.0f, // 1.00 Air, 1.33 Water, 1.50 Cedar Oil
    val newtonMicroscopePosMm: Float = 2.65f,   // Traveling microscope position
    val fiberCoreIndex: Float = 1.48f,          // n1
    val fiberCladdingIndex: Float = 1.41f,      // n2
    val fiberDetectorDistanceMm: Float = 20f,   // r (mm)
    val fiberDetectorOffsetMm: Float = 4.5f,    // x (mm)
    val planckSelectedLedIndex: Int = 0,        // 0=Red(6600Å), 1=Yellow(6100Å), 2=Green(5250Å), 3=Blue(4700Å)
    val planckBiasVoltageV: Float = 1.85f,      // 0.0 to 3.2 V
    val screwGaugePitchScaleMm: Int = 4,
    val screwGaugeHeadDiv: Int = 56,
    val screwGaugeZeroErrorDiv: Int = -3,       // Manual page 9 example: ZE = -3 div, ZC = +0.03 mm
    val manualPdfPageIndex: Int = 54,           // Default open to Page 55 (Diffraction Grating)

    val observations: List<ObservationRecord> = emptyList(),
    val selectedQuizAnswers: Map<Int, Int> = emptyMap(),
    val completedProcedureSteps: Set<Int> = setOf(1, 2),
    val statusBannerMessage: String? = null
) {
    val linesPerInch: Int
        get() = DiffractionPhysics.slitSpacingUmToLpi(slitSpacingUm)

    val linesPerMm: Float
        get() = DiffractionPhysics.slitSpacingUmToLinesPerMm(slitSpacingUm)

    val gratingElementCm: Float
        get() = 2.54f / linesPerInch.coerceAtLeast(1)

    val activeSpectralLines: List<SpectralLine>
        get() {
            val rawLines = when (benchMode) {
                BenchExperimentMode.DIFFRACTION_GRATING ->
                    DiffractionPhysics.activeLinesForMode(lightSourceMode, wavelengthNm)
                BenchExperimentMode.SINGLE_SLIT_HEISENBERG ->
                    listOf(SpectralLine("Diode Laser (${wavelengthNm.roundToInt()} nm)", wavelengthNm, 1.0f))
                BenchExperimentMode.WIRE_THICKNESS_LASER ->
                    listOf(SpectralLine("He-Ne Laser (${(wavelengthNm * 10).roundToInt()} Å)", wavelengthNm, 1.0f))
            }
            val filter = whiteLightFilterBand
            if (filter == null || rawLines.size <= 1) return rawLines
            val filtered = rawLines.filter { it.name.contains(filter, ignoreCase = true) }
            return filtered.ifEmpty { rawLines }
        }

    val principalPeaks: List<DiffractionPeak>
        get() {
            val allPeaks = DiffractionPhysics.computePrincipalPeaks(
                lines = activeSpectralLines,
                slitSpacingUm = slitSpacingUm,
                slitWidthUm = slitWidthUm,
                screenDistanceM = screenDistanceM
            )
            return if (isGratingInsertedOnBench) allPeaks else allPeaks.filter { it.order == 0 }
        }

    val maxObservableOrder: Int
        get() = floor((slitSpacingUm * 1000f * 0.98f) / wavelengthNm).toInt().coerceAtLeast(0)

    val vernierWindowA: VernierReading
        get() = DiffractionPhysics.toVernierReading(telescopeAngleDeg, vernierDivisions)

    val vernierWindowB: VernierReading
        get() = DiffractionPhysics.toVernierReading(telescopeAngleDeg + 180f, vernierDivisions)

    // Defocus factor [0 = sharp at 50%, 1 = max blur at 0% or 100%]
    val opticalDefocusAmount: Float
        get() = (abs(telescopeFocusPercent - 50f) / 50f).coerceIn(0f, 1f)

    val currentDetectorIntensity: Float
        get() {
            if (!lampPowerOn) return 0f
            val angleRad = Math.toRadians(telescopeAngleDeg.toDouble()).toFloat()
            val effectiveSlits = if (benchMode == BenchExperimentMode.DIFFRACTION_GRATING) numSlits else 1
            return activeSpectralLines.maxOfOrNull { line ->
                line.relativeIntensity * DiffractionPhysics.calculateIntensityAtAngle(
                    angleRad = angleRad,
                    wavelengthNm = line.wavelengthNm,
                    slitSpacingUm = slitSpacingUm,
                    slitWidthUm = slitWidthUm,
                    numSlits = effectiveSlits,
                    includeEnvelope = showEnvelope || benchMode != BenchExperimentMode.DIFFRACTION_GRATING
                )
            } ?: 0f
        }

    val nearestPeak: DiffractionPeak?
        get() = principalPeaks.minByOrNull { abs(it.angleDeg - telescopeAngleDeg) }

    val resolvingPowerFirstOrder: Int
        get() = numSlits * 1

    // Exp 1: Wire diffraction central maximum half-width b = D * λ / d (in mm)
    val wireCentralMaxHalfWidthMm: Float
        get() {
            val dMm = wireThicknessMm.coerceAtLeast(0.02f)
            val screenDMm = screenDistanceM * 1000f
            val lambdaMm = wavelengthNm * 1e-6f
            return (screenDMm * lambdaMm) / dMm
        }

    // Exp 9: Heisenberg Uncertainty first minimum angle sin(θ₁) = λ / a
    val heisenbergFirstMinAngleDeg: Float
        get() {
            val sinT = (wavelengthNm / (slitWidthUm * 1000f)).coerceIn(0.001f, 0.99f)
            return Math.toDegrees(asin(sinT.toDouble())).toFloat()
        }

    val heisenbergFirstMinScreenYmm: Float
        get() {
            val rad = Math.toRadians(heisenbergFirstMinAngleDeg.toDouble())
            return (screenDistanceM * 1000.0 * kotlin.math.tan(rad)).toFloat()
        }
}

class DiffractionLabViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DiffractionLabUiState())
    val uiState: StateFlow<DiffractionLabUiState> = _uiState.asStateFlow()

    private var nextRecordId = 1

    init {
        seedInitialObservations()
    }

    private fun seedInitialObservations() {
        // Seed authentic Medicaps Manual Page 60–61 observations (N = 15,000 LPI, Violet 4046 Å, Green 5461 Å, Red 6800 Å)
        val defaultD = 1.6933f // 25400 / 15000 µm
        val sampleLines = listOf(
            Triple("Violet (Std 4046 Å)", 404.6f, 1),
            Triple("Green (5461 Å)", 546.1f, 1),
            Triple("Yellow I (5770 Å)", 577.0f, 1),
            Triple("Red (Std 6800 Å)", 680.0f, 1)
        )
        val initialList = sampleLines.map { (label, wl, order) ->
            val sinT = (order * wl) / (defaultD * 1000f)
            val exactDeg = Math.toDegrees(asin(sinT.toDouble())).toFloat()
            ObservationRecord(
                id = nextRecordId++,
                sourceLabel = label,
                order = order,
                slitSpacingUm = defaultD,
                linesPerInch = 15000,
                v1LeftDeg = -exactDeg,
                v1RightDeg = exactDeg,
                v1TwoThetaDeg = exactDeg * 2f,
                v2LeftDeg = 180f - exactDeg,
                v2RightDeg = 180f + exactDeg,
                v2TwoThetaDeg = exactDeg * 2f,
                twoThetaDeg = exactDeg * 2f,
                meanThetaDeg = exactDeg,
                calculatedWavelengthNm = wl,
                trueWavelengthNm = wl,
                percentError = 0.0f
            )
        }
        _uiState.update { it.copy(observations = initialList) }
    }

    fun selectTab(tab: LabTab) {
        _uiState.update { it.copy(selectedTab = tab, statusBannerMessage = null) }
    }

    fun setBenchExperimentMode(mode: BenchExperimentMode) {
        _uiState.update { state ->
            when (mode) {
                BenchExperimentMode.DIFFRACTION_GRATING -> state.copy(
                    benchMode = mode,
                    numSlits = state.numSlits.coerceAtLeast(12),
                    statusBannerMessage = "Mode: ${mode.title} (${mode.formulaSummary})"
                )
                BenchExperimentMode.SINGLE_SLIT_HEISENBERG -> state.copy(
                    benchMode = mode,
                    lightSourceMode = LightSourceMode.MONOCHROMATIC,
                    wavelengthNm = 655f, // Manual Page 98: λ = 655 nm
                    slitWidthUm = 1.80f,
                    showEnvelope = true,
                    statusBannerMessage = "Mode: Exp 9 Single-Slit Heisenberg Uncertainty (λ = 655 nm)"
                )
                BenchExperimentMode.WIRE_THICKNESS_LASER -> state.copy(
                    benchMode = mode,
                    lightSourceMode = LightSourceMode.MONOCHROMATIC,
                    wavelengthNm = 632.8f, // Manual Page 20: He-Ne λ = 6328 Å
                    statusBannerMessage = "Mode: Exp 1 Thin Wire Thickness by He-Ne Laser (λ = 6328 Å)"
                )
            }
        }
    }

    fun setBenchVisualStyle(style: BenchVisualStyle) {
        _uiState.update { it.copy(benchVisualStyle = style) }
    }

    fun toggleLampPower() {
        _uiState.update { state ->
            val next = !state.lampPowerOn
            state.copy(
                lampPowerOn = next,
                statusBannerMessage = if (next) "Light source powered ON & stabilized." else "Light source switched OFF."
            )
        }
    }

    fun setLightSourceMode(mode: LightSourceMode) {
        _uiState.update { state ->
            val newWl = when (mode) {
                LightSourceMode.MONOCHROMATIC -> state.wavelengthNm
                LightSourceMode.MERCURY_VAPOR -> 546.1f
                LightSourceMode.SODIUM_DOUBLET -> 589.0f
                LightSourceMode.HYDROGEN_BALMER -> 656.3f
                LightSourceMode.CADMIUM_LAMP -> 643.8f
                LightSourceMode.WHITE_CONTINUUM -> 535.0f
            }
            state.copy(
                lightSourceMode = mode,
                wavelengthNm = newWl,
                statusBannerMessage = "Switched source to ${mode.displayName}"
            )
        }
    }

    fun setWavelengthNm(wavelength: Float) {
        val clamped = wavelength.coerceIn(380f, 700f)
        _uiState.update { state ->
            state.copy(
                lightSourceMode = LightSourceMode.MONOCHROMATIC,
                wavelengthNm = clamped,
                statusBannerMessage = null
            )
        }
    }

    fun setSlitSpacingUm(spacingUm: Float) {
        val clamped = spacingUm.coerceIn(0.80f, 15.0f)
        _uiState.update { state ->
            val safeSlitWidth = state.slitWidthUm.coerceAtMost(clamped * 0.75f)
            state.copy(
                slitSpacingUm = clamped,
                slitWidthUm = safeSlitWidth,
                statusBannerMessage = null
            )
        }
    }

    fun setGratingLpi(lpi: Int) {
        val spacingUm = DiffractionPhysics.lpiToSlitSpacingUm(lpi)
        setSlitSpacingUm(spacingUm)
    }

    fun setSlitWidthUm(widthUm: Float) {
        _uiState.update { state ->
            val maxAllowed = if (state.benchMode == BenchExperimentMode.SINGLE_SLIT_HEISENBERG) {
                4.5f
            } else {
                (state.slitSpacingUm * 0.85f).coerceAtLeast(0.20f)
            }
            state.copy(
                slitWidthUm = widthUm.coerceIn(0.15f, maxAllowed),
                statusBannerMessage = null
            )
        }
    }

    fun setNumSlits(slits: Int) {
        _uiState.update {
            it.copy(numSlits = slits.coerceIn(1, 60), statusBannerMessage = null)
        }
    }

    fun setScreenDistanceM(distanceM: Float) {
        _uiState.update {
            it.copy(screenDistanceM = distanceM.coerceIn(0.3f, 2.0f), statusBannerMessage = null)
        }
    }

    fun setTelescopeAngleDeg(angleDeg: Float) {
        _uiState.update {
            it.copy(telescopeAngleDeg = angleDeg.coerceIn(-75f, 75f), statusBannerMessage = null)
        }
    }

    fun nudgeTelescopeAngle(deltaDeg: Float) {
        _uiState.update {
            it.copy(
                telescopeAngleDeg = (it.telescopeAngleDeg + deltaDeg).coerceIn(-75f, 75f),
                statusBannerMessage = null
            )
        }
    }

    fun setCrosswireStyle(style: CrosswireStyle) {
        _uiState.update { it.copy(crosswireStyle = style) }
    }

    fun setCollimatorSlitOpeningMm(mm: Float) {
        _uiState.update { it.copy(collimatorSlitOpeningMm = mm.coerceIn(0.10f, 1.80f)) }
    }

    fun setTelescopeFocusPercent(focus: Float) {
        _uiState.update { it.copy(telescopeFocusPercent = focus.coerceIn(0f, 100f)) }
    }

    fun autoSchusterFocus() {
        _uiState.update {
            it.copy(
                telescopeFocusPercent = 50f,
                collimatorSlitOpeningMm = 0.20f,
                isPrismTableLeveled = true,
                prismTableAngleDeg = 45f,
                statusBannerMessage = "Schuster's parallel-ray focus, narrow slit (0.20 mm), & 45° normal grating alignment complete!"
            )
        }
    }

    fun setPrismTableAngleDeg(angle: Float) {
        _uiState.update {
            it.copy(
                prismTableAngleDeg = angle,
                statusBannerMessage = "Prism/Grating table set to ${angle.roundToInt()}° (${if (angle == 45f || angle == 135f) "Normal Incidence" else "Calibration Step"})"
            )
        }
    }

    fun setVernierDivisions(divisions: Int) {
        _uiState.update {
            val lcStr = if (divisions == 60) "30″ (0.5′)" else "1′ (0.0167°)"
            it.copy(
                vernierDivisions = divisions,
                statusBannerMessage = "Spectrometer Vernier set to $divisions divisions (LC = $lcStr)"
            )
        }
    }

    fun lockLeftSpectrumReading() {
        _uiState.update { state ->
            state.copy(
                lockedLeftAngleDeg = state.telescopeAngleDeg,
                statusBannerMessage = "Locked Left Reading (a): V₁ = ${state.vernierWindowA.formattedDMS}, V₂ = ${state.vernierWindowB.formattedDMS}"
            )
        }
    }

    fun lockRightSpectrumReading() {
        _uiState.update { state ->
            state.copy(
                lockedRightAngleDeg = state.telescopeAngleDeg,
                statusBannerMessage = "Locked Right Reading (b): V₁ = ${state.vernierWindowA.formattedDMS}, V₂ = ${state.vernierWindowB.formattedDMS}"
            )
        }
    }

    fun snapToNearestPeak() {
        _uiState.update { state ->
            val targetPeak = state.principalPeaks
                .filter { it.order != 0 }
                .minByOrNull { abs(it.angleDeg - state.telescopeAngleDeg) }
                ?: state.principalPeaks.firstOrNull()

            if (targetPeak != null) {
                state.copy(
                    telescopeAngleDeg = targetPeak.angleDeg,
                    statusBannerMessage = "Aligned crosshair to m = ${if (targetPeak.order > 0) "+${targetPeak.order}" else targetPeak.order} (${targetPeak.spectralLine.name} @ ${"%.2f".format(targetPeak.angleDeg)}°)"
                )
            } else {
                state
            }
        }
    }

    fun snapToSpecificPeak(peak: DiffractionPeak) {
        _uiState.update { state ->
            state.copy(
                telescopeAngleDeg = peak.angleDeg,
                statusBannerMessage = "Locked onto m = ${if (peak.order > 0) "+${peak.order}" else peak.order} (${peak.spectralLine.name} • ${peak.spectralLine.wavelengthAngstrom} Å)"
            )
        }
    }

    fun toggleEnvelope() {
        _uiState.update { it.copy(showEnvelope = !it.showEnvelope) }
    }

    fun toggleWaveAnimation() {
        _uiState.update { it.copy(animateWaves = !it.animateWaves) }
    }

    fun recordCurrentObservation() {
        _uiState.update { state ->
            val nearest = state.nearestPeak
            val orderUsed = when {
                nearest != null && abs(nearest.angleDeg - state.telescopeAngleDeg) < 4.0f && nearest.order != 0 -> abs(nearest.order)
                else -> 1
            }

            // Use locked left/right readings if both were captured, otherwise symmetric from current telescope angle
            val leftA = state.lockedLeftAngleDeg ?: -abs(state.telescopeAngleDeg)
            val rightB = state.lockedRightAngleDeg ?: abs(state.telescopeAngleDeg)
            val twoTheta = if (abs(rightB - leftA) > 0.5f && abs(abs(rightB - leftA) / 2f - abs(state.telescopeAngleDeg)) < 3.0f) {
                abs(rightB - leftA)
            } else {
                abs(state.telescopeAngleDeg).coerceAtLeast(0.5f) * 2f
            }
            val meanTheta = twoTheta / 2f
            val thetaRad = Math.toRadians(meanTheta.toDouble())
            val dNm = state.slitSpacingUm * 1000.0
            val calcWl = ((dNm * sin(thetaRad)) / orderUsed).toFloat()

            val refLine = if (nearest != null && abs(nearest.angleDeg - state.telescopeAngleDeg) < 5.0f) {
                nearest.spectralLine
            } else {
                state.activeSpectralLines.first()
            }

            val trueWl = refLine.wavelengthNm
            val errPct = ((abs(calcWl - trueWl) / trueWl) * 100f).coerceIn(0f, 99.9f)

            val newRecord = ObservationRecord(
                id = nextRecordId++,
                sourceLabel = refLine.name,
                order = orderUsed,
                slitSpacingUm = state.slitSpacingUm,
                linesPerInch = state.linesPerInch,
                v1LeftDeg = -meanTheta,
                v1RightDeg = meanTheta,
                v1TwoThetaDeg = twoTheta,
                v2LeftDeg = 180f - meanTheta,
                v2RightDeg = 180f + meanTheta,
                v2TwoThetaDeg = twoTheta,
                twoThetaDeg = twoTheta,
                meanThetaDeg = meanTheta,
                calculatedWavelengthNm = calcWl,
                trueWavelengthNm = trueWl,
                percentError = errPct
            )

            state.copy(
                observations = listOf(newRecord) + state.observations,
                lockedLeftAngleDeg = -meanTheta,
                lockedRightAngleDeg = meanTheta,
                statusBannerMessage = "Logged m=±$orderUsed: 2θ=${"%.2f".format(twoTheta)}°, θ=${"%.2f".format(meanTheta)}°, λ=${(calcWl * 10).roundToInt()} Å (${"%.2f".format(errPct)}% err)"
            )
        }
    }

    fun selectWirePreset(index: Int) {
        _uiState.update { state ->
            val thickness = when (index) {
                0 -> 0.12f // Standard Wire 1 (Manual Page 22)
                1 -> 0.18f // Standard Wire 2 (Manual Page 22)
                else -> state.wireThicknessMm
            }
            state.copy(
                wirePresetIndex = index,
                wireThicknessMm = thickness,
                statusBannerMessage = "Selected Wire ${index + 1}: Standard thickness d = $thickness mm"
            )
        }
    }

    fun setWireThicknessMm(mm: Float) {
        _uiState.update {
            it.copy(wirePresetIndex = 2, wireThicknessMm = mm.coerceIn(0.05f, 0.35f))
        }
    }

    // Companion PH3001(P) Virtual Labs Controls
    fun selectPh3001Module(module: PH3001Module) {
        _uiState.update {
            it.copy(
                selectedPh3001Module = module,
                manualPdfPageIndex = (module.manualPage - 1).coerceAtLeast(0)
            )
        }
    }

    fun setNewtonLensRadiusMm(rMm: Float) {
        _uiState.update { it.copy(newtonLensRadiusMm = rMm.coerceIn(300f, 800f)) }
    }

    fun setNewtonMediumIndex(mu: Float) {
        _uiState.update { it.copy(newtonMediumRefractiveIndex = mu.coerceIn(1.0f, 1.65f)) }
    }

    fun setNewtonMicroscopePosMm(posMm: Float) {
        _uiState.update { it.copy(newtonMicroscopePosMm = posMm.coerceIn(-4.5f, 4.5f)) }
    }

    fun setFiberIndices(core: Float, cladding: Float) {
        val safeCore = core.coerceIn(1.42f, 1.62f)
        val safeClad = cladding.coerceIn(1.35f, (safeCore - 0.02f))
        _uiState.update { it.copy(fiberCoreIndex = safeCore, fiberCladdingIndex = safeClad) }
    }

    fun setFiberDetectorOffsetMm(offsetMm: Float) {
        _uiState.update { it.copy(fiberDetectorOffsetMm = offsetMm.coerceIn(-12f, 12f)) }
    }

    fun setFiberDetectorDistanceMm(distMm: Float) {
        _uiState.update { it.copy(fiberDetectorDistanceMm = distMm.coerceIn(10f, 40f)) }
    }

    fun selectPlanckLed(index: Int) {
        _uiState.update { it.copy(planckSelectedLedIndex = index.coerceIn(0, 3)) }
    }

    fun setPlanckBiasVoltage(voltage: Float) {
        _uiState.update { it.copy(planckBiasVoltageV = voltage.coerceIn(0f, 3.2f)) }
    }

    fun setScrewGaugeReading(psrMm: Int, hscDiv: Int, zeroErrorDiv: Int) {
        _uiState.update {
            it.copy(
                screwGaugePitchScaleMm = psrMm.coerceIn(0, 15),
                screwGaugeHeadDiv = hscDiv.coerceIn(0, 99),
                screwGaugeZeroErrorDiv = zeroErrorDiv.coerceIn(-10, 10)
            )
        }
    }

    fun setManualPdfPageIndex(pageIndex: Int) {
        _uiState.update { it.copy(manualPdfPageIndex = pageIndex.coerceIn(0, 112)) }
    }

    fun deleteObservation(id: Int) {
        _uiState.update { state ->
            state.copy(observations = state.observations.filterNot { it.id == id })
        }
    }

    fun clearObservations() {
        _uiState.update {
            it.copy(
                observations = emptyList(),
                statusBannerMessage = "Cleared observation table."
            )
        }
    }

    fun toggleProcedureStep(stepNumber: Int) {
        _uiState.update { state ->
            val updated = if (stepNumber in state.completedProcedureSteps) {
                state.completedProcedureSteps - stepNumber
            } else {
                state.completedProcedureSteps + stepNumber
            }
            state.copy(completedProcedureSteps = updated)
        }
    }

    fun answerVivaQuestion(questionId: Int, optionIndex: Int) {
        _uiState.update { state ->
            state.copy(
                selectedQuizAnswers = state.selectedQuizAnswers + (questionId to optionIndex)
            )
        }
    }

    fun resetVivaQuiz() {
        _uiState.update { it.copy(selectedQuizAnswers = emptyMap()) }
    }

    fun toggleGratingInsertedOnBench() {
        _uiState.update { state ->
            val next = !state.isGratingInsertedOnBench
            state.copy(
                isGratingInsertedOnBench = next,
                statusBannerMessage = if (next) {
                    "IIT-K Virtual Lab: Diffraction Grating placed between Laser and Screen (Higher orders ±n active)."
                } else {
                    "IIT-K Virtual Lab: Grating removed from path — only undiffracted central n=0 beam reaches Screen."
                }
            )
        }
    }

    fun setWhiteLightFilterBand(band: String?) {
        _uiState.update { state ->
            state.copy(
                whiteLightFilterBand = band,
                statusBannerMessage = if (band == null) {
                    "Polychromatic Filter: Showing all spectral color channels simultaneously."
                } else {
                    "Color Filter Active ($band): Isolating $band diffraction orders from polychromatic input."
                }
            )
        }
    }

    fun resetApparatus() {
        _uiState.update { state ->
            DiffractionLabUiState(
                selectedTab = state.selectedTab,
                observations = state.observations,
                statusBannerMessage = "Apparatus reset to standard 15,000 LPI grating & Mercury Vapor Lamp."
            )
        }
    }
}
