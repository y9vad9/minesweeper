package com.y9vad9.minesweeper.logic

enum class AppLocale(val tag: String, val nativeName: String) {
    En("en", "English"),
    Uk("uk", "Українська");

    companion object {
        fun fromTag(tag: String?): AppLocale? = entries.firstOrNull { it.tag == tag }
    }
}

enum class ThemeMode { Dark, Light, System }

enum class Skin { Minimal, Classic }

data class Settings(
    val locale: AppLocale = AppLocale.En,
    val theme: ThemeMode = ThemeMode.System,
    val skin: Skin = Skin.Minimal,
)
