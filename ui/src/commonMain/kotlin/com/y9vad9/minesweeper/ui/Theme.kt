package com.y9vad9.minesweeper.ui

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.y9vad9.minesweeper.logic.Skin
import com.y9vad9.minesweeper.logic.ThemeMode

private val DarkBg = Color(0xFF0E0E0F)
private val DarkSurface = Color(0xFF1A1A1E)
private val DarkSurfaceElevated = Color(0xFF24242A)
private val DarkOnBg = Color(0xFFE8E8EA)
private val DarkMuted = Color(0xFF8A8A92)
private val DarkAccent = Color(0xFFE8C97A)
private val DarkDanger = Color(0xFFE87A7A)

private val DarkScheme = darkColorScheme(
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceElevated,
    onBackground = DarkOnBg,
    onSurface = DarkOnBg,
    onSurfaceVariant = DarkMuted,
    primary = DarkAccent,
    onPrimary = DarkBg,
    secondary = DarkMuted,
    error = DarkDanger,
)

internal val DarkGameColors = GameColors(
    hiddenCell = DarkSurfaceElevated,
    hiddenCellHover = Color(0xFF2E2E36),
    revealedCell = DarkSurface,
    flag = DarkAccent,
    mine = DarkOnBg,
    mineExploded = DarkDanger,
    numbers = listOf(
        Color(0xFF88B8F0),
        Color(0xFF9DD89D),
        Color(0xFFE88A8A),
        Color(0xFFB48DE0),
        Color(0xFFE8B87A),
        Color(0xFF7ACFE0),
        Color(0xFFCFCFCF),
        Color(0xFF888892),
    ),
)

private val LightBg = Color(0xFFF7F7F8)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceElevated = Color(0xFFEDEDF0)
private val LightOnBg = Color(0xFF1A1A1E)
private val LightMuted = Color(0xFF6E6E76)
private val LightAccent = Color(0xFFB8902F)
private val LightDanger = Color(0xFFC04A4A)

private val LightScheme = lightColorScheme(
    background = LightBg,
    surface = LightSurface,
    surfaceVariant = LightSurfaceElevated,
    onBackground = LightOnBg,
    onSurface = LightOnBg,
    onSurfaceVariant = LightMuted,
    primary = LightAccent,
    onPrimary = Color.White,
    secondary = LightMuted,
    error = LightDanger,
)

internal val LightGameColors = GameColors(
    hiddenCell = LightSurfaceElevated,
    hiddenCellHover = Color(0xFFDFDFE5),
    revealedCell = LightSurface,
    flag = LightAccent,
    mine = LightOnBg,
    mineExploded = LightDanger,
    numbers = listOf(
        Color(0xFF1F5DB5),
        Color(0xFF2F7A2F),
        Color(0xFFB52F2F),
        Color(0xFF6B3DAA),
        Color(0xFF9A6315),
        Color(0xFF1F8AA0),
        Color(0xFF3D3D45),
        Color(0xFF54545C),
    ),
)

private val ClassicScheme = lightColorScheme(
    background = ClassicChromeTokens.background,
    surface = ClassicChromeTokens.surface,
    surfaceVariant = ClassicChromeTokens.surfaceVariant,
    onBackground = ClassicChromeTokens.onBackground,
    onSurface = ClassicChromeTokens.onSurface,
    onSurfaceVariant = ClassicChromeTokens.onSurfaceVariant,
    primary = ClassicChromeTokens.primary,
    onPrimary = ClassicChromeTokens.onPrimary,
    secondary = ClassicChromeTokens.secondary,
    error = ClassicChromeTokens.error,
)

class GameColors(
    val hiddenCell: Color,
    val hiddenCellHover: Color,
    val revealedCell: Color,
    val flag: Color,
    val mine: Color,
    val mineExploded: Color,
    val numbers: List<Color>,
)

val LocalGameColors = staticCompositionLocalOf { DarkGameColors }

private fun typographyWith(sans: FontFamily, boldByDefault: Boolean = false): Typography {
    val base = Typography()
    fun TextStyle.withSans() = copy(
        fontFamily = sans,
        fontWeight = if (boldByDefault) FontWeight.Bold else fontWeight,
    )
    return base.copy(
        displayLarge = base.displayLarge.withSans(),
        displayMedium = base.displayMedium.withSans(),
        displaySmall = base.displaySmall.withSans(),
        headlineLarge = base.headlineLarge.withSans(),
        headlineMedium = base.headlineMedium.withSans(),
        headlineSmall = base.headlineSmall.withSans(),
        titleLarge = base.titleLarge.withSans(),
        titleMedium = base.titleMedium.withSans(),
        titleSmall = base.titleSmall.withSans(),
        bodyLarge = base.bodyLarge.withSans(),
        bodyMedium = base.bodyMedium.withSans(),
        bodySmall = base.bodySmall.withSans(),
        labelLarge = base.labelLarge.withSans(),
        labelMedium = base.labelMedium.withSans(),
        labelSmall = base.labelSmall.withSans(),
    )
}

private val MinimalTypography = typographyWith(sans = FontFamily.SansSerif)

@Composable
private fun resolveLookAndFeel(skin: Skin, themeMode: ThemeMode): LookAndFeel = when (skin) {
    Skin.Classic -> classicLookAndFeel()
    Skin.Minimal -> when (themeMode) {
        ThemeMode.Light -> MinimalLightLookAndFeel
        ThemeMode.Dark, ThemeMode.System -> MinimalDarkLookAndFeel
    }
}

fun isDarkBackground(skin: Skin, themeMode: ThemeMode): Boolean = when (skin) {
    Skin.Classic -> false
    Skin.Minimal -> themeMode != ThemeMode.Light
}

@Composable
fun MinesweeperTheme(
    themeMode: ThemeMode = ThemeMode.System,
    skin: Skin = Skin.Minimal,
    content: @Composable () -> Unit,
) {
    val laf = resolveLookAndFeel(skin, themeMode)
    val scheme = when (skin) {
        Skin.Classic -> ClassicScheme
        Skin.Minimal -> when (themeMode) {
            ThemeMode.Light -> LightScheme
            ThemeMode.Dark, ThemeMode.System -> DarkScheme
        }
    }
    val classicFonts = LocalClassicFonts.current
    val typography = if (skin == Skin.Classic) {
        remember(classicFonts) { typographyWith(sans = classicFonts.sans, boldByDefault = true) }
    } else MinimalTypography

    MaterialTheme(colorScheme = scheme, typography = typography) {
        CompositionLocalProvider(
            LocalContentColor provides scheme.onBackground,
            LocalTextStyle provides typography.bodyMedium,
            LocalGameColors provides laf.colors,
            LocalLookAndFeel provides laf,
        ) {
            content()
        }
    }
}
