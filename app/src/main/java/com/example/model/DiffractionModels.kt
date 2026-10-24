package com.example.model

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

enum class LabTab {
    OPTICAL_BENCH,
    SPECTROMETER,
    LAB_LOG,
    PH3001_LABS,
    THEORY_VIVA
}

enum class BenchExperimentMode(
    val title: String,
    val shortTitle: String,
    val manualExpRef: String,
    val formulaSummary: String
) {
    DIFFRACTION_GRATING(
        title = "Diffraction Grating (Multi-Slit)",
        shortTitle = "Grating (Exp 5)",
        manualExpRef = "Exp 5 • Grating & Spectrometer",
        formulaSummary = "e sin θ = n λ   (e = 2.54 / N cm)"
    ),
    SINGLE_SLIT_HEISENBERG(
        title = "Single-Slit & Heisenberg Uncertainty",
        shortTitle = "Single Slit (Exp 9)",
        manualExpRef = "Exp 9 • Heisenberg Uncertainty Principle",
        formulaSummary = "a sin θ = n λ  |  Δy · Δp_y = h"
    ),
    WIRE_THICKNESS_LASER(
        title = "Thin Wire Thickness by He-Ne Laser",
        shortTitle = "Wire Laser (Exp 1)",
        manualExpRef = "Exp 1 • Thickness of Wire (He-Ne 6328 Å)",
        formulaSummary = "d = (D · λ) / b   (Half-width b = W / 2)"
    )
}

enum class BenchVisualStyle(val label: String) {
    RAY_BENCH_2D("2D Ray Bench"),
    PERSPECTIVE_3D_LAB("3D Perspective Lab")
}

enum class CrosswireStyle(val label: String) {
    MULTIPLICATION_X("× Crosswire (Manual 5.8)"),
    STANDARD_PLUS("+ Crosswire"),
    DOUBLE_HAIRLINE("|| Double Hairline")
}

enum class PH3001Module(
    val expCode: String,
    val title: String,
    val manualPage: Int
) {
    GD_CALC_LUMERICAL("CAD 1", "GD-Calc & Lumerical Sub-λ", 55),
    DFB_BRAGG_TMM("CAD 2", "DFB Bragg Grating (TMM)", 58),
    NEWTONS_RINGS("Exp 4", "Newton's Rings Microscope", 45),
    OPTICAL_FIBER_NA("Exp 3", "Optical Fiber N.A. Scanner", 38),
    PLANCK_LED("Exp 6", "Planck's Constant (LED V-I)", 75),
    INSTRUMENTS_LC("Exp 0", "Screw Gauge & Vernier LC", 6)
}

enum class LightSourceMode(
    val displayName: String,
    val shortName: String,
    val description: String
) {
    MONOCHROMATIC(
        displayName = "Tunable Monochromatic Laser",
        shortName = "Laser (λ)",
        description = "Single coherent wavelength adjustable across the visible spectrum (380–700 nm)."
    ),
    MERCURY_VAPOR(
        displayName = "Mercury Vapor Lamp (Hg - Exp 5)",
        shortName = "Hg Lamp",
        description = "Medicaps Manual & PrayogShala polychromatic source: Violet (4046 Å), Blue, Green (5461 Å), Yellow (5770 Å), Red (6800 Å)."
    ),
    SODIUM_DOUBLET(
        displayName = "Sodium Vapor Doublet (Na)",
        shortName = "Na D₁/D₂",
        description = "High-resolution yellow D₁ (5896 Å) and D₂ (5890 Å) doublet for resolving power & Newton's rings."
    ),
    HYDROGEN_BALMER(
        displayName = "Hydrogen Balmer Tube (H)",
        shortName = "H Balmer",
        description = "Atomic hydrogen emission spectrum featuring Hα (6563 Å), Hβ (4861 Å), Hγ (4340 Å), and Hδ (4102 Å)."
    ),
    CADMIUM_LAMP(
        displayName = "Cadmium Spectral Lamp (Cd)",
        shortName = "Cd Lamp",
        description = "High-coherence Cadmium atomic lines: Violet (4678 Å), Cyan (4800 Å), Green (5086 Å), and Red (6438 Å)."
    ),
    WHITE_CONTINUUM(
        displayName = "White Light Continuum",
        shortName = "White Light",
        description = "Continuous polychromatic spectrum (4000–6800 Å) showing central white fringe and full rainbow orders."
    )
}

