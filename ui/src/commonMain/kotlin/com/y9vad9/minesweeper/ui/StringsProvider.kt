package com.y9vad9.minesweeper.ui

import androidx.compose.runtime.staticCompositionLocalOf

fun stringsFor(locale: AppLocale): Strings = when (locale) {
    AppLocale.En -> EnglishStrings
    AppLocale.Uk -> UkrainianStrings
}

val LocalStrings = staticCompositionLocalOf { EnglishStrings }
