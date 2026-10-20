package com.example.domain.engine

import com.example.domain.model.AngleMode
import com.example.domain.model.CalculatorOperator
import com.example.domain.model.ScientificUnaryType
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.Stack
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh

object CalculatorEngine {
    val MATH_CONTEXT: MathContext = MathContext.DECIMAL128
    private const val MAX_DISPLAY_DIGITS = 10

    fun add(a: BigDecimal, b: BigDecimal): BigDecimal = a.add(b, MATH_CONTEXT)

    fun subtract(a: BigDecimal, b: BigDecimal): BigDecimal = a.subtract(b, MATH_CONTEXT)

    fun multiply(a: BigDecimal, b: BigDecimal): BigDecimal = a.multiply(b, MATH_CONTEXT)

    fun divide(a: BigDecimal, b: BigDecimal): BigDecimal {
        if (b.compareTo(BigDecimal.ZERO) == 0) {
            throw ArithmeticException("Division by zero")
        }
        return a.divide(b, MATH_CONTEXT)
    }

    fun applyOperator(a: BigDecimal, b: BigDecimal, op: CalculatorOperator): BigDecimal {
        return when (op) {
            CalculatorOperator.ADD -> add(a, b)
            CalculatorOperator.SUBTRACT -> subtract(a, b)
            CalculatorOperator.MULTIPLY -> multiply(a, b)
            CalculatorOperator.DIVIDE -> divide(a, b)
            CalculatorOperator.POWER -> power(a, b)
        }
    }

    fun power(base: BigDecimal, exponent: BigDecimal): BigDecimal {
        if (base.compareTo(BigDecimal.ZERO) == 0 && exponent.compareTo(BigDecimal.ZERO) <= 0) {
            throw ArithmeticException("Domain error")
        }
        
        // Exact power if exponent is an integer in a reasonable range
        try {
            val expLong = exponent.toLong()
            if (BigDecimal(expLong).compareTo(exponent) == 0) {
                if (expLong >= 0 && expLong <= 1000) {
                    return base.pow(expLong.toInt(), MATH_CONTEXT)
                } else if (expLong < 0 && expLong >= -1000) {
                    return BigDecimal.ONE.divide(base.pow((-expLong).toInt(), MATH_CONTEXT), MATH_CONTEXT)
                }
            }
        } catch (_: Exception) {
            // Fall through to double calculation
        }

        val baseD = base.toDouble()
        val expD = exponent.toDouble()
        if (baseD < 0 && exponent.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
            throw ArithmeticException("Domain error")
        }
        val result = baseD.pow(expD)
        if (result.isNaN() || result.isInfinite()) {
            throw ArithmeticException("Overflow error")
        }
        return BigDecimal(result.toString(), MATH_CONTEXT)
    }

