package com.example.ui.screens

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.AngleMode
import com.example.domain.model.CalculatorAction
import com.example.domain.model.CalculatorOperator
import com.example.domain.model.ConstantType
import com.example.domain.model.ScientificUnaryType
import com.example.ui.components.ButtonKind
import com.example.ui.components.CalculatorButton
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.HistoryBottomSheet
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.IosOrange
import com.example.ui.theme.LightBackground
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.viewmodel.CalculatorUiState
import com.example.viewmodel.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isDark = isSystemInDarkTheme()
    val isScientific = isLandscape || uiState.isScientificExpanded

    val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (uiState.isHistoryVisible) {
        HistoryBottomSheet(
            historyItems = uiState.historyItems,
            sheetState = historySheetState,
            onDismiss = { viewModel.onAction(CalculatorAction.CloseHistory) },
            onSelectItem = { item -> viewModel.onAction(CalculatorAction.SelectHistoryItem(item)) },
            onClearHistory = { viewModel.onAction(CalculatorAction.ClearHistory) }
        )
    }

    // Handle back button when scientific manual mode is expanded
    BackHandler(enabled = uiState.isScientificExpanded && !isLandscape) {
        viewModel.toggleScientificMode()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) DarkBackground else LightBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            TopBar(
                isScientific = isScientific,
                isLandscape = isLandscape,
                isManualExpanded = uiState.isScientificExpanded,
                onOpenHistory = { viewModel.onAction(CalculatorAction.OpenHistory) },
                onToggleScientific = { viewModel.onAction(CalculatorAction.ToggleScientificMode) }
            )

            // Live Display Box
            CalculatorDisplay(
                displayValue = uiState.displayValue,
                expressionText = uiState.expressionText,
                angleMode = uiState.angleMode,
                hasMemory = uiState.hasMemory,
                isScientific = isScientific,
                onBackspace = { viewModel.onAction(CalculatorAction.Backspace) },
                onOpenHistory = { viewModel.onAction(CalculatorAction.OpenHistory) },
                modifier = Modifier.weight(1f)
            )

            // Responsive Keypad Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (isLandscape) {
                    LandscapeScientificKeypad(
                        uiState = uiState,
                        onAction = viewModel::onAction
                    )
                } else {
                    PortraitKeypad(
                        uiState = uiState,
                        isScientificExpanded = uiState.isScientificExpanded,
                        onAction = viewModel::onAction
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    isScientific: Boolean,
    isLandscape: Boolean,
    isManualExpanded: Boolean,
    onOpenHistory: () -> Unit,
    onToggleScientific: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val textSecondary = if (isDark) DarkTextSecondary else LightTextSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onOpenHistory,
            modifier = Modifier.testTag("btn_open_history")
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = stringResource(R.string.cd_history),
                tint = textSecondary,
                modifier = Modifier.size(26.dp)
            )
        }

        if (!isLandscape) {
            IconButton(
                onClick = onToggleScientific,
                modifier = Modifier.testTag("btn_toggle_scientific")
            ) {
                Icon(
                    imageVector = if (isManualExpanded) Icons.Default.Calculate else Icons.Default.Functions,
                    contentDescription = stringResource(R.string.cd_toggle_scientific),
                    tint = if (isManualExpanded) IosOrange else textSecondary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun PortraitKeypad(
    uiState: CalculatorUiState,
    isScientificExpanded: Boolean,
    onAction: (CalculatorAction) -> Unit
) {
    val clearLabel = if (uiState.isAllClear) "AC" else "C"

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Expandable Scientific Drawer for Portrait Mode
        AnimatedVisibility(
            visible = isScientificExpanded,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Scientific Row 1: 2nd, Rad/Deg, sin, cos, tan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalculatorButton(
                        text = "2ⁿᵈ",
                        onClick = { onAction(CalculatorAction.ToggleInverseMode) },
                        kind = ButtonKind.SCIENTIFIC,
                        fontSize = 18.sp,
                        isActiveOperator = uiState.isInverseMode,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = if (uiState.angleMode == AngleMode.DEGREE) "Rad" else "Deg",
                        onClick = { onAction(CalculatorAction.ToggleAngleMode) },
                        kind = ButtonKind.SCIENTIFIC,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = if (uiState.isInverseMode) "sin⁻¹" else "sin",
                        onClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.ASIN else ScientificUnaryType.SIN)) },
                        onLongClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.SIN else ScientificUnaryType.ASIN)) },
                        kind = ButtonKind.SCIENTIFIC,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = if (uiState.isInverseMode) "cos⁻¹" else "cos",
                        onClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.ACOS else ScientificUnaryType.COS)) },
                        onLongClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.COS else ScientificUnaryType.ACOS)) },
                        kind = ButtonKind.SCIENTIFIC,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = if (uiState.isInverseMode) "tan⁻¹" else "tan",
                        onClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.ATAN else ScientificUnaryType.TAN)) },
                        onLongClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.TAN else ScientificUnaryType.ATAN)) },
                        kind = ButtonKind.SCIENTIFIC,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Scientific Row 2: ln/log2, log10, x^y, √, x!
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalculatorButton(
                        text = if (uiState.isInverseMode) "log₂" else "ln",
                        onClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.LOG2 else ScientificUnaryType.LN)) },
                        onLongClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.LN else ScientificUnaryType.LOG2)) },
                        kind = ButtonKind.SCIENTIFIC,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = "log",
                        onClick = { onAction(CalculatorAction.ScientificUnary(ScientificUnaryType.LOG10)) },
                        kind = ButtonKind.SCIENTIFIC,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = "xʸ",
                        onClick = { onAction(CalculatorAction.ScientificBinary(CalculatorOperator.POWER)) },
                        kind = ButtonKind.SCIENTIFIC,
                        fontSize = 18.sp,
                        isActiveOperator = uiState.pendingOperator == CalculatorOperator.POWER,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = if (uiState.isInverseMode) "∛" else "√",
                        onClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.CBRT else ScientificUnaryType.SQRT)) },
                        onLongClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.SQRT else ScientificUnaryType.CBRT)) },
                        kind = ButtonKind.SCIENTIFIC,
                        fontSize = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = "x!",
                        onClick = { onAction(CalculatorAction.ScientificUnary(ScientificUnaryType.FACTORIAL)) },
                        kind = ButtonKind.SCIENTIFIC,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Scientific Row 3: (, ), π, e, 1/x
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalculatorButton(
                        text = "(",
                        onClick = { onAction(CalculatorAction.OpenParenthesis) },
                        kind = ButtonKind.SCIENTIFIC,
                        fontSize = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = ")",
                        onClick = { onAction(CalculatorAction.CloseParenthesis) },
                        kind = ButtonKind.SCIENTIFIC,
                        fontSize = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = "π",
                        onClick = { onAction(CalculatorAction.Constant(ConstantType.PI)) },
                        kind = ButtonKind.SCIENTIFIC,
                        fontSize = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = "e",
                        onClick = { onAction(CalculatorAction.Constant(ConstantType.E)) },
                        kind = ButtonKind.SCIENTIFIC,
                        fontSize = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                    CalculatorButton(
                        text = "1/x",
                        onClick = { onAction(CalculatorAction.ScientificUnary(ScientificUnaryType.RECIPROCAL)) },
                        kind = ButtonKind.SCIENTIFIC,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Standard Portrait 4x5 Grid
        // Row 1: AC/C, +/-, %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalculatorButton(
                text = clearLabel,
                onClick = { onAction(CalculatorAction.Clear) },
                kind = ButtonKind.FUNCTION,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "±",
                onClick = { onAction(CalculatorAction.ToggleSign) },
                kind = ButtonKind.FUNCTION,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "%",
                onClick = { onAction(CalculatorAction.Percent) },
                kind = ButtonKind.FUNCTION,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "÷",
                onClick = { onAction(CalculatorAction.Operator(CalculatorOperator.DIVIDE)) },
                kind = ButtonKind.OPERATOR,
                isActiveOperator = uiState.pendingOperator == CalculatorOperator.DIVIDE,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalculatorButton(
                text = "7",
                onClick = { onAction(CalculatorAction.Digit(7)) },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "8",
                onClick = { onAction(CalculatorAction.Digit(8)) },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "9",
                onClick = { onAction(CalculatorAction.Digit(9)) },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "×",
                onClick = { onAction(CalculatorAction.Operator(CalculatorOperator.MULTIPLY)) },
                kind = ButtonKind.OPERATOR,
                isActiveOperator = uiState.pendingOperator == CalculatorOperator.MULTIPLY,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalculatorButton(
                text = "4",
                onClick = { onAction(CalculatorAction.Digit(4)) },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "5",
                onClick = { onAction(CalculatorAction.Digit(5)) },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "6",
                onClick = { onAction(CalculatorAction.Digit(6)) },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "−",
                onClick = { onAction(CalculatorAction.Operator(CalculatorOperator.SUBTRACT)) },
                kind = ButtonKind.OPERATOR,
                isActiveOperator = uiState.pendingOperator == CalculatorOperator.SUBTRACT,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalculatorButton(
                text = "1",
                onClick = { onAction(CalculatorAction.Digit(1)) },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "2",
                onClick = { onAction(CalculatorAction.Digit(2)) },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "3",
                onClick = { onAction(CalculatorAction.Digit(3)) },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "+",
                onClick = { onAction(CalculatorAction.Operator(CalculatorOperator.ADD)) },
                kind = ButtonKind.OPERATOR,
                isActiveOperator = uiState.pendingOperator == CalculatorOperator.ADD,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 5: 0 (double span), ., =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalculatorButton(
                text = "0",
                onClick = { onAction(CalculatorAction.Digit(0)) },
                aspectRatio = 2.1f,
                shape = RoundedCornerShape(percent = 50),
                modifier = Modifier.weight(2.05f)
            )
            CalculatorButton(
                text = ".",
                onClick = { onAction(CalculatorAction.Decimal) },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "=",
                onClick = { onAction(CalculatorAction.Calculate) },
                kind = ButtonKind.OPERATOR,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LandscapeScientificKeypad(
    uiState: CalculatorUiState,
    onAction: (CalculatorAction) -> Unit
) {
    val clearLabel = if (uiState.isAllClear) "AC" else "C"

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: (, ), mc, m+, m-, mr, C/AC, +/-, %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorButton(text = "(", onClick = { onAction(CalculatorAction.OpenParenthesis) }, kind = ButtonKind.SCIENTIFIC, fontSize = 16.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = ")", onClick = { onAction(CalculatorAction.CloseParenthesis) }, kind = ButtonKind.SCIENTIFIC, fontSize = 16.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "mc", onClick = { onAction(CalculatorAction.MemoryClear) }, kind = ButtonKind.SCIENTIFIC, fontSize = 15.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "m+", onClick = { onAction(CalculatorAction.MemoryAdd) }, kind = ButtonKind.SCIENTIFIC, fontSize = 15.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "m-", onClick = { onAction(CalculatorAction.MemorySubtract) }, kind = ButtonKind.SCIENTIFIC, fontSize = 15.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "mr", onClick = { onAction(CalculatorAction.MemoryRecall) }, kind = ButtonKind.SCIENTIFIC, fontSize = 15.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = clearLabel, onClick = { onAction(CalculatorAction.Clear) }, kind = ButtonKind.FUNCTION, fontSize = 16.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "±", onClick = { onAction(CalculatorAction.ToggleSign) }, kind = ButtonKind.FUNCTION, fontSize = 18.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "%", onClick = { onAction(CalculatorAction.Percent) }, kind = ButtonKind.FUNCTION, fontSize = 16.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "÷", onClick = { onAction(CalculatorAction.Operator(CalculatorOperator.DIVIDE)) }, kind = ButtonKind.OPERATOR, fontSize = 20.sp, isActiveOperator = uiState.pendingOperator == CalculatorOperator.DIVIDE, modifier = Modifier.weight(1f))
        }

        // Row 2: 2nd, x², x³, x^y, e^x, 10^x, 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorButton(text = "2ⁿᵈ", onClick = { onAction(CalculatorAction.ToggleInverseMode) }, kind = ButtonKind.SCIENTIFIC, fontSize = 15.sp, isActiveOperator = uiState.isInverseMode, modifier = Modifier.weight(1f))
            CalculatorButton(text = if (uiState.isInverseMode) "x³" else "x²", onClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.CUBE else ScientificUnaryType.SQUARE)) }, onLongClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.SQUARE else ScientificUnaryType.CUBE)) }, kind = ButtonKind.SCIENTIFIC, fontSize = 15.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = if (uiState.isInverseMode) "x²" else "x³", onClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.SQUARE else ScientificUnaryType.CUBE)) }, kind = ButtonKind.SCIENTIFIC, fontSize = 15.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "xʸ", onClick = { onAction(CalculatorAction.ScientificBinary(CalculatorOperator.POWER)) }, kind = ButtonKind.SCIENTIFIC, fontSize = 15.sp, isActiveOperator = uiState.pendingOperator == CalculatorOperator.POWER, modifier = Modifier.weight(1f))
            CalculatorButton(text = if (uiState.isInverseMode) "yˣ" else "eˣ", onClick = { onAction(CalculatorAction.ScientificUnary(ScientificUnaryType.EXP)) }, kind = ButtonKind.SCIENTIFIC, fontSize = 15.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = if (uiState.isInverseMode) "2ˣ" else "10ˣ", onClick = { onAction(CalculatorAction.ScientificUnary(ScientificUnaryType.EXP10)) }, kind = ButtonKind.SCIENTIFIC, fontSize = 15.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "7", onClick = { onAction(CalculatorAction.Digit(7)) }, fontSize = 18.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "8", onClick = { onAction(CalculatorAction.Digit(8)) }, fontSize = 18.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "9", onClick = { onAction(CalculatorAction.Digit(9)) }, fontSize = 18.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "×", onClick = { onAction(CalculatorAction.Operator(CalculatorOperator.MULTIPLY)) }, kind = ButtonKind.OPERATOR, fontSize = 20.sp, isActiveOperator = uiState.pendingOperator == CalculatorOperator.MULTIPLY, modifier = Modifier.weight(1f))
        }

        // Row 3: 1/x, √, ∛, x!, ln, log10, 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorButton(text = "1/x", onClick = { onAction(CalculatorAction.ScientificUnary(ScientificUnaryType.RECIPROCAL)) }, kind = ButtonKind.SCIENTIFIC, fontSize = 15.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = if (uiState.isInverseMode) "∛" else "√", onClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.CBRT else ScientificUnaryType.SQRT)) }, onLongClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.SQRT else ScientificUnaryType.CBRT)) }, kind = ButtonKind.SCIENTIFIC, fontSize = 16.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "∛", onClick = { onAction(CalculatorAction.ScientificUnary(ScientificUnaryType.CBRT)) }, kind = ButtonKind.SCIENTIFIC, fontSize = 16.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "x!", onClick = { onAction(CalculatorAction.ScientificUnary(ScientificUnaryType.FACTORIAL)) }, kind = ButtonKind.SCIENTIFIC, fontSize = 15.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = if (uiState.isInverseMode) "log₂" else "ln", onClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.LOG2 else ScientificUnaryType.LN)) }, onLongClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.LN else ScientificUnaryType.LOG2)) }, kind = ButtonKind.SCIENTIFIC, fontSize = 15.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "log", onClick = { onAction(CalculatorAction.ScientificUnary(ScientificUnaryType.LOG10)) }, kind = ButtonKind.SCIENTIFIC, fontSize = 15.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "4", onClick = { onAction(CalculatorAction.Digit(4)) }, fontSize = 18.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "5", onClick = { onAction(CalculatorAction.Digit(5)) }, fontSize = 18.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "6", onClick = { onAction(CalculatorAction.Digit(6)) }, fontSize = 18.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "−", onClick = { onAction(CalculatorAction.Operator(CalculatorOperator.SUBTRACT)) }, kind = ButtonKind.OPERATOR, fontSize = 20.sp, isActiveOperator = uiState.pendingOperator == CalculatorOperator.SUBTRACT, modifier = Modifier.weight(1f))
        }

        // Row 4: sin, cos, tan, sinh, cosh, tanh, 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorButton(text = if (uiState.isInverseMode) "sin⁻¹" else "sin", onClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.ASIN else ScientificUnaryType.SIN)) }, onLongClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.SIN else ScientificUnaryType.ASIN)) }, kind = ButtonKind.SCIENTIFIC, fontSize = 14.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = if (uiState.isInverseMode) "cos⁻¹" else "cos", onClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.ACOS else ScientificUnaryType.COS)) }, onLongClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.COS else ScientificUnaryType.ACOS)) }, kind = ButtonKind.SCIENTIFIC, fontSize = 14.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = if (uiState.isInverseMode) "tan⁻¹" else "tan", onClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.ATAN else ScientificUnaryType.TAN)) }, onLongClick = { onAction(CalculatorAction.ScientificUnary(if (uiState.isInverseMode) ScientificUnaryType.TAN else ScientificUnaryType.ATAN)) }, kind = ButtonKind.SCIENTIFIC, fontSize = 14.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "sinh", onClick = { onAction(CalculatorAction.ScientificUnary(ScientificUnaryType.SINH)) }, kind = ButtonKind.SCIENTIFIC, fontSize = 14.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "cosh", onClick = { onAction(CalculatorAction.ScientificUnary(ScientificUnaryType.COSH)) }, kind = ButtonKind.SCIENTIFIC, fontSize = 14.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "tanh", onClick = { onAction(CalculatorAction.ScientificUnary(ScientificUnaryType.TANH)) }, kind = ButtonKind.SCIENTIFIC, fontSize = 14.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "1", onClick = { onAction(CalculatorAction.Digit(1)) }, fontSize = 18.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "2", onClick = { onAction(CalculatorAction.Digit(2)) }, fontSize = 18.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "3", onClick = { onAction(CalculatorAction.Digit(3)) }, fontSize = 18.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "+", onClick = { onAction(CalculatorAction.Operator(CalculatorOperator.ADD)) }, kind = ButtonKind.OPERATOR, fontSize = 20.sp, isActiveOperator = uiState.pendingOperator == CalculatorOperator.ADD, modifier = Modifier.weight(1f))
        }

        // Row 5: Rad/Deg, π, e, EE, Rand, 0 (double span), ., =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorButton(text = if (uiState.angleMode == AngleMode.DEGREE) "Rad" else "Deg", onClick = { onAction(CalculatorAction.ToggleAngleMode) }, kind = ButtonKind.SCIENTIFIC, fontSize = 14.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "π", onClick = { onAction(CalculatorAction.Constant(ConstantType.PI)) }, kind = ButtonKind.SCIENTIFIC, fontSize = 16.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "e", onClick = { onAction(CalculatorAction.Constant(ConstantType.E)) }, kind = ButtonKind.SCIENTIFIC, fontSize = 16.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "EE", onClick = { onAction(CalculatorAction.EE) }, kind = ButtonKind.SCIENTIFIC, fontSize = 14.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "Rand", onClick = { onAction(CalculatorAction.Constant(ConstantType.RAND)) }, kind = ButtonKind.SCIENTIFIC, fontSize = 13.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "0", onClick = { onAction(CalculatorAction.Digit(0)) }, shape = RoundedCornerShape(percent = 50), aspectRatio = 2f, fontSize = 18.sp, modifier = Modifier.weight(2f))
            CalculatorButton(text = ".", onClick = { onAction(CalculatorAction.Decimal) }, fontSize = 20.sp, modifier = Modifier.weight(1f))
            CalculatorButton(text = "=", onClick = { onAction(CalculatorAction.Calculate) }, kind = ButtonKind.OPERATOR, fontSize = 20.sp, modifier = Modifier.weight(1f))
        }
    }
}
