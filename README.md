# iPhone Calculator for Android 📱

A production-quality Android calculator application modeled on the iconic iPhone calculator experience, built with modern **Kotlin**, **Jetpack Compose**, and **Material 3**.

---

## ✨ Features

### 1. Dual Calculation Modes
- **Basic Mode (Portrait)**:
  - Standard operations: Addition ($+$), Subtraction ($-$), Multiplication ($\times$), Division ($\div$).
  - Clean `AC` / `C` clear logic.
  - Sign toggling ($\pm$) and iOS-authentic percentage calculation (context-aware for $+$, $-$, $\times$, $\div$).
  - Zero-error decimal arithmetic powered by `BigDecimal` ($0.1 + 0.2 = 0.3$).
- **Scientific Mode (Landscape & Manual Toggle)**:
  - **Trigonometry**: $\sin$, $\cos$, $\tan$, and inverses $\sin^{-1}$, $\cos^{-1}$, $\tan^{-1}$.
  - **Hyperbolic**: $\sinh$, $\cosh$, $\tanh$.
  - **Logarithms & Powers**: $\ln$, $\log_{10}$, $\log_2$, $e^x$, $10^x$, $x^2$, $x^3$, $x^y$.
  - **Roots & Factorial**: $\sqrt{x}$, $\sqrt[3]{x}$, $x!$, $\frac{1}{x}$.
  - **Constants**: $\pi$, $e$, $\text{Rand}$ (random decimal).
  - **Expression Controls**: Parentheses `(`, `)` with Dijkstra Shunting-Yard evaluation, scientific notation entry (`EE`).
  - **Angle Modes**: Toggle between Degrees (`DEG`) and Radians (`RAD`) with live badge indicator.
  - **Memory Registers**: `mc` (memory clear), `m+` (memory add), `m-` (memory subtract), `mr` (memory recall) with active indicator.
  - **Secondary Function Access**: Fast `2ⁿᵈ` toggle key or direct long-press on keys to access inverse operations.

### 2. Authentic iOS Design & UX Polish
- **Dark Glassmorphism Theme (Default)**: Deep obsidian background (`#000000`), frosted translucent glass layers, circular keys, and signature `#FF9F0A` orange accent operators.
- **Active Operator Highlight**: Selected operator button inverts to a white background with orange glyph until the next digit is typed.
- **Light Theme**: High-contrast, minimal aesthetic with mist-gray surfaces and vibrant accents.
- **Dynamic Text Shrinking**: Display text smoothly scales down from 64sp to 30sp as digit length grows, preventing clipping.
- **Tactile Feedback & Motion**: Spring-based compression scale animation on press paired with system haptic ticks (`HapticFeedbackConstants.KEYBOARD_TAP`).
- **Gesture Controls**:
  - **Swipe Left/Right** on the display to delete trailing digits.
  - **Long-Press** on the result to copy to the system clipboard.
  - **Swipe Down** or tap the top clock icon to reveal calculation history.

### 3. Calculation History
- Slide-up glassmorphic Material 3 modal bottom sheet.
- Automatically stores the last 50 completed calculations with timestamps.
- Tap any calculation to instantly reload its result into the active calculation.
- One-tap "Clear History" button.
- Asynchronously persisted across app restarts using **Jetpack DataStore Preferences**.

---

## 🛠️ Architecture & Tech Stack

```
com.example/
├── domain/
│   ├── engine/
│   │   └── CalculatorEngine.kt       # BigDecimal math, Shunting-Yard parser, formatter
│   └── model/
│       ├── AngleMode.kt              # DEG / RAD modes
│       ├── CalculatorAction.kt       # Unidirectional UI actions
│       ├── CalculatorOperator.kt     # Operator precedence and symbols
│       └── ScientificUnaryType.kt    # Trigonometric, logarithmic, and power functions
├── data/
│   ├── model/
│   │   └── CalculationHistoryItem.kt # History entity
│   └── repository/
│       └── HistoryRepository.kt      # DataStore asynchronous storage (up to 50 items)
├── viewmodel/
│   ├── CalculatorUiState.kt          # Immutable state model
│   └── CalculatorViewModel.kt        # State machine, SavedStateHandle restoration
└── ui/
    ├── components/
    │   ├── CalculatorButton.kt       # Tactile button with spring scale & haptics
    │   ├── CalculatorDisplay.kt      # Live expression, auto-shrinking text & gestures
    │   └── HistoryBottomSheet.kt     # Glassmorphic history sheet
    ├── screens/
    │   └── CalculatorScreen.kt       # Responsive portrait & landscape scientific layout
    └── theme/
        ├── Color.kt                  # iOS Dark & Light palettes
        ├── Theme.kt                  # Dynamic color and theme provider
        └── Type.kt                   # Typography styles
```

- **Language**: Kotlin 100%
- **UI Toolkit**: Jetpack Compose with Material Design 3 (M3)
- **Architecture**: MVVM with unidirectional data flow (`StateFlow`)
- **State Preservation**: `SavedStateHandle` survives screen rotation and system process death
- **Data Persistence**: AndroidX DataStore Preferences
- **Minimum SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 36

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (or newer)
- JDK 17 (Eclipse Temurin recommended)
- Gradle 9.3.1 (or use the built-in Android Studio Gradle sync)

### Running Locally
1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/<repo-name>.git
   cd <repo-name>
   ```
2. Open the project in **Android Studio**.
3. Let Gradle sync and index dependencies.
4. Select an emulator or connected physical Android device and click **Run** (`Shift + F10`).

### Running Tests
Execute the local unit and Robolectric test suite:
```bash
gradle :app:testDebugUnitTest
```

### Building the Debug APK
```bash
gradle :app:assembleDebug --stacktrace --no-daemon
```
The compiled APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🤖 Continuous Integration & GitHub Releases

The repository includes a ready-to-use GitHub Actions workflow (`.github/workflows/build-apk.yml`) that:
1. Automatically triggers on every push to `main` or manual `workflow_dispatch`.
2. Configures Temurin Java 17 and Gradle 9.3.1.
3. Generates a temporary debug keystore and compiles `app-debug.apk`.
4. Uploads the build artifact (`app-debug-apk`) to GitHub Actions.
5. Publishes a GitHub Release tagged `debug-apk-build-<run_number>-<attempt>` with the APK attached for direct mobile download.

---

## 📄 License
This project is open-source and available under the standard Apache 2.0 / MIT license.
