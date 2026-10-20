package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.data.model.CalculationHistoryItem
import com.example.data.repository.HistoryRepository
import com.example.domain.engine.CalculatorEngine
import com.example.domain.model.AngleMode
import com.example.domain.model.CalculatorAction
import com.example.domain.model.CalculatorOperator
import com.example.domain.model.ConstantType
import com.example.domain.model.ScientificUnaryType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Random

class CalculatorViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    constructor(application: Application) : this(application, SavedStateHandle())

    private val repository: HistoryRepository = HistoryRepository(application)

    private var rawBuffer: String
        get() = savedStateHandle["rawBuffer"] ?: "0"
        set(value) { savedStateHandle["rawBuffer"] = value }

    private var accumulator: BigDecimal?
        get() {
            val str: String? = savedStateHandle["accumulator"]
            return str?.let { try { BigDecimal(it) } catch (_: Exception) { null } }
        }
        set(value) { savedStateHandle["accumulator"] = value?.toString() }

    private var pendingOperator: CalculatorOperator?
        get() {
            val name: String? = savedStateHandle["pendingOperator"]
            return name?.let { try { CalculatorOperator.valueOf(it) } catch (_: Exception) { null } }
        }
        set(value) { savedStateHandle["pendingOperator"] = value?.name }

    private var lastOperator: CalculatorOperator?
        get() {
            val name: String? = savedStateHandle["lastOperator"]
            return name?.let { try { CalculatorOperator.valueOf(it) } catch (_: Exception) { null } }
        }
        set(value) { savedStateHandle["lastOperator"] = value?.name }

    private var lastOperand: BigDecimal?
        get() {
            val str: String? = savedStateHandle["lastOperand"]
            return str?.let { try { BigDecimal(it) } catch (_: Exception) { null } }
        }
        set(value) { savedStateHandle["lastOperand"] = value?.toString() }

    private var shouldResetInput: Boolean
        get() = savedStateHandle["shouldResetInput"] ?: false
        set(value) { savedStateHandle["shouldResetInput"] = value }

    private var memory: BigDecimal = BigDecimal.ZERO

    private val _uiState = MutableStateFlow(
        CalculatorUiState(
            displayValue = CalculatorEngine.formatInputBuffer(rawBuffer),
            pendingOperator = pendingOperator
        )
    )
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.historyFlow.collect { history ->
                _uiState.update { it.copy(historyItems = history) }
            }
        }
    }

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Digit -> handleDigit(action.digit)
            is CalculatorAction.Decimal -> handleDecimal()
            is CalculatorAction.Operator -> handleOperator(action.op)
            is CalculatorAction.Calculate -> handleCalculate()
            is CalculatorAction.Clear -> handleClear()
            is CalculatorAction.AllClear -> handleAllClear()
            is CalculatorAction.ToggleSign -> handleToggleSign()
            is CalculatorAction.Percent -> handlePercent()
            is CalculatorAction.Backspace -> handleBackspace()
            is CalculatorAction.ScientificUnary -> handleScientificUnary(action.type)
            is CalculatorAction.Constant -> handleConstant(action.type)
            is CalculatorAction.ScientificBinary -> handleOperator(action.op)
            is CalculatorAction.OpenParenthesis -> handleOpenParenthesis()
            is CalculatorAction.CloseParenthesis -> handleCloseParenthesis()
            is CalculatorAction.EE -> handleEE()
            is CalculatorAction.MemoryClear -> handleMemoryClear()
            is CalculatorAction.MemoryAdd -> handleMemoryAdd()
            is CalculatorAction.MemorySubtract -> handleMemorySubtract()
            is CalculatorAction.MemoryRecall -> handleMemoryRecall()
            is CalculatorAction.ToggleAngleMode -> toggleAngleMode()
            is CalculatorAction.ToggleScientificMode -> toggleScientificMode()
            is CalculatorAction.ToggleInverseMode -> toggleInverseMode()
            is CalculatorAction.OpenHistory -> _uiState.update { it.copy(isHistoryVisible = true) }
            is CalculatorAction.CloseHistory -> _uiState.update { it.copy(isHistoryVisible = false) }
            is CalculatorAction.SelectHistoryItem -> selectHistoryItem(action.item)
            is CalculatorAction.ClearHistory -> clearHistory()
        }
    }

    private fun handleDigit(digit: Int) {
        if (_uiState.value.isError) {
            handleAllClear()
        }

        if (shouldResetInput) {
            rawBuffer = digit.toString()
            shouldResetInput = false
        } else {
            // Count total digits excluding minus and decimal point
            val digitCount = rawBuffer.count { it.isDigit() }
            if (digitCount >= 9) return

            rawBuffer = if (rawBuffer == "0") digit.toString() else rawBuffer + digit
        }

        _uiState.update {
            it.copy(
                displayValue = CalculatorEngine.formatInputBuffer(rawBuffer),
                isAllClear = false,
                isError = false,
                pendingOperator = if (shouldResetInput) pendingOperator else null
            )
        }
    }

    private fun handleDecimal() {
        if (_uiState.value.isError) {
            handleAllClear()
        }

        if (shouldResetInput) {
            rawBuffer = "0."
            shouldResetInput = false
        } else if (!rawBuffer.contains(".")) {
            rawBuffer = "$rawBuffer."
        }

        _uiState.update {
            it.copy(
                displayValue = CalculatorEngine.formatInputBuffer(rawBuffer),
                isAllClear = false,
                isError = false
            )
        }
    }

    private fun handleBackspace() {
        if (_uiState.value.isError || shouldResetInput) return

        rawBuffer = if (rawBuffer.length > 1) {
            val shortened = rawBuffer.dropLast(1)
            if (shortened == "-") "0" else shortened
        } else {
            "0"
        }

        _uiState.update {
            it.copy(
                displayValue = CalculatorEngine.formatInputBuffer(rawBuffer),
                isAllClear = rawBuffer == "0"
            )
        }
    }

    private fun handleClear() {
        if (!_uiState.value.isAllClear) {
            // Single tap C: clears current buffer
            rawBuffer = "0"
            _uiState.update {
                it.copy(
                    displayValue = "0",
                    isAllClear = true,
                    isError = false
                )
            }
        } else {
            handleAllClear()
        }
    }

    private fun handleAllClear() {
        rawBuffer = "0"
        accumulator = null
        pendingOperator = null
        lastOperator = null
        lastOperand = null
        shouldResetInput = false
        _uiState.update {
            it.copy(
                displayValue = "0",
                expressionText = "",
                pendingOperator = null,
                isAllClear = true,
                isError = false
            )
        }
    }

    private fun handleToggleSign() {
        if (_uiState.value.isError) return
        val current = parseCurrentNumber() ?: return
        if (current.compareTo(BigDecimal.ZERO) == 0) return

        val negated = current.negate()
        rawBuffer = negated.toPlainString()
        _uiState.update {
            it.copy(displayValue = CalculatorEngine.formatDisplay(negated))
        }
    }

    private fun handlePercent() {
        if (_uiState.value.isError) return
        val current = parseCurrentNumber() ?: return
        val base = accumulator
        val isAddSub = pendingOperator == CalculatorOperator.ADD || pendingOperator == CalculatorOperator.SUBTRACT

        try {
            val result = CalculatorEngine.percent(current, base, isAddSub)
            rawBuffer = result.toPlainString()
            _uiState.update {
                it.copy(displayValue = CalculatorEngine.formatDisplay(result))
            }
        } catch (_: Exception) {
            setErrorState()
        }
    }

    private fun handleOperator(op: CalculatorOperator) {
        if (_uiState.value.isError) return
        val current = parseCurrentNumber() ?: return

        if (accumulator != null && pendingOperator != null && !shouldResetInput) {
            try {
                val evaluated = CalculatorEngine.applyOperator(accumulator!!, current, pendingOperator!!)
                accumulator = evaluated
                rawBuffer = evaluated.toPlainString()
                _uiState.update {
                    it.copy(
                        displayValue = CalculatorEngine.formatDisplay(evaluated),
                        expressionText = "${CalculatorEngine.formatDisplay(evaluated)} ${op.symbol}",
                        pendingOperator = op
                    )
                }
            } catch (_: Exception) {
                setErrorState()
                return
            }
        } else {
            accumulator = current
            _uiState.update {
                it.copy(
                    expressionText = "${CalculatorEngine.formatDisplay(current)} ${op.symbol}",
                    pendingOperator = op
                )
            }
        }

        pendingOperator = op
        shouldResetInput = true
        lastOperator = null
        lastOperand = null
    }

    private fun handleCalculate() {
        if (_uiState.value.isError) return
        val current = parseCurrentNumber() ?: return

        if (pendingOperator != null && accumulator != null) {
            val operand = current
            val op = pendingOperator!!
            try {
                val result = CalculatorEngine.applyOperator(accumulator!!, operand, op)
                val expression = "${CalculatorEngine.formatDisplay(accumulator!!)} ${op.symbol} ${CalculatorEngine.formatDisplay(operand)}"
                val formattedResult = CalculatorEngine.formatDisplay(result)

                viewModelScope.launch {
                    repository.addCalculation(expression, formattedResult)
                }

                lastOperator = op
                lastOperand = operand
                accumulator = result
                rawBuffer = result.toPlainString()
                pendingOperator = null
                shouldResetInput = true

                _uiState.update {
                    it.copy(
                        displayValue = formattedResult,
                        expressionText = "$expression =",
                        pendingOperator = null,
                        isAllClear = true,
                        isError = false
                    )
                }
            } catch (_: Exception) {
                setErrorState()
            }
        } else if (lastOperator != null && lastOperand != null) {
            // Repeated equals
            try {
                val result = CalculatorEngine.applyOperator(current, lastOperand!!, lastOperator!!)
                val expression = "${CalculatorEngine.formatDisplay(current)} ${lastOperator!!.symbol} ${CalculatorEngine.formatDisplay(lastOperand!!)}"
                val formattedResult = CalculatorEngine.formatDisplay(result)

                viewModelScope.launch {
                    repository.addCalculation(expression, formattedResult)
                }

                accumulator = result
                rawBuffer = result.toPlainString()
                shouldResetInput = true

                _uiState.update {
                    it.copy(
                        displayValue = formattedResult,
                        expressionText = "$expression =",
                        isAllClear = true,
                        isError = false
                    )
                }
            } catch (_: Exception) {
                setErrorState()
            }
        }
    }

    private fun handleScientificUnary(type: ScientificUnaryType) {
        if (_uiState.value.isError) return
        val current = parseCurrentNumber() ?: return

        try {
            val result = CalculatorEngine.applyUnary(current, type, _uiState.value.angleMode)
            val formattedResult = CalculatorEngine.formatDisplay(result)
            val label = if (_uiState.value.isInverseMode) type.inverseLabel else type.label
            val expr = "$label(${CalculatorEngine.formatDisplay(current)})"

            viewModelScope.launch {
                repository.addCalculation(expr, formattedResult)
            }

            rawBuffer = result.toPlainString()
            shouldResetInput = true

            _uiState.update {
                it.copy(
                    displayValue = formattedResult,
                    expressionText = "$expr =",
                    isAllClear = false,
                    isError = false
                )
            }
        } catch (_: Exception) {
            setErrorState()
        }
    }

    private fun handleConstant(type: ConstantType) {
        if (_uiState.value.isError) handleAllClear()

        val value = when (type) {
            ConstantType.PI -> BigDecimal("3.141592653589793")
            ConstantType.E -> BigDecimal("2.718281828459045")
            ConstantType.RAND -> BigDecimal(Random().nextDouble().toString(), CalculatorEngine.MATH_CONTEXT)
        }

        rawBuffer = value.toPlainString()
        shouldResetInput = true

        _uiState.update {
            it.copy(
                displayValue = CalculatorEngine.formatDisplay(value),
                isAllClear = false,
                isError = false
            )
        }
    }

    private fun handleOpenParenthesis() {
        val currentExpr = _uiState.value.expressionText
        _uiState.update { it.copy(expressionText = "$currentExpr ( ") }
    }

    private fun handleCloseParenthesis() {
        val currentExpr = _uiState.value.expressionText
        val current = parseCurrentNumber()
        val numStr = current?.let { CalculatorEngine.formatDisplay(it) } ?: ""
        val fullExpr = "$currentExpr $numStr )"
        try {
            val result = CalculatorEngine.evaluateExpression(fullExpr)
            rawBuffer = result.toPlainString()
            shouldResetInput = true
            _uiState.update {
                it.copy(
                    displayValue = CalculatorEngine.formatDisplay(result),
                    expressionText = "$fullExpr =",
                    isAllClear = false
                )
            }
        } catch (_: Exception) {
            _uiState.update { it.copy(expressionText = fullExpr) }
        }
    }

    private fun handleEE() {
        if (_uiState.value.isError || rawBuffer.contains("e") || rawBuffer.contains("E")) return
        rawBuffer = "$rawBuffer" + "e"
        _uiState.update {
            it.copy(displayValue = rawBuffer, isAllClear = false)
        }
    }

    private fun handleMemoryClear() {
        memory = BigDecimal.ZERO
        _uiState.update { it.copy(hasMemory = false) }
    }

    private fun handleMemoryAdd() {
        val current = parseCurrentNumber() ?: return
        memory = CalculatorEngine.add(memory, current)
        _uiState.update { it.copy(hasMemory = true) }
    }

    private fun handleMemorySubtract() {
        val current = parseCurrentNumber() ?: return
        memory = CalculatorEngine.subtract(memory, current)
        _uiState.update { it.copy(hasMemory = true) }
    }

    private fun handleMemoryRecall() {
        rawBuffer = memory.toPlainString()
        shouldResetInput = true
        _uiState.update {
            it.copy(
                displayValue = CalculatorEngine.formatDisplay(memory),
                isAllClear = false
            )
        }
    }

    private fun toggleAngleMode() {
        _uiState.update {
            it.copy(
                angleMode = if (it.angleMode == AngleMode.DEGREE) AngleMode.RADIAN else AngleMode.DEGREE
            )
        }
    }

    fun toggleScientificMode() {
        _uiState.update { it.copy(isScientificExpanded = !it.isScientificExpanded) }
    }

    private fun toggleInverseMode() {
        _uiState.update { it.copy(isInverseMode = !it.isInverseMode) }
    }

    private fun selectHistoryItem(item: CalculationHistoryItem) {
        val cleanResult = item.result.replace(",", "")
        try {
            val bd = BigDecimal(cleanResult)
            rawBuffer = bd.toPlainString()
            shouldResetInput = true
            _uiState.update {
                it.copy(
                    displayValue = item.result,
                    expressionText = item.expression,
                    isHistoryVisible = false,
                    isAllClear = false,
                    isError = false
                )
            }
        } catch (_: Exception) {
            _uiState.update { it.copy(isHistoryVisible = false) }
        }
    }

    private fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    private fun parseCurrentNumber(): BigDecimal? {
        val clean = rawBuffer.replace(",", "")
        return try {
            BigDecimal(clean, CalculatorEngine.MATH_CONTEXT)
        } catch (_: Exception) {
            null
        }
    }

    private fun setErrorState() {
        rawBuffer = "0"
        accumulator = null
        pendingOperator = null
        lastOperator = null
        lastOperand = null
        shouldResetInput = true
        _uiState.update {
            it.copy(
                displayValue = "Error",
                expressionText = "",
                pendingOperator = null,
                isAllClear = true,
                isError = true
            )
        }
    }
}
