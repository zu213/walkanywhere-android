package com.zachupstone.walkanywhere.ui.theme

import android.app.Activity
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

private val DarkColorScheme = darkColorScheme(
    primary = Grey80,
    secondary = SlateGrey80,
    tertiary = Silver80,
    primaryContainer = Color(0xFF3C4043),
    onPrimaryContainer = Color(0xFFE8EAED),
    secondaryContainer = Color(0xFF44474B),
    onSecondaryContainer = Color(0xFFE3E5E8),
    tertiaryContainer = Color(0xFF474747),
    onTertiaryContainer = Color(0xFFE3E3E3),
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    surfaceVariant = Color(0xFF3A3A3A),
    surfaceContainer = Color(0xFF1E1E1E),
    outline = Color(0xFF8E8E8E)
)

private val LightColorScheme = lightColorScheme(
    primary = Grey40,
    secondary = SlateGrey40,
    tertiary = Silver40,
    primaryContainer = Color(0xFFDADCE0),
    onPrimaryContainer = Color(0xFF202124),
    secondaryContainer = Color(0xFFD5D8DC),
    onSecondaryContainer = Color(0xFF1F2328),
    tertiaryContainer = Color(0xFFE0E0E0),
    onTertiaryContainer = Color(0xFF212121),
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFAFAFA),
    surfaceVariant = Color(0xFFE4E4E4),
    surfaceContainer = Color(0xFFF0F0F0),
    outline = Color(0xFF757575)
)

@Composable
fun WalkAnywhereTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
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
        content = content
    )
}