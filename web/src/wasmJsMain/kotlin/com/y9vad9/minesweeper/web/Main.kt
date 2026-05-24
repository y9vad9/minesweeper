package com.y9vad9.minesweeper.web

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.y9vad9.minesweeper.ui.App
import com.y9vad9.minesweeper.ui.CellInputModifier
import com.y9vad9.minesweeper.ui.GameStore
import com.y9vad9.minesweeper.ui.HistoryStore
import com.y9vad9.minesweeper.ui.LineChartPainter
import com.y9vad9.minesweeper.ui.LocalEmojiGlyphs
import com.y9vad9.minesweeper.ui.ClassicFonts
import com.y9vad9.minesweeper.ui.SettingsStore
import com.y9vad9.minesweeper.ui.StoreQualifier
import com.y9vad9.minesweeper.ui.appStoresModule
import kotlinx.browser.document
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        KoinApplication(application = { modules(webModule, appStoresModule) }) {
            CompositionLocalProvider(LocalEmojiGlyphs provides false) {
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
    }
}
