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
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFCCE8E4),
    onSecondaryContainer = Color(0xFF003833),
    tertiary = Color(0xFF018786),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFBCEBE5),
    onTertiaryContainer = Color(0xFF00201F),
    background = Color(0xFFF8F9FA),  // Very light gray/white
    onBackground = Color(0xFF212529),
    surface = Color(0xFFFFFFFF),     // White card surfaces
    onSurface = Color(0xFF212529),
    surfaceVariant = Color(0xFFE9ECEF),
    onSurfaceVariant = Color(0xFF49454F),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
)

// 1.5 Awan Wayang Dark Theme
private val AwanWayangDarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF4A00B0),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF005047),
    onSecondaryContainer = Color(0xFFCCE8E4),
    tertiary = Color(0xFF3700B3),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF1E0066),
    onTertiaryContainer = Color(0xFFD0BCFF),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFCAC4D0),
    error = Color(0xFFCF6679),
    onError = Color.Black,
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC)
)

// 2. Dark Futuristic - Cyberpunk/Neon vibe
private val DarkFuturisticColorScheme = darkColorScheme(
    primary = Color(0xFF00FFCC),     // Neon Cyan
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF006652),
    onPrimaryContainer = Color(0xFFB3FFF0),
    secondary = Color(0xFFFF00FF),   // Neon Magenta
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF800080),
    onSecondaryContainer = Color(0xFFFFB3FF),
    tertiary = Color(0xFFB026FF),    // Neon Purple
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF5D0099),
    onTertiaryContainer = Color(0xFFE6B3FF),
    background = Color(0xFF0D0E15),  // Very dark blue/black
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF151828),     // Slightly lighter dark blue
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1E2336),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = Color(0xFFFF4D4D),
    onError = Color.Black,
    errorContainer = Color(0xFF990000),
    onErrorContainer = Color(0xFFFFB3B3)
)

// 3. Nature Forest - Earthy, green tones
private val NatureForestColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),     // Forest Green
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF003300),
    secondary = Color(0xFF8D6E63),   // Brown
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7CCC8),
    onSecondaryContainer = Color(0xFF3E2723),
    tertiary = Color(0xFFAED581),    // Light Green
    onTertiary = Color(0xFF1B5E20),
    tertiaryContainer = Color(0xFFF1F8E9),
    onTertiaryContainer = Color(0xFF1B5E20),
    background = Color(0xFFF1F8E9),  // Light green tint
    onBackground = Color(0xFF1E3314),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1E3314),
    surfaceVariant = Color(0xFFDCEDC8),
    onSurfaceVariant = Color(0xFF33691E),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
)

// 4. Ocean Blue - Deep ocean colors
private val OceanBlueColorScheme = lightColorScheme(
    primary = Color(0xFF0277BD),     // Ocean Blue
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB3E5FC),
    onPrimaryContainer = Color(0xFF00384D),
    secondary = Color(0xFF00ACC1),   // Cyan
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2EBF2),
    onSecondaryContainer = Color(0xFF003B46),
    tertiary = Color(0xFF4DD0E1),    // Light Cyan
    onTertiary = Color(0xFF003B46),
    tertiaryContainer = Color(0xFFE0F7FA),
    onTertiaryContainer = Color(0xFF003B46),
    background = Color(0xFFE1F5FE),  // Very light blue
    onBackground = Color(0xFF014361),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF014361),
    surfaceVariant = Color(0xFFB3E5FC),
    onSurfaceVariant = Color(0xFF005B82),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
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
