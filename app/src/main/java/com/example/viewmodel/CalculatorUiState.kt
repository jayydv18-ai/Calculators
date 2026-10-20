package com.example.viewmodel

import com.example.data.model.CalculationHistoryItem
import com.example.domain.model.AngleMode
import com.example.domain.model.CalculatorOperator

data class CalculatorUiState(
    val displayValue: String = "0",
    val expressionText: String = "",
    val pendingOperator: CalculatorOperator? = null,
    val isAllClear: Boolean = true,
    val angleMode: AngleMode = AngleMode.DEGREE,
    val isScientificExpanded: Boolean = false,
    val isInverseMode: Boolean = false,
    val hasMemory: Boolean = false,
    val isError: Boolean = false,
    val isHistoryVisible: Boolean = false,
    val historyItems: List<CalculationHistoryItem> = emptyList()
)
