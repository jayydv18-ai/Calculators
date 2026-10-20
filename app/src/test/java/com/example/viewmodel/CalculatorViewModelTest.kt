package com.example.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.CalculatorAction
import com.example.domain.model.CalculatorOperator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalculatorViewModelTest {

    private lateinit var viewModel: CalculatorViewModel

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = CalculatorViewModel(app)
    }

    @Test
    fun `digit input updates display and toggles all clear state`() {
        viewModel.onAction(CalculatorAction.Digit(5))
        assertEquals("5", viewModel.uiState.value.displayValue)
        assertFalse(viewModel.uiState.value.isAllClear)

        viewModel.onAction(CalculatorAction.Digit(2))
        assertEquals("52", viewModel.uiState.value.displayValue)
    }

    @Test
    fun `basic addition 5 + 3 equals 8`() {
        viewModel.onAction(CalculatorAction.Digit(5))
        viewModel.onAction(CalculatorAction.Operator(CalculatorOperator.ADD))
        assertEquals(CalculatorOperator.ADD, viewModel.uiState.value.pendingOperator)

        viewModel.onAction(CalculatorAction.Digit(3))
        viewModel.onAction(CalculatorAction.Calculate)

        assertEquals("8", viewModel.uiState.value.displayValue)
        assertTrue(viewModel.uiState.value.isAllClear)
    }

    @Test
    fun `repeated equals re-executes last operation`() {
        // 5 + 3 = 8
        viewModel.onAction(CalculatorAction.Digit(5))
        viewModel.onAction(CalculatorAction.Operator(CalculatorOperator.ADD))
        viewModel.onAction(CalculatorAction.Digit(3))
        viewModel.onAction(CalculatorAction.Calculate)
        assertEquals("8", viewModel.uiState.value.displayValue)

        // = again -> 8 + 3 = 11
        viewModel.onAction(CalculatorAction.Calculate)
        assertEquals("11", viewModel.uiState.value.displayValue)

        // = again -> 11 + 3 = 14
        viewModel.onAction(CalculatorAction.Calculate)
        assertEquals("14", viewModel.uiState.value.displayValue)
    }

    @Test
    fun `backspace gesture deletes last entered digit`() {
        viewModel.onAction(CalculatorAction.Digit(1))
        viewModel.onAction(CalculatorAction.Digit(2))
        viewModel.onAction(CalculatorAction.Digit(3))
        assertEquals("123", viewModel.uiState.value.displayValue)

        viewModel.onAction(CalculatorAction.Backspace)
        assertEquals("12", viewModel.uiState.value.displayValue)

        viewModel.onAction(CalculatorAction.Backspace)
        assertEquals("1", viewModel.uiState.value.displayValue)

        viewModel.onAction(CalculatorAction.Backspace)
        assertEquals("0", viewModel.uiState.value.displayValue)
        assertTrue(viewModel.uiState.value.isAllClear)
    }

    @Test
    fun `division by zero results in clean Error state`() {
        viewModel.onAction(CalculatorAction.Digit(9))
        viewModel.onAction(CalculatorAction.Operator(CalculatorOperator.DIVIDE))
        viewModel.onAction(CalculatorAction.Digit(0))
        viewModel.onAction(CalculatorAction.Calculate)

        assertEquals("Error", viewModel.uiState.value.displayValue)
        assertTrue(viewModel.uiState.value.isError)
    }
}
