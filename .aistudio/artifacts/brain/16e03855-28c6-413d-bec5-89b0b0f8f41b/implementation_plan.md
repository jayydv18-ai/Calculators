# iPhone-Inspired Scientific & Basic Calculator

An authentic, production-grade Android calculator app modeled on the iconic iOS calculator experience, delivering precision arithmetic via BigDecimal, responsive basic and scientific modes, dark glassmorphism and minimal light themes, tactile feedback, and calculation history.

### User Review & Critical Decisions

> [!IMPORTANT]
> The architectural and design decisions below align with your requirements and confirmed preferences:

- **Scientific Secondary Functions**: Long-press on scientific trigonometric and power keys (e.g., `sin`, `cos`, `tan`, `ln`, `eˣ`) directly reveals and triggers inverse functions (`sin⁻¹`, `cos⁻¹`, `tan⁻¹`, `log₂`, `yˣ`), supported also by an optional `2nd` mode toggle key for fast single-tap switching.
- **Tactile & Audio Feedback**: Subtle haptic vibration triggered on button press using Android's `HapticFeedbackConstants` along with spring-based scale compression animations on button touch.
- **History Access**: Dual entry points—a clock icon in the top utility bar and a downward swipe gesture directly across the calculation display—revealing a Material 3 modal bottom sheet with the last 50 saved calculations.
- **Arithmetic Precision**: All operations use arbitrary-precision `BigDecimal` and `MathContext.DECIMAL128` (34 decimal digits of precision) to prevent floating-point inaccuracies (e.g., ensuring `0.1 + 0.2 = 0.3`).

---

### 1. Overview & Core Concept

- **What It Does**: A dual-mode calculator providing basic arithmetic in portrait mode and an expanded 30+ key scientific workbench in landscape orientation (or via manual toggle). It incorporates gestures (swipe display to backspace, long-press to copy), memory registers (`mc`, `m+`, `m-`, `mr`), angle unit switching (`Rad` / `Deg`), and local persistent history.
- **Target Audience / Persona**: Everyday mobile users wanting the muscle memory and elegance of the iOS calculator, alongside students, scientists, and engineers requiring dependable trigonometry, logarithms, powers, factorials, and constants.
- **Key Value**: Delivers the tactile polish and intuitive operator behavior of iOS with the power of Android edge-to-edge Compose rendering and zero floating-point rounding quirks.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Basic Calculation**: User enters digits, taps an operator (which highlights in inverted white-on-orange while pending), enters the second operand, and taps `=` to compute the result. The secondary expression breadcrumb updates live above the main digits.
2. **Repeated Equals & Chaining**: Tapping `=` repeatedly replays the last operator and operand (e.g., `5 + 2 = 7 = 9 = 11`), replicating iOS calculator behavior.
3. **Gesture Editing**: User mistypes a number and swipes left or right across the main display digits to delete the trailing digit. Long-pressing the result copies the formatted value to the system clipboard with a toast notification.
4. **Scientific Computing**: Rotating to landscape or tapping the scientific icon unfolds a smooth grid expansion with logarithmic, trigonometric, hyperbolic, factorial, and power functions. Pressing `Rad`/`Deg` toggles an unobtrusive badge in the display.
5. **History Inspection & Reuse**: Swiping down on the display or tapping the history icon slides up a glassmorphism bottom sheet displaying past calculation equations and timestamps. Tapping any item reloads its result or equation into the live editor.

#### Visual Identity & Theme
- **Aesthetic Direction**: High-end iOS minimalism with modern Android glassmorphism depth.
- **Dark Theme (Default)**: Deep obsidian backdrop (`#000000` to `#0B0B0E`), frosted translucent card surfaces (`rgba(255, 255, 255, 0.08)` with subtle `1.dp` light borders), deep charcoal number keys (`#2C2C2E`), light-gray utility keys (`#505054`), and vivid iOS orange accent operators (`#FF9F0A`).
- **Light Theme**: High-contrast crisp paper aesthetic (`#F6F6F8`), soft mist gray keys (`#E5E5EA`), cool silver utility keys (`#D1D1D6`), and deep amber-orange operators (`#F18701`).
- **Typography & Hierarchy**: Clean, tabular typography with dynamic font auto-shrinking (starting at 64sp down to 28sp as digit count reaches 9-10 digits) so calculations never wrap or clip.
- **Active Operator State**: When an operator (`+`, `−`, `×`, `÷`) is selected, its button inverts to a white background with orange glyph text until the next digit or command is pressed.

#### Interactive Feedback & Motion
- **Press Animation**: Interactive scale physics (compressing to 92% on press and springing back with `spring(dampingRatio = 0.6f)`) paired with system haptic ticks.
- **Mode Switching**: Staggered layout animated transition between basic portrait layout and expanded landscape layout using `AnimatedContent` and Compose grid animations.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Custom Expression & State Engine vs. External Parser Library**
  - *Chosen Approach*: Handcrafted, zero-dependency tokenizer and Shunting-Yard evaluator combined with an iOS-style state machine (tracking accumulator, pending operator, input buffer, last evaluation, and error state).
  - *Why*: Satisfies prompt rules against third-party calculator libraries, guarantees exact iPhone operator behaviors (repeated `=`, immediate percentage transformation based on context, and unary negation), and avoids external dependency bloat.
  - *Alternatives Considered*: Android ExpressionEvaluator or mXparser (rejected due to third-party restriction and lack of exact iOS state machine semantics).

