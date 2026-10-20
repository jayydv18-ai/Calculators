package com.example.domain.model

import com.example.data.model.CalculationHistoryItem

sealed interface CalculatorAction {
    data class Digit(val digit: Int) : CalculatorAction
    data object Decimal : CalculatorAction
    data class Operator(val op: CalculatorOperator) : CalculatorAction
    data object Calculate : CalculatorAction
    data object Clear : CalculatorAction
    data object AllClear : CalculatorAction
    data object ToggleSign : CalculatorAction
    data object Percent : CalculatorAction
    
    // Scientific functions
    data class ScientificUnary(val type: ScientificUnaryType) : CalculatorAction
    data class Constant(val type: ConstantType) : CalculatorAction
    data class ScientificBinary(val op: CalculatorOperator) : CalculatorAction
    data object OpenParenthesis : CalculatorAction
    data object CloseParenthesis : CalculatorAction
    data object EE : CalculatorAction
    
    // Memory keys
    data object MemoryClear : CalculatorAction
    data object MemoryAdd : CalculatorAction
    data object MemorySubtract : CalculatorAction
    data object MemoryRecall : CalculatorAction
    
    // Mode toggles
    data object ToggleAngleMode : CalculatorAction
    data object ToggleScientificMode : CalculatorAction
    data object ToggleInverseMode : CalculatorAction
    data object Backspace : CalculatorAction
    
    // History actions
    data object OpenHistory : CalculatorAction
    data object CloseHistory : CalculatorAction
    data class SelectHistoryItem(val item: CalculationHistoryItem) : CalculatorAction
    data object ClearHistory : CalculatorAction
}
