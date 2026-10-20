package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkFunctionKey
import com.example.ui.theme.DarkGlassBorder
import com.example.ui.theme.DarkNumberKey
import com.example.ui.theme.DarkScientificKey
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.IosOrange
import com.example.ui.theme.LightFunctionKey
import com.example.ui.theme.LightGlassBorder
import com.example.ui.theme.LightNumberKey
import com.example.ui.theme.LightScientificKey
import com.example.ui.theme.LightTextPrimary

enum class ButtonKind {
    NUMBER,
    FUNCTION,
    OPERATOR,
    SCIENTIFIC
}

@Composable
fun CalculatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    kind: ButtonKind = ButtonKind.NUMBER,
    isActiveOperator: Boolean = false,
    fontSize: TextUnit = 28.sp,
    aspectRatio: Float = 1f,
    shape: Shape = CircleShape,
    contentDesc: String = text,
    testTag: String = "btn_$text"
) {
    val view = LocalView.current
    val isDark = isSystemInDarkTheme()
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f),
        label = "button_scale"
    )

    // Compute background and content colors according to button role, active state, and theme
    val (backgroundColor, contentColor) = when {
        isActiveOperator -> {
            // iOS operator active inverted state
            Pair(Color.White, IosOrange)
        }
        kind == ButtonKind.OPERATOR -> {
            Pair(IosOrange, Color.White)
        }
        kind == ButtonKind.FUNCTION -> {
            if (isDark) Pair(DarkFunctionKey, DarkTextPrimary) else Pair(LightFunctionKey, LightTextPrimary)
        }
        kind == ButtonKind.SCIENTIFIC -> {
            if (isDark) Pair(DarkScientificKey, DarkTextPrimary) else Pair(LightScientificKey, LightTextPrimary)
        }
        else -> { // NUMBER
            if (isDark) Pair(DarkNumberKey, DarkTextPrimary) else Pair(LightNumberKey, LightTextPrimary)
        }
    }

    val borderStroke = if (isDark) {
        BorderStroke(1.dp, DarkGlassBorder)
    } else {
        BorderStroke(0.5.dp, LightGlassBorder)
    }

    Surface(
        modifier = modifier
            .scale(scale)
            .semantics { contentDescription = contentDesc }
            .testTag(testTag)
            .then(
                if (aspectRatio > 0f) Modifier.aspectRatio(aspectRatio) else Modifier
            )
            .pointerInput(text) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onClick()
                    },
                    onLongPress = onLongClick?.let { longAction ->
                        {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            longAction()
                        }
                    }
                )
            },
        shape = shape,
        color = backgroundColor,
        border = borderStroke,
        tonalElevation = if (isDark) 2.dp else 1.dp,
        shadowElevation = if (!isDark && kind == ButtonKind.NUMBER) 1.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = contentColor,
                fontSize = fontSize,
                fontWeight = if (kind == ButtonKind.OPERATOR || kind == ButtonKind.FUNCTION) FontWeight.Medium else FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