    fun factorial(value: BigDecimal): BigDecimal {
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
            throw ArithmeticException("Domain error")
        }
        val n = value.toInt()
        if (n > 100) {
            throw ArithmeticException("Overflow error")
        }
        var result = BigDecimal.ONE
        for (i in 2..n) {
            result = result.multiply(BigDecimal(i), MATH_CONTEXT)
        }
        return result
    }

    fun percent(current: BigDecimal, base: BigDecimal?, isAdditionOrSubtraction: Boolean): BigDecimal {
        val hundredth = current.divide(BigDecimal("100"), MATH_CONTEXT)
        return if (isAdditionOrSubtraction && base != null) {
            base.multiply(hundredth, MATH_CONTEXT)
        } else {
            hundredth
        }
    }

    fun applyUnary(value: BigDecimal, type: ScientificUnaryType, angleMode: AngleMode): BigDecimal {
        val v = value.toDouble()
        val rad = if (angleMode == AngleMode.DEGREE) Math.toRadians(v) else v

        val res: Double = when (type) {
            ScientificUnaryType.SIN -> {
                val clean = normalizeAngle(v, angleMode)
                if (angleMode == AngleMode.DEGREE && (clean == 0.0 || clean == 180.0 || clean == 360.0)) 0.0
                else if (angleMode == AngleMode.DEGREE && clean == 90.0) 1.0
                else if (angleMode == AngleMode.DEGREE && clean == 270.0) -1.0
                else sin(rad)
            }
            ScientificUnaryType.COS -> {
                val clean = normalizeAngle(v, angleMode)
                if (angleMode == AngleMode.DEGREE && (clean == 90.0 || clean == 270.0)) 0.0
                else if (angleMode == AngleMode.DEGREE && (clean == 0.0 || clean == 360.0)) 1.0
                else if (angleMode == AngleMode.DEGREE && clean == 180.0) -1.0
                else cos(rad)
            }
            ScientificUnaryType.TAN -> {
                val clean = normalizeAngle(v, angleMode)
                if (angleMode == AngleMode.DEGREE && (clean == 90.0 || clean == 270.0)) {
                    throw ArithmeticException("Domain error")
                }
                tan(rad)
            }
            ScientificUnaryType.ASIN -> {
                if (v < -1.0 || v > 1.0) throw ArithmeticException("Domain error")
                val r = asin(v)
                if (angleMode == AngleMode.DEGREE) Math.toDegrees(r) else r
            }
            ScientificUnaryType.ACOS -> {
                if (v < -1.0 || v > 1.0) throw ArithmeticException("Domain error")
                val r = acos(v)
                if (angleMode == AngleMode.DEGREE) Math.toDegrees(r) else r
            }
            ScientificUnaryType.ATAN -> {
                val r = atan(v)
                if (angleMode == AngleMode.DEGREE) Math.toDegrees(r) else r
            }
            ScientificUnaryType.SINH -> sinh(v)
            ScientificUnaryType.COSH -> cosh(v)
            ScientificUnaryType.TANH -> tanh(v)
            ScientificUnaryType.LN -> {
                if (v <= 0.0) throw ArithmeticException("Domain error")
                ln(v)
            }
            ScientificUnaryType.LOG10 -> {
                if (v <= 0.0) throw ArithmeticException("Domain error")
                log10(v)
            }
            ScientificUnaryType.LOG2 -> {
                if (v <= 0.0) throw ArithmeticException("Domain error")
                log2(v)
            }
            ScientificUnaryType.EXP -> exp(v)
            ScientificUnaryType.EXP10 -> 10.0.pow(v)
            ScientificUnaryType.SQUARE -> return multiply(value, value)
            ScientificUnaryType.CUBE -> return multiply(value, multiply(value, value))
            ScientificUnaryType.SQRT -> {
                if (value.compareTo(BigDecimal.ZERO) < 0) throw ArithmeticException("Domain error")
                sqrt(v)
            }
            ScientificUnaryType.CBRT -> cbrt(v)
            ScientificUnaryType.RECIPROCAL -> return divide(BigDecimal.ONE, value)
            ScientificUnaryType.FACTORIAL -> return factorial(value)
        }

        if (res.isNaN() || res.isInfinite()) {
            throw ArithmeticException("Domain error")
        }
        
        // Clean up small floating point noise near zero (e.g. 1e-16 -> 0)
        val cleanRes = if (kotlin.math.abs(res) < 1e-15) 0.0 else res
        return BigDecimal(cleanRes.toString(), MATH_CONTEXT)
    }

    private fun normalizeAngle(angle: Double, mode: AngleMode): Double {
        if (mode == AngleMode.DEGREE) {
            var a = angle % 360.0
            if (a < 0) a += 360.0
            return a
        }
        return angle
    }

    /**
     * Evaluates a mathematical token expression using the Shunting-Yard algorithm.
     * Supports +, -, *, /, ^ and parentheses.
     */
    fun evaluateExpression(expression: String): BigDecimal {
        val tokens = tokenize(expression)
        val rpn = infixToRPN(tokens)
        return evaluateRPN(rpn)
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val s = expr.replace(" ", "")
        while (i < s.length) {
            val c = s[i]
            when {
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < s.length && (s[i].isDigit() || s[i] == '.' || s[i] == 'e' || s[i] == 'E')) {
                        if ((s[i] == 'e' || s[i] == 'E') && i + 1 < s.length && (s[i + 1] == '+' || s[i + 1] == '-')) {
                            sb.append(s[i])
                            i++
                            sb.append(s[i])
                            i++
                        } else {
                            sb.append(s[i])
                            i++
                        }
                    }
                    tokens.add(sb.toString())
                    continue
                }
                c == '+' || c == '-' || c == '−' || c == '*' || c == '×' || c == '/' || c == '÷' || c == '^' || c == '(' || c == ')' -> {
                    // Check for unary minus: at start or right after open parenthesis or another operator
                    if ((c == '-' || c == '−') && (tokens.isEmpty() || tokens.last() == "(" || isOperator(tokens.last()))) {
                        // Unary minus - treat next number as negative if followed by digit
                        if (i + 1 < s.length && (s[i + 1].isDigit() || s[i + 1] == '.')) {
                            val sb = StringBuilder("-")
                            i++
                            while (i < s.length && (s[i].isDigit() || s[i] == '.' || s[i] == 'e' || s[i] == 'E')) {
                                sb.append(s[i])
                                i++
                            }
                            tokens.add(sb.toString())
                            continue
                        }
                    }
                    tokens.add(c.toString())
                    i++
                }
                else -> i++
            }
        }
        return tokens
    }

    private fun isOperator(t: String): Boolean {
        return t == "+" || t == "-" || t == "−" || t == "*" || t == "×" || t == "/" || t == "÷" || t == "^"
    }

    private fun getPrecedence(op: String): Int {
        return when (op) {
            "+", "-", "−" -> 1
            "*", "×", "/", "÷" -> 2
            "^" -> 3
            else -> 0
        }
    }

    private fun infixToRPN(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = Stack<String>()

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> output.add(token)
                token == "(" -> stack.push(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.peek() != "(") {
                        output.add(stack.pop())
                    }
                    if (stack.isNotEmpty() && stack.peek() == "(") {
                        stack.pop()
                    }
                }
                isOperator(token) -> {
                    while (stack.isNotEmpty() && isOperator(stack.peek()) &&
                        (getPrecedence(stack.peek()) > getPrecedence(token) ||
                                (getPrecedence(stack.peek()) == getPrecedence(token) && token != "^"))
                    ) {
                        output.add(stack.pop())
                    }
                    stack.push(token)
                }
            }
        }
        while (stack.isNotEmpty()) {
            output.add(stack.pop())
        }
        return output
    }

    private fun evaluateRPN(rpn: List<String>): BigDecimal {
        val stack = Stack<BigDecimal>()
        for (token in rpn) {
            val num = token.toDoubleOrNull()
            if (num != null) {
                stack.push(BigDecimal(token, MATH_CONTEXT))
            } else if (isOperator(token)) {
                if (stack.size < 2) throw ArithmeticException("Invalid expression")
                val b = stack.pop()
                val a = stack.pop()
                val op = CalculatorOperator.fromSymbol(token) ?: throw ArithmeticException("Unknown operator")
                stack.push(applyOperator(a, b, op))
            }
        }
        if (stack.isEmpty()) return BigDecimal.ZERO
        return stack.pop()
    }

    /**
     * Formats a BigDecimal into an iOS-style display string.
     * Uses thousands grouping and scientific notation for magnitudes >= 1e9 or < 1e-7.
     * Keeps display length within MAX_DISPLAY_DIGITS (9-10 digits).
     */
    fun formatDisplay(value: BigDecimal): String {
        // Handle zero explicitly
        if (value.compareTo(BigDecimal.ZERO) == 0) return "0"

        val absVal = value.abs()
        val magnitudeUpper = BigDecimal("1000000000") // 1e9
        val magnitudeLower = BigDecimal("0.0000001") // 1e-7

        val isScientific = absVal >= magnitudeUpper || absVal < magnitudeLower

        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
            decimalSeparator = '.'
        }

        if (isScientific) {
            val df = DecimalFormat("0.######E0", symbols)
            return df.format(value.toDouble()).replace("E", "e")
        }

        // Standard decimal formatting
        val stripped = value.stripTrailingZeros()
        val plain = stripped.toPlainString()

        // Check if plain string is within display budget
        val integerPart = stripped.toBigInteger()
        val intStr = integerPart.toString()

        val df = DecimalFormat("#,##0.#########", symbols)
        val formatted = df.format(stripped)

        // Truncate if exceeds display digits without losing meaning
        if (formatted.length > MAX_DISPLAY_DIGITS + 3) { // account for commas
            val rounded = value.setScale(MAX_DISPLAY_DIGITS - intStr.length.coerceAtMost(MAX_DISPLAY_DIGITS), RoundingMode.HALF_UP)
            return df.format(rounded.stripTrailingZeros())
        }

        return formatted
    }

    /**
     * Formats while typing (e.g. preserves trailing '.' or trailing zeros like "12.00").
     */
    fun formatInputBuffer(buffer: String): String {
        if (buffer.isEmpty() || buffer == "Error") return buffer
        if (buffer == "-") return "-"

        val isNegative = buffer.startsWith("-")
        val cleanBuffer = if (isNegative) buffer.substring(1) else buffer

        val parts = cleanBuffer.split(".")
        val intPart = parts[0]
        val formattedInt = if (intPart.isNotEmpty()) {
            try {
                val symbols = DecimalFormatSymbols(Locale.US).apply { groupingSeparator = ',' }
                val df = DecimalFormat("#,##0", symbols)
                df.format(BigInteger(intPart))
            } catch (_: Exception) {
                intPart
            }
        } else {
            "0"
        }

        val prefix = if (isNegative) "-" else ""
        return if (parts.size > 1) {
            "$prefix$formattedInt.${parts[1]}"
        } else if (cleanBuffer.endsWith(".")) {
            "$prefix$formattedInt."
        } else {
            "$prefix$formattedInt"
        }
    }
}
