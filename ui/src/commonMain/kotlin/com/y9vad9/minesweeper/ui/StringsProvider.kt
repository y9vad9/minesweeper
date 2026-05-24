package com.y9vad9.minesweeper.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.y9vad9.minesweeper.logic.AppLocale

fun stringsFor(locale: AppLocale): Strings = when (locale) {
    AppLocale.En -> EnglishStrings
    AppLocale.Uk -> UkrainianStrings
}

val LocalStrings = staticCompositionLocalOf { EnglishStrings }
