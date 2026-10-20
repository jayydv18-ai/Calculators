package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.AngleMode
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.IosOrange
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary

@Composable
fun CalculatorDisplay(
    displayValue: String,
    expressionText: String,
    angleMode: AngleMode,
    hasMemory: Boolean,
    isScientific: Boolean,
    onBackspace: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val isDark = isSystemInDarkTheme()

    val textColor = if (isDark) DarkTextPrimary else LightTextPrimary
    val secondaryColor = if (isDark) DarkTextSecondary else LightTextSecondary

    // Calculate dynamic font size based on length of display string
    val length = displayValue.length
    val fontSize = when {
        length <= 6 -> 64.sp
        length <= 8 -> 54.sp
        length <= 10 -> 44.sp
        length <= 12 -> 36.sp
        else -> 30.sp
    }

    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragX += dragAmount.x
                        totalDragY += dragAmount.y
                    },
                    onDragEnd = {
                        // Horizontal swipe to delete last digit
                        if (kotlin.math.abs(totalDragX) > 40f && kotlin.math.abs(totalDragX) > kotlin.math.abs(totalDragY)) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onBackspace()
                        }
                        // Downward swipe to open calculation history
                        else if (totalDragY > 60f && totalDragY > kotlin.math.abs(totalDragX)) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                            onOpenHistory()
                        }
                        totalDragX = 0f
                        totalDragY = 0f
                    }
                )
            }
            .pointerInput(displayValue) {
                detectTapGestures(
                    onLongPress = {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Calculation Result", displayValue)
                        clipboard.setPrimaryClip(clip)
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                            Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            },
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Status Row (RAD/DEG badge, Memory badge)
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isScientific) {
                    Text(
                        text = if (angleMode == AngleMode.RADIAN) stringResource(R.string.angle_rad) else stringResource(R.string.angle_deg),
                        color = secondaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("badge_angle")
                    )
                }
                if (hasMemory) {
                    Text(
                        text = "M",
                        color = IosOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("badge_memory")
                    )
                }
            }

            // Live Expression breadcrumb line
            Text(
                text = expressionText,
                color = secondaryColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                textAlign = TextAlign.End,
                modifier = Modifier.testTag("display_expression")
            )
        }

        // Main Primary Formatted Result
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = displayValue,
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Light,
                lineHeight = fontSize,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier
                    .testTag("display_result")
                    .semantics { contentDescription = "Result: $displayValue" }
            )
        }
    }
}
