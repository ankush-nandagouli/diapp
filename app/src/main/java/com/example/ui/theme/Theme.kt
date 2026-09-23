package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme =
  darkColorScheme(
    primary = Color.White,
    onPrimary = DakshyamNavy,
    primaryContainer = DakshyamNavyBlue,
    onPrimaryContainer = Color.White,
    secondary = DakshyamCyan,
    onSecondary = DakshyamNavy,
    secondaryContainer = DakshyamLightNavy,
    onSecondaryContainer = Color.White,
    tertiary = DakshyamSky,
    background = DakshyamDarkNavy,
    surface = DarkCard,
    surfaceVariant = DakshyamLightNavy,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF233554),
    outlineVariant = Color(0xFF1B2A4A)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = DakshyamNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF0F7FF), // Crisp clean ice-blue container
    onPrimaryContainer = DakshyamNavy,
    secondary = DakshyamNavyBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = DakshyamNavy,
    tertiary = DakshyamTeal,
    background = Color(0xFFF8FAFC), // Modern snappy off-white background
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F5F9),
    onBackground = DakshyamNavy,
    onSurface = DakshyamNavy,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
  )

val SnappyShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Use our customized Navy Blue and White palette consistently for instant snappy loading
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      shapes = SnappyShapes,
      content = content
  )
}
