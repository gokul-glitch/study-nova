package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassMaterial
import com.example.ui.util.LiquidHapticType
import com.example.ui.util.rememberLiquidHaptics

/**
 * Clean, solid background container.
 * Eliminates all washed-out pastel/aurora gradients and provides high-contrast foundation.
 */
@Composable
fun AppBackground(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC))
    ) {
        content()
    }
}

/**
 * Production-ready elevated card component with solid surface and crisp outline.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = Color.Unspecified,
    shape: Shape = RoundedCornerShape(GlassMaterial.DefaultCornerRadius),
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val haptics = rememberLiquidHaptics()
    val containerColor = if (color != Color.Unspecified) color else if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val borderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    if (onClick != null) {
        Card(
            onClick = {
                haptics.perform(LiquidHapticType.LightClick)
                onClick()
            },
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 3.dp),
            border = BorderStroke(1.dp, borderColor),
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, borderColor),
            content = content
        )
    }
}

/**
 * Primary action button with solid Royal Blue background, bold white typography,
 * and standard 48dp touch target.
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val haptics = rememberLiquidHaptics()

    Button(
        onClick = {
            haptics.perform(LiquidHapticType.LightClick)
            onClick()
        },
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(GlassMaterial.ButtonCornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2563EB),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF94A3B8),
            disabledContentColor = Color.White.copy(alpha = 0.8f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
        enabled = enabled,
        content = content
    )
}

/**
 * Secondary outlined button with crisp border and solid surface.
 */
@Composable
fun GlassOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val haptics = rememberLiquidHaptics()

    OutlinedButton(
        onClick = {
            haptics.perform(LiquidHapticType.LightClick)
            onClick()
        },
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(GlassMaterial.ButtonCornerRadius),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF),
            contentColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
        ),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)),
        enabled = enabled,
        content = content
    )
}

/**
 * Clean filter chip with high-contrast active and inactive states.
 */
@Composable
fun GlassFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val haptics = rememberLiquidHaptics()

    Surface(
        onClick = {
            haptics.perform(LiquidHapticType.Selection)
            onClick()
        },
        modifier = modifier,
        shape = RoundedCornerShape(GlassMaterial.DefaultCornerRadius),
        color = if (selected) Color(0xFF2563EB) else (if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)),
        border = BorderStroke(1.dp, if (selected) Color(0xFF2563EB) else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))),
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            ProvideTextStyle(
                value = MaterialTheme.typography.labelLarge.copy(
                    color = if (selected) Color.White else (if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)),
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                )
            ) {
                label()
            }
        }
    }
}

/**
 * Solid, accessible OutlinedTextField with readable text and clear borders.
 */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    colors: TextFieldColors? = null
) {
    val isDark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(GlassMaterial.ButtonCornerRadius)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        enabled = enabled,
        readOnly = readOnly,
        shape = shape,
        colors = colors ?: OutlinedTextFieldDefaults.colors(
            focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF),
            unfocusedContainerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF),
            disabledContainerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
            focusedBorderColor = Color(0xFF2563EB),
            unfocusedBorderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedTextColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
            unfocusedTextColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
            focusedPlaceholderColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
            unfocusedPlaceholderColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
            focusedLeadingIconColor = Color(0xFF2563EB),
            unfocusedLeadingIconColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
            focusedTrailingIconColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
            unfocusedTrailingIconColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
        )
    )
}

/**
 * Solid surface box with clean background and border.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    shape: Shape = RoundedCornerShape(GlassMaterial.DefaultCornerRadius),
    elevation: Dp = 1.dp,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val containerColor = if (color != Color.Unspecified) color else if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val borderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = elevation
    ) {
        content()
    }
}
