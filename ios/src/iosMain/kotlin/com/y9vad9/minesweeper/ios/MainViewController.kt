package com.y9vad9.minesweeper.ios

import androidx.compose.ui.window.ComposeUIViewController
import com.y9vad9.minesweeper.ui.App
import com.y9vad9.minesweeper.ui.CellInputModifier
import com.y9vad9.minesweeper.ui.ClassicFonts
import com.y9vad9.minesweeper.ui.GameStore
import com.y9vad9.minesweeper.ui.HistoryStore
import com.y9vad9.minesweeper.ui.LineChartPainter
import com.y9vad9.minesweeper.ui.SettingsStore
import com.y9vad9.minesweeper.ui.StoreQualifier
import com.y9vad9.minesweeper.ui.appStoresModule
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    KoinApplication(application = { modules(iosModule, appStoresModule) }) {
        App(
            settingsStore = koinInject<SettingsStore>(qualifier = named(StoreQualifier.SETTINGS)),
            gameStore = koinInject<GameStore>(qualifier = named(StoreQualifier.GAME)),
            historyStore = koinInject<HistoryStore>(qualifier = named(StoreQualifier.HISTORY)),
            classicFonts = koinInject<ClassicFonts>(),
            cellInput = koinInject<CellInputModifier>(),
            lineChart = koinInject<LineChartPainter>(),
        )
    }
}
