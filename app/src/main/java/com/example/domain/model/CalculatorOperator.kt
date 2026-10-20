package com.example.domain.model

enum class CalculatorOperator(val symbol: String, val precedence: Int) {
    ADD("+", 1),
    SUBTRACT("−", 1),
    MULTIPLY("×", 2),
    DIVIDE("÷", 2),
    POWER("^", 3);

    companion object {
        fun fromSymbol(symbol: String): CalculatorOperator? {
            return entries.find { it.symbol == symbol || (it == SUBTRACT && symbol == "-") || (it == MULTIPLY && symbol == "*") || (it == DIVIDE && symbol == "/") }
        }
    }
}
