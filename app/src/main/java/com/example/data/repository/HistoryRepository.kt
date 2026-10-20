package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.CalculationHistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "calculator_prefs")

class HistoryRepository(private val context: Context) {
    private val historyKey = stringPreferencesKey("calculation_history_v1")

    val historyFlow: Flow<List<CalculationHistoryItem>> = context.dataStore.data.map { preferences ->
        val raw = preferences[historyKey] ?: ""
        deserializeHistory(raw)
    }

    suspend fun addCalculation(expression: String, result: String) {
        if (expression.isBlank() || result.isBlank() || result == "Error") return
        
        context.dataStore.edit { preferences ->
            val raw = preferences[historyKey] ?: ""
            val currentList = deserializeHistory(raw).toMutableList()
            
            val newItem = CalculationHistoryItem(
                id = UUID.randomUUID().toString(),
                expression = expression,
                result = result,
                timestamp = System.currentTimeMillis()
            )
            // Prepend new item, limit to last 50
            currentList.add(0, newItem)
            val trimmed = if (currentList.size > 50) currentList.subList(0, 50) else currentList
            preferences[historyKey] = serializeHistory(trimmed)
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { preferences ->
            preferences.remove(historyKey)
        }
    }

    private fun serializeHistory(items: List<CalculationHistoryItem>): String {
        return items.joinToString("\n") { item ->
            // Use base64 or URL safe escaping, or a clean delimiter
            val cleanExpr = item.expression.replace("\t", " ")
            val cleanRes = item.result.replace("\t", " ")
            "${item.id}\t$cleanExpr\t$cleanRes\t${item.timestamp}"
        }
    }

    private fun deserializeHistory(raw: String): List<CalculationHistoryItem> {
        if (raw.isBlank()) return emptyList()
        return raw.lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.split("\t")
                if (parts.size >= 4) {
                    CalculationHistoryItem(
                        id = parts[0],
                        expression = parts[1],
                        result = parts[2],
                        timestamp = parts[3].toLongOrNull() ?: System.currentTimeMillis()
                    )
                } else null
            }
            .toList()
    }
}
