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

enum class AppThemeType {
    AWAN_WAYANG,
    AWAN_WAYANG_DARK,
    DARK_FUTURISTIC,
    NATURE_FOREST,
    OCEAN_BLUE
}

// 1. Awan Wayang Theme - Light, clean, white dominant
private val AwanWayangColorScheme = lightColorScheme(
    primary = Color(0xFF6200EA),     // Deep Purple for accents
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF018786),
    background = Color(0xFFF8F9FA),  // Very light gray/white
    surface = Color(0xFFFFFFFF),     // White card surfaces
    surfaceVariant = Color(0xFFE9ECEF),
    onPrimary = Color.White,
    onBackground = Color(0xFF212529),
    onSurface = Color(0xFF212529)
)

// 1.5 Awan Wayang Dark Theme
private val AwanWayangDarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF3700B3),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2C2C2C),
    onPrimary = Color.Black,
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0)
)

// 2. Dark Futuristic - Cyberpunk/Neon vibe
private val DarkFuturisticColorScheme = darkColorScheme(
    primary = Color(0xFF00FFCC),     // Neon Cyan
    secondary = Color(0xFFFF00FF),   // Neon Magenta
    tertiary = Color(0xFFB026FF),    // Neon Purple
    background = Color(0xFF0D0E15),  // Very dark blue/black
    surface = Color(0xFF151828),     // Slightly lighter dark blue
    surfaceVariant = Color(0xFF1E2336),
    onPrimary = Color.Black,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0)
)

// 3. Nature Forest - Earthy, green tones
private val NatureForestColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),     // Forest Green
    secondary = Color(0xFF8D6E63),   // Brown
    tertiary = Color(0xFFAED581),    // Light Green
    background = Color(0xFFF1F8E9),  // Light green tint
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFDCEDC8),
    onPrimary = Color.White,
    onBackground = Color(0xFF1E3314),
    onSurface = Color(0xFF1E3314)
)

// 4. Ocean Blue - Deep ocean colors
private val OceanBlueColorScheme = lightColorScheme(
    primary = Color(0xFF0277BD),     // Ocean Blue
    secondary = Color(0xFF00ACC1),   // Cyan
    tertiary = Color(0xFF4DD0E1),    // Light Cyan
    background = Color(0xFFE1F5FE),  // Very light blue
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFB3E5FC),
    onPrimary = Color.White,
    onBackground = Color(0xFF014361),
    onSurface = Color(0xFF014361)
)

@Composable
fun MyApplicationTheme(
    themeType: AppThemeType = AppThemeType.AWAN_WAYANG,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (themeType) {
        AppThemeType.AWAN_WAYANG -> AwanWayangColorScheme
        AppThemeType.AWAN_WAYANG_DARK -> AwanWayangDarkColorScheme
        AppThemeType.DARK_FUTURISTIC -> DarkFuturisticColorScheme
        AppThemeType.NATURE_FOREST -> NatureForestColorScheme
        AppThemeType.OCEAN_BLUE -> OceanBlueColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
