package com.example.data.model

data class CalculationHistoryItem(
    val id: String,
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)
