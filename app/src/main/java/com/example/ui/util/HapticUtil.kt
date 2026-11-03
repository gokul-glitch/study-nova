package com.example.ui.util

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView

enum class LiquidHapticType {
    LightClick,     // Subtle tap for card presses, buttons, micro-interactions
    Selection,      // Tab switch, chip selection, segmented control
    Toggle,         // Flip card, checkbox, switch
    Success,        // Action completed, quiz answer correct, generated
    Error,          // Error state
    LongPress       // Long press actions
}

class LiquidHaptics(
    private val view: View,
    private val composeHaptics: HapticFeedback
) {
    fun perform(type: LiquidHapticType = LiquidHapticType.LightClick) {
        try {
            when (type) {
                LiquidHapticType.LightClick -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_TICK)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    } else {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                }
                LiquidHapticType.Selection -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
                    } else {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    }
                }
                LiquidHapticType.Toggle -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        view.performHapticFeedback(HapticFeedbackConstants.TOGGLE_ON)
                    } else {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                }
                LiquidHapticType.Success -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    } else {
                        composeHaptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
                LiquidHapticType.Error -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                    } else {
                        composeHaptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
                LiquidHapticType.LongPress -> {
                    composeHaptics.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
        } catch (_: Exception) {
            composeHaptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
}

@Composable
fun rememberLiquidHaptics(): LiquidHaptics {
    val view = LocalView.current
    val composeHaptics = LocalHapticFeedback.current
    return remember(view, composeHaptics) {
        LiquidHaptics(view, composeHaptics)
    }
}
