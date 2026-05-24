package com.y9vad9.minesweeper.ui

import com.y9vad9.minesweeper.logic.AppLocale
import com.y9vad9.minesweeper.logic.Settings
import com.y9vad9.minesweeper.logic.SettingsRepository
import com.y9vad9.minesweeper.logic.Skin
import com.y9vad9.minesweeper.logic.ThemeMode
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce

data class SettingsState(
    val settings: Settings = Settings(),
    val loaded: Boolean = false,
) : MVIState

sealed interface SettingsIntent : MVIIntent {
    data class LocaleChanged(val locale: AppLocale) : SettingsIntent
    data class ThemeChanged(val theme: ThemeMode) : SettingsIntent
    data class SkinChanged(val skin: Skin) : SettingsIntent
}

typealias SettingsStore = Store<SettingsState, SettingsIntent, Nothing>

fun settingsStore(
    scope: CoroutineScope,
    repository: SettingsRepository,
): SettingsStore = store(
    initial = SettingsState(),
    scope = scope,
) {
    recover { null }

    init {
        val settings = repository.load()
        updateState { copy(settings = settings, loaded = true) }
    }

    reduce { intent: SettingsIntent ->
        updateState {
            val next = when (intent) {
                is SettingsIntent.LocaleChanged -> settings.copy(locale = intent.locale)
                is SettingsIntent.ThemeChanged -> settings.copy(theme = intent.theme)
                is SettingsIntent.SkinChanged -> settings.copy(skin = intent.skin)
            }
            repository.save(next)
            copy(settings = next)
        }
    }
}
