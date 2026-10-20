package com.example.domain

import com.example.domain.engine.CalculatorEngine
import com.example.domain.model.AngleMode
import com.example.domain.model.CalculatorOperator
import com.example.domain.model.ScientificUnaryType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class CalculatorEngineTest {

    @Test
    fun `decimal arithmetic 0_1 plus 0_2 equals exactly 0_3 without floating point error`() {
        val a = BigDecimal("0.1")
        val b = BigDecimal("0.2")
        val result = CalculatorEngine.add(a, b)
        assertEquals("0.3", result.stripTrailingZeros().toPlainString())
        assertEquals("0.3", CalculatorEngine.formatDisplay(result))
    }

    @Test
    fun `operator precedence evaluates multiplication before addition`() {
        // 2 + 3 * 4 = 14
        val result = CalculatorEngine.evaluateExpression("2 + 3 * 4")
        assertEquals("14", result.stripTrailingZeros().toPlainString())
    }

    @Test
    fun `parentheses override default operator precedence`() {
        // (2 + 3) * 4 = 20
        val result = CalculatorEngine.evaluateExpression("( 2 + 3 ) * 4")
        assertEquals("20", result.stripTrailingZeros().toPlainString())
    }

    @Test
    fun `chained operations evaluate correctly with negative numbers`() {
        // 10 - 2 * 3 = 4
        val result = CalculatorEngine.evaluateExpression("10 - 2 * 3")
        assertEquals("4", result.stripTrailingZeros().toPlainString())
    }

    @Test
    fun `division by zero throws ArithmeticException`() {
        assertThrows(ArithmeticException::class.java) {
            CalculatorEngine.divide(BigDecimal("42"), BigDecimal.ZERO)
        }
    }

    @Test
    fun `percent calculations match iOS calculator behavior`() {
        // 100 + 10% = base (100) * (10 / 100) = 10
        val percentOfBase = CalculatorEngine.percent(
            current = BigDecimal("10"),
            base = BigDecimal("100"),
            isAdditionOrSubtraction = true
        )
        assertEquals("10", percentOfBase.stripTrailingZeros().toPlainString())

        // Standalone percent: 50% = 0.5
        val standalone = CalculatorEngine.percent(
            current = BigDecimal("50"),
            base = null,
            isAdditionOrSubtraction = false
        )
        assertEquals("0.5", standalone.stripTrailingZeros().toPlainString())
    }

    @Test
    fun `scientific trigonometry in degrees mode`() {
        // sin(90 deg) = 1
        val sin90 = CalculatorEngine.applyUnary(BigDecimal("90"), ScientificUnaryType.SIN, AngleMode.DEGREE)
        assertEquals("1", CalculatorEngine.formatDisplay(sin90))

        // cos(0 deg) = 1
        val cos0 = CalculatorEngine.applyUnary(BigDecimal("0"), ScientificUnaryType.COS, AngleMode.DEGREE)
        assertEquals("1", CalculatorEngine.formatDisplay(cos0))

        // cos(90 deg) = 0
        val cos90 = CalculatorEngine.applyUnary(BigDecimal("90"), ScientificUnaryType.COS, AngleMode.DEGREE)
        assertEquals("0", CalculatorEngine.formatDisplay(cos90))
    }

    @Test
    fun `scientific logarithms and powers`() {
        // ln(e) ≈ 1
        val e = BigDecimal("2.718281828459045")
        val lnE = CalculatorEngine.applyUnary(e, ScientificUnaryType.LN, AngleMode.RADIAN)
        assertTrue(kotlin.math.abs(lnE.toDouble() - 1.0) < 1e-9)

        // 2^3 = 8
        val pow = CalculatorEngine.power(BigDecimal("2"), BigDecimal("3"))
        assertEquals("8", pow.stripTrailingZeros().toPlainString())

        // sqrt(16) = 4
        val sqrt = CalculatorEngine.applyUnary(BigDecimal("16"), ScientificUnaryType.SQRT, AngleMode.DEGREE)
        assertEquals("4", CalculatorEngine.formatDisplay(sqrt))

        // 5! = 120
        val fact = CalculatorEngine.factorial(BigDecimal("5"))
        assertEquals("120", fact.stripTrailingZeros().toPlainString())
    }

    @Test
    fun `domain errors for square root of negative and invalid asin`() {
        // sqrt(-4) -> error
        assertThrows(ArithmeticException::class.java) {
            CalculatorEngine.applyUnary(BigDecimal("-4"), ScientificUnaryType.SQRT, AngleMode.DEGREE)
        }

        // asin(2) -> error
        assertThrows(ArithmeticException::class.java) {
            CalculatorEngine.applyUnary(BigDecimal("2"), ScientificUnaryType.ASIN, AngleMode.DEGREE)
        }

        // ln(-1) -> error
        assertThrows(ArithmeticException::class.java) {
            CalculatorEngine.applyUnary(BigDecimal("-1"), ScientificUnaryType.LN, AngleMode.RADIAN)
        }
    }

    @Test
    fun `thousands separator formatting and scientific notation threshold`() {
        val large = BigDecimal("1234567.89")
        assertEquals("1,234,567.89", CalculatorEngine.formatDisplay(large))

        val veryLarge = BigDecimal("10000000000") // 1e10 -> scientific
        val formattedSci = CalculatorEngine.formatDisplay(veryLarge)
        assertTrue(formattedSci.contains("e") || formattedSci.contains("E"))
    }
}
