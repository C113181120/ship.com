package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = WaveMintLight,
    onPrimary = MarinePrimaryDark,
    primaryContainer = MarinePrimary,
    onPrimaryContainer = Color.White,
    secondary = MarineSecondary,
    tertiary = WaveMint,
    background = MarineBackgroundDark,
    surface = MarineSurfaceDark,
    onBackground = OnMarineBackgroundDark,
    onSurface = Color.White,
    error = CoralRed
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MarinePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBFE9FF),
    onPrimaryContainer = MarinePrimaryDark,
    secondary = MarineSecondaryDark,
    tertiary = WaveMint,
    background = MarineBackgroundLight,
    surface = MarineSurfaceLight,
    onBackground = Color(0xFF191C1E),
    onSurface = Color(0xFF191C1E),
    error = CoralRed,

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
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

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
