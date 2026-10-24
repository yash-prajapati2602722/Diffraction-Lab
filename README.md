# Diffraction Lab — 3D Grating Spectrometer & Photonic CAD Virtual Physics Lab

An interactive Android virtual physics laboratory built with **Kotlin** and **Jetpack Compose (Material 3)** for simulating **Diffraction Grating Spectrometers**, **Laser Screen Fringe Wavelength Determination**, **Sub-Wavelength Photonic Gratings (GD-Calc® / Ansys Lumerical LSWM / Stanford S4 RCWA)**, **DFB Bragg Gratings (OghmaNano TMM)**, and the complete **PH3001(P) Applied Physics Lab Manual** experiment suite.

---

## ✨ Key Features

### 1. Free-Form 3D Spectrometer & Grating Model (`Grating Bench` & `Spectrometer` Tabs)
- **Full 6-DOF Touch Camera Controller**:
  - **1-Finger Free Orbit (360° Yaw & $\pm 88^\circ$ Pitch)**: Inspect the 3D spectrometer, collimator, grating clamp, and telescope from any angle (including top-down, side, and underneath views) without scroll-stealing.
  - **1-Finger / 2-Finger Free Pan ($X, Y$)**: Pan across the optical bench to inspect the mercury lamp housing, collimator slit, prism/grating table leveling screws, or Vernier windows $V_1$ and $V_2$.
  - **Pinch-to-Zoom (`0.45x – 3.20x`) & On-Screen Gimbal HUD**: Multi-touch pinch zoom, double-tap camera reset, and on-screen directional pan/zoom buttons.
- **Hardware Anatomy & Optical Modes**:
  - **Figure 5.1(b) Callout Pins**: Labeled 3D markers for all 15 spectrometer components (*Collimator, Slit, Grating Clamp, Prism Table, Main Scale, Vernier $V_1/V_2$, Telescope, Rack & Pinion Focus, Leveling Screws*).
  - **X-Ray Optics Mode**: Translucent brass barrels revealing internal achromatic doublet lenses, focal planes, and volumetric diffracted rays ($n = 0, \pm 1, \pm 2, \pm 3$).
  - **Floating PiP Crosswire Eyepiece**: Live Picture-in-Picture reticle ($\times$, $+$, or $\parallel$) with Schuster focusing and adjustable slit width.

### 2. IIT-Kanpur Virtual Lab Setup & VisuPhy White-Light Color Filter (`Grating Bench` Tab)
- **Step-by-Step Apparatus Assembly (`bc2-iitk.vlabs.ac.in`)**:
  - Toggle **`1. Laser / Lamp Source`**, **`2. Place Diffraction Grating`** (compare the undiffracted $n=0$ central spot against higher-order diffracted beams), **`3. Snap +1st Order Screen`**, and **`4. White Light Dispersion`**.
- **Polychromatic Single-Color Channel Filter**:
  - Isolate individual spectral color bands (**Violet**, **Blue**, **Cyan**, **Green**, **Yellow**, **Orange**, **Red**) from White Light or Mercury/Sodium/Hydrogen/Cadmium emission lamps to observe how diffraction angle $\theta$ shifts with wavelength $\lambda$.

### 3. 5-Step Screen Fringe Wavelength Calculator (`Observations` Tab)
Solves the diffraction grating equation from screen distance $L$ and fringe separation $x$ or $s_n$:
1. **Grating Spacing**: $d = \frac{1}{N_{\text{lines/mm}}} = \frac{2.54\text{ cm}}{N_{\text{LPI}}}$
2. **Diffraction Angle**: $\theta = \arctan\left(\frac{s_n}{2L}\right) = \arctan\left(\frac{x}{L}\right)$
3. **Wavelength**: $\lambda = \frac{d \sin\theta}{n}$
- Includes presets for **80 lines/mm (Physics Online Laser Lab)**, **300 lines/mm (IIT-Bombay He-Ne Lab)**, **600 lines/mm (Thorlabs / Edmund Optics)**, and **15,000 LPI (Standard Spectrometer Grating)**.

### 4. Photonic Grating CAD & PH3001(P) Companion Simulators (`More Labs` Tab)
- **CAD 1 — GD-Calc® & Ansys Lumerical Sub-Wavelength Model (LSWM) / Stanford S4 Studio**:
  - Simulate **1D PMMA Pillars on MgO Substrate (S4 Tutorial P3)**, **Blazed Sawtooth Echelle Gratings (Lumerical PSO)**, **Binary Phase Gratings**, and **Sub-Wavelength Zero-Order Metamaterials ($\Lambda < \lambda / n_{\text{sub}}$)**.
  - Live diffraction efficiency ($\eta_m$ for $m \in [-3, +3]$) bar chart, Rytov effective medium index ($n_{\text{eff,TE}}, n_{\text{eff,TM}}$), **1-Tap Particle Swarm Optimization (PSO)**, and **Zemax OpticStudio JSON / Stanford S4 Lua Script Export**.
- **CAD 2 — OghmaNano DFB Bragg Grating Transfer Matrix Method (TMM)**:
  - Simulate Distributed Feedback (DFB) Bragg gratings ($\lambda_B = 2 n_{\text{eff}} \Lambda_B$) with adjustable index modulation $\Delta n$, quarter-wave ($\lambda/4$) phase shift defect, and **fabrication pitch uncertainty ($\sigma_\Lambda$)**, complete with live Reflectance $R(\lambda)$ / Transmittance $T(\lambda)$ stopband spectra and **OghmaNano Lua script generation**.
- **PH3001(P) Applied Physics Lab Modules**:
  - **Exp 4**: Newton's Rings Traveling Microscope Simulator ($D_{n+p}^2 - D_n^2 = 4pR\lambda/\mu$).
  - **Exp 3**: Optical Fiber Numerical Aperture ($\text{NA} = \sin\theta_{\max} = \frac{x}{\sqrt{r^2 + x^2}}$).
  - **Exp 6**: Planck's Constant via LED Knee Voltage ($e V_0 = h c / \lambda$).
  - **Exp 0**: Micrometer Screw Gauge & Vernier Calliper Least Count Trainer.

### 5. Embedded Lab Manual PDFs, Interactive Flowchart & Viva Quiz (`Theory & Viva` Tab)
- **Built-in Native PDF Viewer**: Switch between the focused 11-page **Experiment 5: Wavelength Using Grating** handout and the full 113-page **Applied Physics Lab Manual (PH3001P)**.
- **Interactive Exp. 5 Flowchart**: Tap any alignment step ($0^\circ \rightarrow 90^\circ \rightarrow 45^\circ/135^\circ \rightarrow V_1/V_2$) to execute it directly on the 3D Spectrometer.
- **11-Question Viva-Voce Quiz & Optical Simulation Software Directory**.

---

## 🛠️ Tech Stack & Architecture
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3, Canvas 2D/3D projective rendering, adaptive `BoxWithConstraints` layouts)
- **Architecture**: MVVM (`DiffractionLabViewModel` + immutable `StateFlow<DiffractionLabUiState>`)
- **PDF Rendering**: Android native `PdfRenderer`

---

## 🚀 Building & Exporting to GitHub
1. Open the project in **Android Studio** or build from the command line:
   ```bash
   ./gradlew assembleDebug
   ```
2. To push directly from **Google AI Studio**:
   - Click the **GitHub / Export** icon in the top-right toolbar of AI Studio to connect your repository and push the project with this `README.md`.
