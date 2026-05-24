package com.y9vad9.minesweeper.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily

interface ClassicFonts {
    val sans: FontFamily
    val mono: FontFamily
}

private object DefaultClassicFonts : ClassicFonts {
    override val sans: FontFamily = FontFamily.SansSerif
    override val mono: FontFamily = FontFamily.Monospace
}

val LocalClassicFonts = staticCompositionLocalOf<ClassicFonts> { DefaultClassicFonts }