data class SpectralLine(
    val name: String,
    val wavelengthNm: Float,
    val relativeIntensity: Float = 1.0f
) {
    val wavelengthAngstrom: Int
        get() = (wavelengthNm * 10f).roundToInt()
}

data class DiffractionPeak(
    val order: Int,
    val spectralLine: SpectralLine,
    val angleRad: Float,
    val angleDeg: Float,
    val screenYCm: Float,
    val relativeIntensity: Float
)

data class VernierReading(
    val totalDegrees: Float,
    val degreesInt: Int,
    val minutesInt: Int,
    val secondsInt: Int,
    val mainScaleDeg: Float,
    val vernierCoincidence: Int,
    val leastCountMinutes: Float,
    val formattedDMS: String
)

data class ObservationRecord(
    val id: Int,
    val sourceLabel: String,
    val order: Int,
    val slitSpacingUm: Float,
    val linesPerInch: Int,
    // Vernier V1 readings (Left a, Right b, 2θ = |a - b|)
    val v1LeftDeg: Float,
    val v1RightDeg: Float,
    val v1TwoThetaDeg: Float,
    // Vernier V2 (+180°) readings (Left a, Right b, 2θ = |a - b|)
    val v2LeftDeg: Float,
    val v2RightDeg: Float,
    val v2TwoThetaDeg: Float,
    // Mean 2θ and θ
    val twoThetaDeg: Float,
    val meanThetaDeg: Float,
    val calculatedWavelengthNm: Float,
    val trueWavelengthNm: Float,
    val percentError: Float
) {
    val calculatedAngstrom: Int
        get() = (calculatedWavelengthNm * 10f).roundToInt()
    val trueAngstrom: Int
        get() = (trueWavelengthNm * 10f).roundToInt()

    val v1LeftVernier: VernierReading
        get() = DiffractionPhysics.toVernierReading(v1LeftDeg)
    val v1RightVernier: VernierReading
        get() = DiffractionPhysics.toVernierReading(v1RightDeg)
    val v2LeftVernier: VernierReading
        get() = DiffractionPhysics.toVernierReading(v2LeftDeg)
    val v2RightVernier: VernierReading
        get() = DiffractionPhysics.toVernierReading(v2RightDeg)
}

data class FlowchartActionStep(
    val stepIndex: Int,
    val title: String,
    val subtitle: String,
    val targetAngleDeg: Float?,
    val targetPrismAngleDeg: Float? = null,
    val isDecisionNode: Boolean = false
)

