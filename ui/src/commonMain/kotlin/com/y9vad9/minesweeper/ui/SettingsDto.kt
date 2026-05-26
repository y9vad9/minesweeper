package com.y9vad9.minesweeper.ui

import kotlinx.serialization.Serializable

@Serializable
internal data class SettingsDto(
    val locale: String = AppLocale.En.tag,
    val theme: String = ThemeMode.System.name,
    val skin: String = Skin.Minimal.name,
)

internal fun Settings.toDto(): SettingsDto = SettingsDto(
    locale = locale.tag,
    theme = theme.name,
    skin = skin.name,
)

internal fun SettingsDto.toDomain(): Settings = Settings(
    locale = AppLocale.fromTag(locale) ?: AppLocale.En,
    theme = runCatching { ThemeMode.valueOf(theme) }.getOrDefault(ThemeMode.System),
    skin = runCatching { Skin.valueOf(skin) }.getOrDefault(Skin.Minimal),
)