- **Decision 2: BigDecimal with High-Precision MathContext**
  - *Chosen Approach*: `BigDecimal` for standard four-function arithmetic, percentage, and memory registers, paired with high-precision approximations for transcendental functions (`sin`, `cos`, `tan`, `ln`, `eˣ`).
  - *Why*: Eliminates IEEE-754 binary floating point errors (`0.1 + 0.2 = 0.30000000000000004`), ensuring clean decimal display without awkward rounding artifacts.
  - *Alternatives Considered*: Standard Kotlin `Double` (rejected due to decimal precision flaws).

- **Decision 3: Local Storage with Jetpack DataStore Preferences**
  - *Chosen Approach*: Serialized JSON array of the last 50 calculation history entries persisted asynchronously via `androidx.datastore.preferences`.
  - *Why*: Non-blocking, crash-safe, coroutine-native, lightweight, and requires no heavy SQL migrations.
  - *Alternatives Considered*: Room Database (excessive overhead for an unindexed 50-entry queue).

---

### 4. Technical Architecture & Data Strategy *(Technical Reference)*

#### Architecture & Component Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                              MainActivity                              │
│         (Edge-to-Edge Insets, Orientation & Process State Sync)        │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                           CalculatorScreen                             │
│ ┌────────────────────────────────────────────────────────────────────┐ │
│ │ Top Utility Bar (History Toggle, Mode Toggle, Rad/Deg Indicator)   │ │
│ ├────────────────────────────────────────────────────────────────────┤ │
│ │ Live Display Box (Expression Breadcrumb, Scaled Digits, Gestures)  │ │
│ ├────────────────────────────────────────────────────────────────────┤ │
│ │ Keypad Grid (Portrait 4x5 Basic / Landscape 10x5 Scientific)       │ │
│ ├────────────────────────────────────────────────────────────────────┤ │
│ │ History Bottom Sheet (LazyColumn of 50 Calculations, Tap to Reuse) │ │
│ └────────────────────────────────────────────────────────────────────┘ │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ (UI Events & StateFlow)
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                         CalculatorViewModel                            │
│  - StateFlow<CalculatorUiState>                                        │
│  - On-Action Dispatcher (Digit, Operator, Equals, Clear, Scientific)   │
│  - SavedStateHandle (Process Death & Rotation Restoration)             │
└───────────────┬────────────────────────────────────────┬───────────────┘
                │                                        │
                ▼                                        ▼
┌────────────────────────────────┐     ┌─────────────────────────────────┐
│     CalculatorEngine (Domain)   │     │   HistoryRepository (Data)      │
│  - Shunting-Yard Parser        │     │  - DataStore Preferences        │
│  - BigDecimal Arithmetic       │     │  - Last 50 Calculations Queue   │
│  - Scientific Transcendental   │     │  - Flow<List<HistoryItem>>      │
│  - Number Formatter (SIP/Sci)  │     │  - Clear & Append Operations    │
└────────────────────────────────┘     └─────────────────────────────────┘
```

#### State & Data Models
- **`CalculatorUiState`**:
  - `displayValue: String` (Primary formatted number on display, e.g., "1,234.5")
  - `expressionText: String` (Breadcrumb of current chain, e.g., "12 × 5 +")
  - `pendingOperator: CalculatorOperator?` (Highlighted active operator: `ADD`, `SUB`, `MUL`, `DIV`, `POW`)
  - `isAllClear: Boolean` (`true` displays "AC", `false` displays "C")
  - `angleMode: AngleMode` (`DEGREE` vs `RADIAN`)
  - `isScientificExpanded: Boolean` (Manual toggle override)
  - `isInverseMode: Boolean` (For secondary scientific keys)
  - `memoryValue: BigDecimal` (Current memory register)
  - `hasMemory: Boolean` (Indicator for `mr` key)
  - `isError: Boolean` (Displays "Error" when division by zero or domain exception occurs)
  - `historyItems: List<CalculationHistoryItem>` (Latest 50 entries)
  - `isHistoryVisible: Boolean` (Modal bottom sheet state)
- **`CalculationHistoryItem`**:
  - `id: String`, `expression: String`, `result: String`, `timestamp: Long`

#### Key Interactive Component & State Mapping
- **Digit Keys (`0`–`9`, `.`)**: Appends to buffer, converts `AC` to `C`, resets active operator highlight.
- **Primary Operators (`+`, `−`, `×`, `÷`)**: Commits current buffer, triggers calculation if chain is pending, and sets persistent active highlight until next input.
- **Equals (`=`)**: Evaluates expression, handles repeated `=` behavior, formats result with thousand-separators, and records entry into DataStore history.
- **Clear (`AC` / `C`)**: Single tap resets current buffer; subsequent tap clears entire accumulator, pending operator, and expression line.
- **Display Gestures**: Horizontal drag triggers single-digit backspace; long-press triggers clipboard copy with feedback; downward drag reveals history sheet.
- **Memory Keys (`mc`, `m+`, `m-`, `mr`)**: Updates or recalls in-memory BigDecimal register with visual state indicator.