data class VivaQuestion(
    val id: Int,
    val category: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class ProcedureStep(
    val stepNumber: Int,
    val title: String,
    val detail: String,
    val formulaHint: String
)

object DiffractionPhysics {

    val mercuryLines = listOf(
        SpectralLine("Violet (Std 4046 Å)", 404.6f, 0.85f),
        SpectralLine("Blue-Violet", 435.8f, 0.92f),
        SpectralLine("Cyan-Green", 491.6f, 0.58f),
        SpectralLine("Green (Primary)", 546.1f, 1.00f),
        SpectralLine("Yellow I", 577.0f, 0.92f),
        SpectralLine("Yellow II", 579.0f, 0.90f),
        SpectralLine("Red (Std 6800 Å)", 680.0f, 0.82f)
    )

    val sodiumLines = listOf(
        SpectralLine("Na D₂ (Yellow)", 589.0f, 1.00f),
        SpectralLine("Na D₁ (Yellow)", 589.6f, 0.92f)
    )

    val hydrogenLines = listOf(
        SpectralLine("Hδ (Violet)", 410.2f, 0.65f),
        SpectralLine("Hγ (Blue-Violet)", 434.0f, 0.78f),
        SpectralLine("Hβ (Cyan)", 486.1f, 0.92f),
        SpectralLine("Hα (Red)", 656.3f, 1.00f)
    )

    val cadmiumLines = listOf(
        SpectralLine("Cd Violet", 467.8f, 0.78f),
        SpectralLine("Cd Cyan", 480.0f, 0.88f),
        SpectralLine("Cd Green", 508.6f, 0.96f),
        SpectralLine("Cd Red", 643.8f, 1.00f)
    )

    val whiteContinuumSampledLines = listOf(
        SpectralLine("Violet Band", 410f, 0.75f),
        SpectralLine("Blue Band", 455f, 0.85f),
        SpectralLine("Cyan Band", 495f, 0.90f),
        SpectralLine("Green Band", 535f, 1.00f),
        SpectralLine("Yellow Band", 580f, 0.95f),
        SpectralLine("Orange Band", 615f, 0.90f),
        SpectralLine("Red Band", 660f, 0.85f)
    )

    val presetLaserWavelengths = listOf(
        SpectralLine("Violet Diode", 405f),
        SpectralLine("Blue Argon", 473f),
        SpectralLine("Green DPSS", 532f),
        SpectralLine("Yellow He-Ne", 594f),
        SpectralLine("He-Ne (Exp 1: 6328 Å)", 632.8f),
        SpectralLine("Diode (Exp 9: 655 nm)", 655.0f)
    )

    val presetGratingsLpi = listOf(
        2032 to "80 L/mm (Physics Online: d = 12.5 µm)",
        2540 to "100 L/mm (d = 10.0 µm)",
        7620 to "300 L/mm (d = 3.33 µm)",
        12700 to "500 L/mm (d = 2.00 µm)",
        15000 to "15,000 LPI Manual Std (d = 1.693 µm)",
        15240 to "600 L/mm Thorlabs/Edmund (d = 1.667 µm)",
        20320 to "800 L/mm (d = 1.25 µm)",
        25400 to "1000 L/mm (d = 1.00 µm)"
    )

    val visibleColorSpectrumBands = listOf(
        Triple("Violet", "3900 – 4550 Å (390 – 455 nm)", 420f),
        Triple("Blue", "4550 – 4920 Å (455 – 492 nm)", 470f),
        Triple("Green", "4920 – 5770 Å (492 – 577 nm)", 532f),
        Triple("Yellow", "5770 – 5970 Å (577 – 597 nm)", 585f),
        Triple("Orange", "5970 – 6220 Å (597 – 622 nm)", 608f),
        Triple("Red", "6220 – 7000 Å (622 – 700 nm)", 650f)
    )

    val gratingFaqItems = listOf(
        Triple(
            "What is the order of spectrum (n) in a diffraction grating?",
            "n = 0, ±1, ±2, ±3 ... (where n_max = ⌊d / λ⌋)",
            "The order n represents the integer number of wavelengths (nλ) of path difference between light rays emerging from adjacent slits. n = 0 is the undeviated central bright maximum; n = ±1, ±2 are the first- and second-order dispersed spectra."
        ),
        Triple(
            "How do you measure laser wavelength using symmetrical screen distance s_n?",
            "θ = arctan(x / L) = arctan(s_n / 2L)  ⟹  λ = (d · sin θ) / n",
            "Place a screen at distance L from the grating. Measure the total distance s_n between the nth order bright spots on the left (-n) and right (+n) sides of the central spot, divide by 2 to get x = s_n / 2, compute θ = arctan(x / L), and solve λ = (d sin θ) / n."
        ),
        Triple(
            "What should you look for when choosing a grating (Edmund Optics / Thorlabs)?",
            "Groove Density (lines/mm), Blaze Wavelength (λ_B), & Transmission vs. Reflection",
            "1) Groove Density N controls angular dispersion. 2) Blaze Angle / Blaze Wavelength concentrates diffracted energy into a specific order for maximum efficiency. 3) Ruled gratings offer high efficiency while Holographic gratings minimize stray light and ghosting."
        ),
        Triple(
            "What is Free Spectral Range (FSR) and order overlapping?",
            "FSR = λ / n   (Overlapping occurs when n₁·λ₁ = n₂·λ₂)",
            "In higher orders (n ≥ 2), the red end of order n can overlap with the violet/UV end of order n+1 because sin θ depends on the product nλ (e.g., 2 × 600 nm = 3 × 400 nm)."
        )
    )

    // Interactive Flowchart from Page 9 of Wavelength_using_Grating.pdf
    val exp5FlowchartSteps = listOf(
        FlowchartActionStep(
            stepIndex = 1,
            title = "1. Switch ON Mercury Lamp & Determine Least Count",
            subtitle = "1 MSD = 0.5° (30′), 30 VSD ⟹ LC = 30′ / 30 = 1′",
            targetAngleDeg = 0f,
            targetPrismAngleDeg = 45f
        ),
        FlowchartActionStep(
            stepIndex = 2,
            title = "2. Normal Incidence Grating Alignment (90° → 45°)",
            subtitle = "Rotate telescope to 90°, align reflected image on prism table, then turn table by 45° or 135°.",
            targetAngleDeg = 0f,
            targetPrismAngleDeg = 45f,
            isDecisionNode = true
        ),
        FlowchartActionStep(
            stepIndex = 3,
            title = "3. Observe White Central Slit (Zero-Order m = 0)",
            subtitle = "Source, grating & telescope in a straight line (θ = 0° 00′).",
            targetAngleDeg = 0f
        ),
        FlowchartActionStep(
            stepIndex = 4,
            title = "4. Swing Telescope LEFT (1st Order): Aim at RED Line",
            subtitle = "Set × cross-wire on Red (6800 Å) left spectrum & note V₁ and V₂ (a).",
            targetAngleDeg = -23.68f
        ),
        FlowchartActionStep(
            stepIndex = 5,
            title = "5. Swing Telescope LEFT (1st Order): Aim at YELLOW & VIOLET",
            subtitle = "Set × cross-wire on Yellow (5770 Å @ -19.92°) and Violet (4046 Å @ -13.82°).",
            targetAngleDeg = -13.82f
        ),
        FlowchartActionStep(
            stepIndex = 6,
            title = "6. Swing Telescope RIGHT (1st Order): Aim at VIOLET, YELLOW & RED",
            subtitle = "Repeat on the right side (b): Violet (+13.82°), Yellow (+19.92°), Red (+23.68°).",
            targetAngleDeg = 23.68f
        ),
        FlowchartActionStep(
            stepIndex = 7,
            title = "7. Compute 2θ = |a - b|, θ = 2θ/2, & λ = e sin θ / n",
            subtitle = "Verify % Error = |(Std - Expt) / Std| × 100 < 20% for each color!",
            targetAngleDeg = 13.82f,
            isDecisionNode = true
        )
    )

    val spectrometerPrecautions = listOf(
        "1. Both the telescope and the collimator should be focused for parallel rays (Schuster's method).",
        "2. The eyepiece should be focused distinctly on the cross-wires without parallax.",
        "3. The Prism table should be properly leveled using the spirit level or optical pin method.",
        "4. While taking observations, the collimator slit should be made narrow and the cross-wires as a multiplication mark (×) should be adjusted at the exact center of the spectral line image."
    )

    val spectrometerApplications = listOf(
        "1. Calibration of optical spectrometers using standard emission lines.",
        "2. Identification of unknown chemical elements by their characteristic emission spectra.",
        "3. Study of atomic and molecular spectral lines and fine structure (e.g., Sodium doublet).",
        "4. Determination of atomic electronic energy levels and transitions.",
        "5. High-precision wavelength calibration of optical and photonic instruments."
    )

    fun activeLinesForMode(mode: LightSourceMode, customWavelengthNm: Float): List<SpectralLine> {
        return when (mode) {
            LightSourceMode.MONOCHROMATIC -> listOf(
                SpectralLine("Laser (${customWavelengthNm.roundToInt()} nm)", customWavelengthNm, 1.0f)
            )
            LightSourceMode.MERCURY_VAPOR -> mercuryLines
            LightSourceMode.SODIUM_DOUBLET -> sodiumLines
            LightSourceMode.HYDROGEN_BALMER -> hydrogenLines
            LightSourceMode.CADMIUM_LAMP -> cadmiumLines
            LightSourceMode.WHITE_CONTINUUM -> whiteContinuumSampledLines
        }
    }

    fun slitSpacingUmToLpi(slitSpacingUm: Float): Int {
        return (25400f / slitSpacingUm.coerceAtLeast(0.5f)).roundToInt()
    }

    fun lpiToSlitSpacingUm(lpi: Int): Float {
        return 25400f / lpi.coerceAtLeast(500).toFloat()
    }

    fun slitSpacingUmToLinesPerMm(slitSpacingUm: Float): Float {
        return 1000f / slitSpacingUm.coerceAtLeast(0.5f)
    }

    fun calculateIntensityAtAngle(
        angleRad: Float,
        wavelengthNm: Float,
        slitSpacingUm: Float,
        slitWidthUm: Float,
        numSlits: Int,
        includeEnvelope: Boolean = true
    ): Float {
        val sinTheta = sin(angleRad.toDouble())
        val dNm = slitSpacingUm.toDouble() * 1000.0
        val aNm = slitWidthUm.coerceAtMost(slitSpacingUm * 0.95f).toDouble() * 1000.0
        val lambda = wavelengthNm.toDouble()

        val alpha = PI * dNm * sinTheta / lambda
        val beta = PI * aNm * sinTheta / lambda

        val envelope = if (!includeEnvelope || abs(beta) < 1e-6) {
            1.0
        } else {
            val s = sin(beta) / beta
            s * s
        }

        val m = numSlits.coerceAtLeast(1)
        val interference = if (m == 1) {
            1.0
        } else {
            val sinAlpha = sin(alpha)
            if (abs(sinAlpha) < 1e-5) {
                1.0
            } else {
                val ratio = sin(m * alpha) / (m * sinAlpha)
                ratio * ratio
            }
        }

        return (envelope * interference).coerceIn(0.0, 1.0).toFloat()
    }

    fun calculateEnvelopeAtAngle(
        angleRad: Float,
        wavelengthNm: Float,
        slitWidthUm: Float
    ): Float {
        val sinTheta = sin(angleRad.toDouble())
        val aNm = slitWidthUm.toDouble() * 1000.0
        val beta = PI * aNm * sinTheta / wavelengthNm.toDouble()
        if (abs(beta) < 1e-6) return 1f
        val s = sin(beta) / beta
        return (s * s).coerceIn(0.0, 1.0).toFloat()
    }

    fun computePrincipalPeaks(
        lines: List<SpectralLine>,
        slitSpacingUm: Float,
        slitWidthUm: Float,
        screenDistanceM: Float
    ): List<DiffractionPeak> {
        val dNm = slitSpacingUm * 1000f
        val peaks = mutableListOf<DiffractionPeak>()

        for (line in lines) {
            val maxOrder = floor((dNm * 0.98f) / line.wavelengthNm).toInt().coerceAtMost(6)
            for (order in -maxOrder..maxOrder) {
                val sinTheta = (order * line.wavelengthNm) / dNm
                if (abs(sinTheta) <= 0.985f) {
                    val angleRad = asin(sinTheta)
                    val angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()
                    val screenYCm = (screenDistanceM * 100f * tan(angleRad))
                    val env = calculateEnvelopeAtAngle(angleRad, line.wavelengthNm, slitWidthUm)
                    peaks.add(
                        DiffractionPeak(
                            order = order,
                            spectralLine = line,
                            angleRad = angleRad,
                            angleDeg = angleDeg,
                            screenYCm = screenYCm,
                            relativeIntensity = (line.relativeIntensity * env).coerceIn(0.08f, 1f)
                        )
                    )
                }
            }
        }
        return peaks.sortedBy { it.angleDeg }
    }

    fun toVernierReading(angleDeg: Float, vernierDivisions: Int = 30): VernierReading {
        val normalized = ((angleDeg % 360f) + 360f) % 360f
        val absAngle = abs(angleDeg)
        val totalArcSeconds = (absAngle * 3600f).roundToInt()
        val degInt = totalArcSeconds / 3600
        val minInt = (totalArcSeconds % 3600) / 60
        val secInt = totalArcSeconds % 60
        val signPrefix = if (angleDeg < -0.001f) "-" else if (angleDeg > 0.001f) "+" else ""

        val totalHalfDegrees = floor(normalized * 2.0).toInt()
        val mainScaleDeg = totalHalfDegrees * 0.5f
        val lcMinutes = 30f / vernierDivisions.coerceAtLeast(10)
        val remainderDeg = normalized - mainScaleDeg
        val coincidence = ((remainderDeg * 60f) / lcMinutes).roundToInt().coerceIn(0, vernierDivisions - 1)

        return VernierReading(
            totalDegrees = angleDeg,
            degreesInt = degInt,
            minutesInt = minInt,
            secondsInt = secInt,
            mainScaleDeg = mainScaleDeg,
            vernierCoincidence = coincidence,
            leastCountMinutes = lcMinutes,
            formattedDMS = "${signPrefix}${degInt}° ${minInt.toString().padStart(2, '0')}′ ${secInt.toString().padStart(2, '0')}″"
        )
    }

    val procedureSteps = listOf(
        ProcedureStep(
            stepNumber = 1,
            title = "Optical Axis Intersection & Spirit Leveling (Manual 5.3 A & B)",
            detail = "Ensure telescope and collimator axes intersect the vertical rotation axis (tested with a center pin on the prism table). Level the prism table using the three leveling screws and spirit level.",
            formulaHint = "Least Count LC = 1 MSD / Total VSD = 0.5° / 30 = 1′"
        ),
        ProcedureStep(
            stepNumber = 2,
            title = "Schuster's Method for Parallel Rays (Manual 5.3 C)",
            detail = "Place a prism at minimum deviation. Turn refracting edge toward collimator and focus rack-and-pinion; then turn toward telescope and focus until slight rotation causes no defocus.",
            formulaHint = "Source & Screen effectively moved to infinity (Fraunhofer condition)"
        ),
        ProcedureStep(
            stepNumber = 3,
            title = "Normal Incidence Grating Setup (0° → 90° → 45°/135°, Manual 5.4)",
            detail = "Coincide vertical crosswire with direct slit image (0°). Rotate telescope by 90°. Rotate grating table until reflected slit image coincides with crosswire, then turn table by 45° or 135°.",
            formulaHint = "Grating Element: e = 2.54 / N cm = 1.6933 × 10⁻⁴ cm (N = 15,000 lines/in)"
        ),
        ProcedureStep(
            stepNumber = 4,
            title = "Measure Left (a) & Right (b) 1st-Order Spectra on V₁ & V₂ (Manual 5.5)",
            detail = "Rotate slit in its plane until spectral lines are sharp and bright. Superimpose multiplication (×) crosswire on Violet (4046 Å), Yellow (5770 Å), and Red (6800 Å) on left (a) and right (b).",
            formulaHint = "2θ = |a - b|   ⟹   θ = Avg(2θ) / 2   ⟹   λ = (e · sin θ) / n"
        )
    )

    // Complete 11 Sample Viva-Voce Questions from Section 5.10 (Page 10 of Wavelength_using_Grating.pdf)
    val vivaQuestions = listOf(
        VivaQuestion(
            id = 1,
            category = "Manual 5.10 Q1 & Q2",
            question = "What is the principle of the Diffraction Grating experiment, and what is the condition to observe a distinct diffraction pattern?",
            options = listOf(
                "Fraunhofer multi-slit diffraction (e sin θ = nλ); slit spacing e must be comparable to wavelength λ of light",
                "Total internal reflection; slit must be several centimeters wide",
                "Refraction through a thick glass slab; light must be unpolarized",
                "Photoelectric emission; frequency must exceed threshold"
            ),
            correctIndex = 0,
            explanation = "The experiment works on Fraunhofer diffraction and constructive interference from N equidistant slits (e sin θ = nλ). To see a clear diffraction pattern, the grating element e must be comparable in size to wavelength λ."
        ),
        VivaQuestion(
            id = 2,
            category = "Manual 5.10 Q3 & Q4",
            question = "What is a Diffraction Grating and what is meant by the 'Grating Element' (e)?",
            options = listOf(
                "An optically flat glass plate with N equidistant parallel ruled slits; Grating Element e = (a + b) = 2.54 / N cm is the distance between two successive slits",
                "A prism that reflects light; Grating Element is the apex angle",
                "A single wide aperture; Grating Element is the screen distance",
                "A polarizing filter; Grating Element is the Brewster angle"
            ),
            correctIndex = 0,
            explanation = "A diffraction grating consists of a large number of equidistant parallel slits. The combined width of one transparent slit (a) and one opaque space (b) is the grating element e = a + b = 2.54 / N cm."
        ),
        VivaQuestion(
            id = 3,
            category = "Manual 5.10 Q5 & Q6",
            question = "What are the three essential parts of a spectrometer and the main function of the diffraction grating?",
            options = listOf(
                "1) Collimator (parallel rays), 2) Prism/Grating Table, 3) Telescope; Grating disperses light into sharp principal maxima by wavelength",
                "1) Voltmeter, 2) Ammeter, 3) Galvanometer; Grating amplifies electric current",
                "1) Laser, 2) Wire, 3) Graph paper; Grating measures wire thickness",
                "1) Micrometer, 2) Pitch scale, 3) Thimble; Grating measures glass thickness"
            ),
            correctIndex = 0,
            explanation = "The spectrometer consists of the Collimator, Prism Table, and Telescope. The diffraction grating disperses incident polychromatic light into discrete spectral orders according to e sin θ = nλ."
        ),
        VivaQuestion(
            id = 4,
            category = "Manual 5.10 Q7",
            question = "Instead of a Mercury (Hg) source, if we use a Sodium (Na) vapor lamp, what changes can we observe?",
            options = listOf(
                "Instead of multiple colored lines (violet, green, yellow, red), only a closely spaced yellow doublet (5890 Å and 5896 Å) is seen in each order",
                "A continuous white rainbow appears",
                "Only deep violet lines are visible",
                "No central maximum is formed"
            ),
            correctIndex = 0,
            explanation = "Mercury emits a polychromatic line spectrum (Violet, Blue, Green, Yellow, Red), whereas Sodium emits nearly monochromatic yellow D₁ (5896 Å) and D₂ (5890 Å) doublet lines."
        ),
        VivaQuestion(
            id = 5,
            category = "Manual 5.10 Q8 & Q9",
            question = "What do you mean by diffraction, and how many centimeters are in 1 inch when calculating e = 2.54 / N?",
            options = listOf(
                "Bending/spreading of waves around obstacles or narrow openings; 1 inch = 2.54 cm",
                "Bending of light at a refractive boundary; 1 inch = 1.00 cm",
                "Absorption of light in a medium; 1 inch = 10.0 cm",
                "Splitting of light by a mirror; 1 inch = 25.4 cm"
            ),
            correctIndex = 0,
            explanation = "Diffraction is the bending or spreading-out of a wave when it passes through a narrow opening or around an obstacle. Since gratings are rated in lines per inch (N), 1 inch = 2.54 cm gives e = 2.54 / N cm."
        ),
        VivaQuestion(
            id = 6,
            category = "Manual 5.10 Q10",
            question = "What is the Least Count (LC) of the laboratory spectrometer with 1 MSD = 0.5° and 30 Vernier divisions?",
            options = listOf(
                "LC = 1 MSD / 30 VSD = 0.5° / 30 = 1′ (one arcminute = 1/60 degree)",
                "LC = 0.5° (30 arcminutes)",
                "LC = 0.01 cm",
                "LC = 10′"
            ),
            correctIndex = 0,
            explanation = "One Main Scale Division (MSD) is 0.5° = 30′. Divided across 30 Vernier Scale Divisions (VSD), the Least Count is 30′ / 30 = 1′."
        ),
        VivaQuestion(
            id = 7,
            category = "Manual 5.10 Q11",
            question = "Why does Red light deviate the most in the case of a diffraction grating?",
            options = listOf(
                "From sin θ = nλ / e, diffraction angle θ increases with wavelength λ; Red (~6800 Å) has the longest visible wavelength",
                "Because Red light has the shortest wavelength",
                "Because glass has a higher refractive index for Red light",
                "Red light actually deviates the least in a grating"
            ),
            correctIndex = 0,
            explanation = "In a grating, sin θ = nλ / e. Since Red has the longest wavelength (6800 Å vs 4046 Å for Violet), sin θ and angle θ are largest for Red (opposite to dispersion in a prism)."
        )
    )
}
