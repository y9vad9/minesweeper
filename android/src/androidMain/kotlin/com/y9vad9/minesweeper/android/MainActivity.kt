package com.y9vad9.minesweeper.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.y9vad9.minesweeper.ui.App
import com.y9vad9.minesweeper.ui.CellInputModifier
import com.y9vad9.minesweeper.ui.ClassicFonts
import com.y9vad9.minesweeper.ui.GameStore
import com.y9vad9.minesweeper.ui.HistoryStore
import com.y9vad9.minesweeper.ui.LineChartPainter
import com.y9vad9.minesweeper.ui.SettingsStore
import com.y9vad9.minesweeper.ui.StoreQualifier
import com.y9vad9.minesweeper.ui.isDarkBackground
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import pro.respawn.flowmvi.compose.dsl.subscribe

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsStore = koinInject<SettingsStore>(qualifier = named(StoreQualifier.SETTINGS))
            val settingsState by settingsStore.subscribe()

            val darkBars = isDarkBackground(settingsState.settings.skin, settingsState.settings.theme)
            val view = LocalView.current
            LaunchedEffect(darkBars) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkBars
                controller.isAppearanceLightNavigationBars = !darkBars
            }

            App(
                settingsStore = settingsStore,
                gameStore = koinInject<GameStore>(qualifier = named(StoreQualifier.GAME)),
                historyStore = koinInject<HistoryStore>(qualifier = named(StoreQualifier.HISTORY)),
                classicFonts = koinInject<ClassicFonts>(),
                cellInput = koinInject<CellInputModifier>(),
                lineChart = koinInject<LineChartPainter>(),
            )
        }
    }
}
