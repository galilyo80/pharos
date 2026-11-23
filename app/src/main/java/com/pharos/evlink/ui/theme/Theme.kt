package com.pharos.evlink.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ---- Pharos palette: deep night-blue with a luminous cyan accent ("the lighthouse") ---- */
val PharosBg        = Color(0xFF05070F)
val PharosSurface   = Color(0xFF0A1022)
val PharosSurface2  = Color(0xFF0C1326)
val PharosCyan      = Color(0xFF38BDF8)
val PharosTeal      = Color(0xFF2DD4BF)
val PharosGreen     = Color(0xFF10B981)
val PharosAmber     = Color(0xFFF59E0B)
val PharosRed       = Color(0xFFEF4444)
val PharosText      = Color(0xFFEAF1FF)
val PharosTextDim   = Color(0xFF7E8BB0)

private val PharosColors = darkColorScheme(
    primary = PharosCyan,
    onPrimary = Color(0xFF04121F),
    secondary = PharosTeal,
    background = PharosBg,
    onBackground = PharosText,
    surface = PharosSurface,
    onSurface = PharosText,
    surfaceVariant = PharosSurface2,
    error = PharosRed
)

private val PharosShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

private val PharosType = Typography(
    headlineMedium = Typography().headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
    titleLarge = Typography().titleLarge.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = Typography().labelLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
)

@Composable
fun PharosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PharosColors,           // always dark — premium automotive look
        shapes = PharosShapes,
        typography = PharosType,
        content = content
    )
}
