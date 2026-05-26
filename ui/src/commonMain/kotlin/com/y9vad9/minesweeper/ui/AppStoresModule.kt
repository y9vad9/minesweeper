package com.y9vad9.minesweeper.ui

import com.y9vad9.minesweeper.GameStateRepository
import com.y9vad9.minesweeper.HistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module

object StoreQualifier {
    const val APP_SCOPE = "appScope"
    const val GAME = "gameStore"
    const val HISTORY = "historyStore"
    const val SETTINGS = "settingsStore"
}

val appStoresModule = module {
    single<CoroutineScope>(named(StoreQualifier.APP_SCOPE)) {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
    single<GameStore>(named(StoreQualifier.GAME)) {
        gameStore(
            scope = get(named(StoreQualifier.APP_SCOPE)),
            persistence = getOrNull<GameStateRepository>(),
        )
    }
    single<HistoryStore>(named(StoreQualifier.HISTORY)) {
        historyStore(scope = get(named(StoreQualifier.APP_SCOPE)), repository = get<HistoryRepository>())
    }
    single<SettingsStore>(named(StoreQualifier.SETTINGS)) {
        settingsStore(scope = get(named(StoreQualifier.APP_SCOPE)), repository = get<SettingsRepository>())
    }
}
