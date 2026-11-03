package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Clean Material 3 visual system parameters.
 * Provides crisp borders, standard corner radii, and solid readable surfaces.
 */
object GlassMaterial {
    val BorderWidth: Dp = 1.dp
    val DefaultCornerRadius: Dp = 16.dp
    val PillCornerRadius: Dp = 24.dp
    val ButtonCornerRadius: Dp = 12.dp
    val SmallCornerRadius: Dp = 8.dp

    fun surfaceColor(isDark: Boolean, tint: Color = Color.Unspecified): Color {
        return if (tint != Color.Unspecified) {
            tint
        } else {
            if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
        }
    }

    fun borderColor(isDark: Boolean): Color {
        return if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    }

    fun borderBrush(isDark: Boolean): androidx.compose.ui.graphics.Brush {
        val color = borderColor(isDark)
        return androidx.compose.ui.graphics.Brush.verticalGradient(listOf(color, color))
    }

    fun innerHighlightBrush(isDark: Boolean): androidx.compose.ui.graphics.Brush {
        return androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
    }

    fun specularShimmerBrush(isDark: Boolean): androidx.compose.ui.graphics.Brush {
        return androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
    }

    fun surfaceBrush(isDark: Boolean, tint: Color = Color.Unspecified): androidx.compose.ui.graphics.Brush {
        val color = surfaceColor(isDark, tint)
        return androidx.compose.ui.graphics.Brush.verticalGradient(listOf(color, color))
    }

    fun spotShadowColor(isDark: Boolean): Color {
        return if (isDark) Color(0xFF000000).copy(alpha = 0.25f) else Color(0xFF0F172A).copy(alpha = 0.05f)
    }
}

/**
 * Modifier extension applying a solid, crisp, elevated Material 3 card surface.
 */
fun Modifier.liquidGlassSurface(
    shape: Shape = RoundedCornerShape(GlassMaterial.DefaultCornerRadius),
    isDark: Boolean = false,
    tint: Color = Color.Unspecified,
    elevation: Dp = 1.dp,
    borderWidth: Dp = GlassMaterial.BorderWidth
): Modifier {
    val containerColor = GlassMaterial.surfaceColor(isDark, tint)
    val borderColor = GlassMaterial.borderColor(isDark)

    val base = if (elevation > 0.dp) {
        this.shadow(
            elevation = elevation,
            shape = shape,
            spotColor = GlassMaterial.spotShadowColor(isDark),
            ambientColor = Color.Transparent
        )
    } else {
        this
    }

    return base
        .clip(shape)
        .background(containerColor, shape)
        .border(borderWidth, borderColor, shape)
}

/**
 * Modifier for solid, clean docked or floating navigation containers.
 */
fun Modifier.liquidGlassNavigation(
    shape: Shape = RoundedCornerShape(GlassMaterial.PillCornerRadius),
    isDark: Boolean = false,
    elevation: Dp = 3.dp,
    borderWidth: Dp = GlassMaterial.BorderWidth
): Modifier {
    val containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val borderColor = GlassMaterial.borderColor(isDark)

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            spotColor = GlassMaterial.spotShadowColor(isDark),
            ambientColor = Color.Transparent
        )
        .clip(shape)
        .background(containerColor, shape)
        .border(borderWidth, borderColor, shape)
}
