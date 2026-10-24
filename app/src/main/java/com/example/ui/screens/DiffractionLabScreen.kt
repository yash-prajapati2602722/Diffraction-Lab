package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.Biotech
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.model.LabTab
import com.example.model.LightSourceMode
import com.example.ui.components.IntensityProfileCurveCanvas
import com.example.ui.components.InterferenceFringeScreenStrip
import com.example.ui.components.OpticalBenchRayCanvas
import com.example.ui.components.ParameterControlsDeck
import com.example.ui.components.Prayogshala3DSpectrometerCard
import com.example.ui.components.SpectrometerEyepieceAndTurntable
import com.example.ui.components.VernierScaleMagnifierCard
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
import com.example.viewmodel.DiffractionLabViewModel
import kotlin.math.roundToInt

@Composable
fun DiffractionLabScreen(
    viewModel: DiffractionLabViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = uiState.selectedTab != LabTab.OPTICAL_BENCH) {
        viewModel.selectTab(LabTab.OPTICAL_BENCH)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = LabVoidBg,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            DiffractionBottomNavBar(
                selectedTab = uiState.selectedTab,
                observationCount = uiState.observations.size,
                onSelectTab = viewModel::selectTab
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 720.dp
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = if (isWideScreen) 1140.dp else 640.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LabHeaderHudBar(uiState = uiState)

                    AnimatedVisibility(
                        visible = uiState.statusBannerMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        uiState.statusBannerMessage?.let { msg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(LaserCyan.copy(alpha = 0.16f))
                                    .border(1.dp, LaserCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    when (uiState.selectedTab) {
                        LabTab.OPTICAL_BENCH -> {
                            IitKanpurAndWhiteLightChannelBar(
                                uiState = uiState,
                                onToggleLaserPower = viewModel::toggleLampPower,
                                onToggleGratingInserted = viewModel::toggleGratingInsertedOnBench,
                                onSelectWhiteLightMode = {
                                    viewModel.setLightSourceMode(LightSourceMode.WHITE_CONTINUUM)
                                },
                                onSelectColorFilterBand = viewModel::setWhiteLightFilterBand,
                                onPushAngle = viewModel::setTelescopeAngleDeg
                            )
                            if (isWideScreen) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1.1f),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Prayogshala3DSpectrometerCard(
                                            uiState = uiState,
                                            onAngleChange = viewModel::setTelescopeAngleDeg,
                                            onToggleLampPower = viewModel::toggleLampPower
                                        )
                                        InterferenceFringeScreenStrip(
                                            uiState = uiState,
                                            onAngleChange = viewModel::setTelescopeAngleDeg
                                        )
                                        OpticalBenchRayCanvas(
                                            uiState = uiState,
                                            onAngleChange = viewModel::setTelescopeAngleDeg,
                                            onSelectBenchStyle = viewModel::setBenchVisualStyle,
                                            onToggleLampPower = viewModel::toggleLampPower
                                        )
                                        IntensityProfileCurveCanvas(
                                            uiState = uiState,
                                            onAngleChange = viewModel::setTelescopeAngleDeg
                                        )
                                    }
                                    Column(modifier = Modifier.weight(0.9f)) {
                                        ParameterControlsDeck(
                                            uiState = uiState,
                                            onSelectBenchMode = viewModel::setBenchExperimentMode,
                                            onSelectSourceMode = viewModel::setLightSourceMode,
                                            onWavelengthChange = viewModel::setWavelengthNm,
                                            onSlitSpacingChange = viewModel::setSlitSpacingUm,
                                            onGratingLpiSelect = viewModel::setGratingLpi,
                                            onSlitWidthChange = viewModel::setSlitWidthUm,
                                            onNumSlitsChange = viewModel::setNumSlits,
                                            onScreenDistanceChange = viewModel::setScreenDistanceM,
                                            onAngleChange = viewModel::setTelescopeAngleDeg,
                                            onNudgeAngle = viewModel::nudgeTelescopeAngle,
                                            onSnapToPeak = viewModel::snapToNearestPeak,
                                            onLockLeftReading = viewModel::lockLeftSpectrumReading,
                                            onLockRightReading = viewModel::lockRightSpectrumReading,
                                            onRecordReading = viewModel::recordCurrentObservation,
                                            onSelectWirePreset = viewModel::selectWirePreset,
                                            onWireThicknessChange = viewModel::setWireThicknessMm,
                                            onToggleEnvelope = viewModel::toggleEnvelope,
                                            onToggleWaves = viewModel::toggleWaveAnimation,
                                            onResetApparatus = viewModel::resetApparatus
                                        )
                                    }
                                }
                            } else {
                                Prayogshala3DSpectrometerCard(
                                    uiState = uiState,
                                    onAngleChange = viewModel::setTelescopeAngleDeg,
                                    onToggleLampPower = viewModel::toggleLampPower
                                )
                                InterferenceFringeScreenStrip(
                                    uiState = uiState,
                                    onAngleChange = viewModel::setTelescopeAngleDeg
                                )
                                OpticalBenchRayCanvas(
                                    uiState = uiState,
                                    onAngleChange = viewModel::setTelescopeAngleDeg,
                                    onSelectBenchStyle = viewModel::setBenchVisualStyle,
                                    onToggleLampPower = viewModel::toggleLampPower
                                )
                                IntensityProfileCurveCanvas(
                                    uiState = uiState,
                                    onAngleChange = viewModel::setTelescopeAngleDeg
                                )
                                ParameterControlsDeck(
                                    uiState = uiState,
                                    onSelectBenchMode = viewModel::setBenchExperimentMode,
                                    onSelectSourceMode = viewModel::setLightSourceMode,
                                    onWavelengthChange = viewModel::setWavelengthNm,
                                    onSlitSpacingChange = viewModel::setSlitSpacingUm,
                                    onGratingLpiSelect = viewModel::setGratingLpi,
                                    onSlitWidthChange = viewModel::setSlitWidthUm,
                                    onNumSlitsChange = viewModel::setNumSlits,
                                    onScreenDistanceChange = viewModel::setScreenDistanceM,
                                    onAngleChange = viewModel::setTelescopeAngleDeg,
                                    onNudgeAngle = viewModel::nudgeTelescopeAngle,
                                    onSnapToPeak = viewModel::snapToNearestPeak,
                                    onLockLeftReading = viewModel::lockLeftSpectrumReading,
                                    onLockRightReading = viewModel::lockRightSpectrumReading,
                                    onRecordReading = viewModel::recordCurrentObservation,
                                    onSelectWirePreset = viewModel::selectWirePreset,
                                    onWireThicknessChange = viewModel::setWireThicknessMm,
                                    onToggleEnvelope = viewModel::toggleEnvelope,
                                    onToggleWaves = viewModel::toggleWaveAnimation,
                                    onResetApparatus = viewModel::resetApparatus
                                )
                            }
                        }

                        LabTab.SPECTROMETER -> {
                            Prayogshala3DSpectrometerCard(
                                uiState = uiState,
                                onAngleChange = viewModel::setTelescopeAngleDeg,
                                onToggleLampPower = viewModel::toggleLampPower
                            )
                            InterferenceFringeScreenStrip(
                                uiState = uiState,
                                onAngleChange = viewModel::setTelescopeAngleDeg
                            )
                            SpectrometerEyepieceAndTurntable(
                                uiState = uiState,
                                onAngleChange = viewModel::setTelescopeAngleDeg,
                                onCrosswireStyleChange = viewModel::setCrosswireStyle,
                                onSlitOpeningChange = viewModel::setCollimatorSlitOpeningMm,
                                onFocusChange = viewModel::setTelescopeFocusPercent,
                                onAutoSchusterFocus = viewModel::autoSchusterFocus,
                                onPrismTableAngleChange = viewModel::setPrismTableAngleDeg,
                                onVernierDivisionsChange = viewModel::setVernierDivisions,
                                onLockLeftReading = viewModel::lockLeftSpectrumReading,
                                onLockRightReading = viewModel::lockRightSpectrumReading,
                                onRecordObservation = viewModel::recordCurrentObservation
                            )
                            VernierScaleMagnifierCard(uiState = uiState)
                            ParameterControlsDeck(
                                uiState = uiState,
                                onSelectBenchMode = viewModel::setBenchExperimentMode,
                                onSelectSourceMode = viewModel::setLightSourceMode,
                                onWavelengthChange = viewModel::setWavelengthNm,
                                onSlitSpacingChange = viewModel::setSlitSpacingUm,
                                onGratingLpiSelect = viewModel::setGratingLpi,
                                onSlitWidthChange = viewModel::setSlitWidthUm,
                                onNumSlitsChange = viewModel::setNumSlits,
                                onScreenDistanceChange = viewModel::setScreenDistanceM,
                                onAngleChange = viewModel::setTelescopeAngleDeg,
                                onNudgeAngle = viewModel::nudgeTelescopeAngle,
                                onSnapToPeak = viewModel::snapToNearestPeak,
                                onLockLeftReading = viewModel::lockLeftSpectrumReading,
                                onLockRightReading = viewModel::lockRightSpectrumReading,
                                onRecordReading = viewModel::recordCurrentObservation,
                                onSelectWirePreset = viewModel::selectWirePreset,
                                onWireThicknessChange = viewModel::setWireThicknessMm,
                                onToggleEnvelope = viewModel::toggleEnvelope,
                                onToggleWaves = viewModel::toggleWaveAnimation,
                                onResetApparatus = viewModel::resetApparatus
                            )
                        }

                        LabTab.LAB_LOG -> {
                            LabLogTabContent(
                                uiState = uiState,
                                onSnapToPeak = viewModel::snapToSpecificPeak,
                                onRecordCurrent = viewModel::recordCurrentObservation,
                                onDeleteRecord = viewModel::deleteObservation,
                                onClearAll = viewModel::clearObservations
                            )
                        }

                        LabTab.PH3001_LABS -> {
                            Ph3001CompanionLabsTabContent(
                                uiState = uiState,
                                onSelectModule = viewModel::selectPh3001Module,
                                onNewtonRadiusChange = viewModel::setNewtonLensRadiusMm,
                                onNewtonMediumChange = viewModel::setNewtonMediumIndex,
                                onNewtonMicroscopeChange = viewModel::setNewtonMicroscopePosMm,
                                onFiberIndicesChange = viewModel::setFiberIndices,
                                onFiberOffsetChange = viewModel::setFiberDetectorOffsetMm,
                                onFiberDistanceChange = viewModel::setFiberDetectorDistanceMm,
                                onPlanckLedSelect = viewModel::selectPlanckLed,
                                onPlanckVoltageChange = viewModel::setPlanckBiasVoltage,
                                onScrewGaugeChange = viewModel::setScrewGaugeReading,
                                onJumpToManualPage = { pageIdx ->
                                    viewModel.setManualPdfPageIndex(pageIdx)
                                    viewModel.selectTab(LabTab.THEORY_VIVA)
                                },
                                onPushGratingToBench = { wlNm, dUm ->
                                    viewModel.setLightSourceMode(LightSourceMode.MONOCHROMATIC)
                                    viewModel.setWavelengthNm(wlNm)
                                    viewModel.setSlitSpacingUm(dUm)
                                    viewModel.selectTab(LabTab.OPTICAL_BENCH)
                                }
                            )
                        }

                        LabTab.THEORY_VIVA -> {
                            TheoryAndVivaTabContent(
                                uiState = uiState,
                                onToggleProcedureStep = viewModel::toggleProcedureStep,
                                onSelectQuizOption = viewModel::answerVivaQuestion,
                                onResetQuiz = viewModel::resetVivaQuiz,
                                onSelectPdfPage = viewModel::setManualPdfPageIndex,
                                onExecuteFlowchartStep = { step ->
                                    step.targetPrismAngleDeg?.let { viewModel.setPrismTableAngleDeg(it) }
                                    step.targetAngleDeg?.let { viewModel.setTelescopeAngleDeg(it) }
                                    viewModel.selectTab(LabTab.SPECTROMETER)
                                },
                                onOpenPhotonicCadTab = {
                                    viewModel.selectTab(LabTab.PH3001_LABS)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun LabHeaderHudBar(
    uiState: DiffractionLabUiState
) {
    val wlColor = wavelengthToColor(uiState.wavelengthNm)

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(wlColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = stringResource(id = R.string.subtitle_prayogshala),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Surface(
                color = LabSurfaceElevated,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "e sin θ = n λ",
                    style = MaterialTheme.typography.labelLarge,
                    color = LaserCyan,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TelemetryMiniChip(
                label = "λ (Å / nm)",
                value = "${(uiState.wavelengthNm * 10).roundToInt()} Å",
                accent = wlColor,
                modifier = Modifier.weight(1f)
            )
            TelemetryMiniChip(
                label = "Element e",
                value = "${"%.2f".format(uiState.slitSpacingUm)} µm",
                accent = LaserCyan,
                modifier = Modifier.weight(1f)
            )
            TelemetryMiniChip(
                label = "Grating N",
                value = "${uiState.linesPerInch} LPI",
                accent = SpectralEmerald,
                modifier = Modifier.weight(1f)
            )
            TelemetryMiniChip(
                label = "Angle θ",
                value = "${"%.2f".format(uiState.telescopeAngleDeg)}°",
                accent = SpectralAmber,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TelemetryMiniChip(
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

@Composable
private fun DiffractionBottomNavBar(
    selectedTab: LabTab,
    observationCount: Int,
    onSelectTab: (LabTab) -> Unit
) {
    NavigationBar(
        containerColor = LabSurface,
        contentColor = TextPrimary
    ) {
        NavigationBarItem(
            selected = selectedTab == LabTab.OPTICAL_BENCH,
            onClick = { onSelectTab(LabTab.OPTICAL_BENCH) },
            modifier = Modifier.testTag("nav_tab_optical_bench"),
            icon = {
                Icon(
                    imageVector = if (selectedTab == LabTab.OPTICAL_BENCH) Icons.Filled.Science else Icons.Outlined.Science,
                    contentDescription = stringResource(id = R.string.tab_bench)
                )
            },
            label = {
                Text(
                    text = stringResource(id = R.string.tab_bench),
                    style = MaterialTheme.typography.labelSmall
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LaserCyan,
                selectedTextColor = LaserCyan,
                indicatorColor = LaserCyan.copy(alpha = 0.16f),
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )

        NavigationBarItem(
            selected = selectedTab == LabTab.SPECTROMETER,
            onClick = { onSelectTab(LabTab.SPECTROMETER) },
            modifier = Modifier.testTag("nav_tab_spectrometer"),
            icon = {
                Icon(
                    imageVector = if (selectedTab == LabTab.SPECTROMETER) Icons.Filled.Biotech else Icons.Outlined.Biotech,
                    contentDescription = stringResource(id = R.string.tab_spectrometer)
                )
            },
            label = {
                Text(
                    text = stringResource(id = R.string.tab_spectrometer),
                    style = MaterialTheme.typography.labelSmall
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SpectralAmber,
                selectedTextColor = SpectralAmber,
                indicatorColor = SpectralAmber.copy(alpha = 0.16f),
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )

        NavigationBarItem(
            selected = selectedTab == LabTab.LAB_LOG,
            onClick = { onSelectTab(LabTab.LAB_LOG) },
            modifier = Modifier.testTag("nav_tab_lab_log"),
            icon = {
                Icon(
                    imageVector = if (selectedTab == LabTab.LAB_LOG) Icons.Filled.TableChart else Icons.Outlined.TableChart,
                    contentDescription = stringResource(id = R.string.tab_observations)
                )
            },
            label = {
                Text(
                    text = "${stringResource(id = R.string.tab_observations)} ($observationCount)",
                    style = MaterialTheme.typography.labelSmall
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SpectralEmerald,
                selectedTextColor = SpectralEmerald,
                indicatorColor = SpectralEmerald.copy(alpha = 0.16f),
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )

        NavigationBarItem(
            selected = selectedTab == LabTab.PH3001_LABS,
            onClick = { onSelectTab(LabTab.PH3001_LABS) },
            modifier = Modifier.testTag("nav_tab_ph3001_labs"),
            icon = {
                Icon(
                    imageVector = if (selectedTab == LabTab.PH3001_LABS) Icons.Filled.Architecture else Icons.Outlined.Architecture,
                    contentDescription = stringResource(id = R.string.tab_more_labs)
                )
            },
            label = {
                Text(
                    text = stringResource(id = R.string.tab_more_labs),
                    style = MaterialTheme.typography.labelSmall
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = LaserCyan,
                selectedTextColor = LaserCyan,
                indicatorColor = LaserCyan.copy(alpha = 0.16f),
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )

        NavigationBarItem(
            selected = selectedTab == LabTab.THEORY_VIVA,
            onClick = { onSelectTab(LabTab.THEORY_VIVA) },
            modifier = Modifier.testTag("nav_tab_theory_viva"),
            icon = {
                Icon(
                    imageVector = if (selectedTab == LabTab.THEORY_VIVA) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                    contentDescription = stringResource(id = R.string.tab_theory)
                )
            },
            label = {
                Text(
                    text = stringResource(id = R.string.tab_theory),
                    style = MaterialTheme.typography.labelSmall
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SpectralViolet,
                selectedTextColor = SpectralViolet,
                indicatorColor = SpectralViolet.copy(alpha = 0.16f),
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )
    }
}
