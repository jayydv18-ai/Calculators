package com.example.domain.model

enum class ScientificUnaryType(val label: String, val inverseLabel: String) {
    SIN("sin", "sin⁻¹"),
    COS("cos", "cos⁻¹"),
    TAN("tan", "tan⁻¹"),
    ASIN("sin⁻¹", "sin"),
    ACOS("cos⁻¹", "cos"),
    ATAN("tan⁻¹", "tan"),
    SINH("sinh", "sinh⁻¹"),
    COSH("cosh", "cosh⁻¹"),
    TANH("tanh", "tanh⁻¹"),
    LN("ln", "log₂"),
    LOG10("log₁₀", "log₂"),
    LOG2("log₂", "ln"),
    EXP("eˣ", "yˣ"),
    EXP10("10ˣ", "2ˣ"),
    SQUARE("x²", "x³"),
    CUBE("x³", "x²"),
    SQRT("√x", "∛x"),
    CBRT("∛x", "√x"),
    RECIPROCAL("1/x", "1/x"),
    FACTORIAL("x!", "x!")
}

enum class ConstantType(val symbol: String) {
    PI("π"),
    E("e"),
    RAND("Rand")
}
