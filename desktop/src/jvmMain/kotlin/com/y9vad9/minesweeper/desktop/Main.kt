package com.y9vad9.minesweeper.desktop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.y9vad9.minesweeper.logic.AppLocale
import com.y9vad9.minesweeper.logic.Skin
import com.y9vad9.minesweeper.logic.ThemeMode
import com.y9vad9.minesweeper.ui.App
import com.y9vad9.minesweeper.ui.ClassicFonts
import com.y9vad9.minesweeper.ui.CellInputModifier
import com.y9vad9.minesweeper.ui.GameIntent
import com.y9vad9.minesweeper.ui.GameStore
import com.y9vad9.minesweeper.ui.HistoryStore
import com.y9vad9.minesweeper.ui.LineChartPainter
import com.y9vad9.minesweeper.ui.SettingsIntent
import com.y9vad9.minesweeper.ui.SettingsStore
import com.y9vad9.minesweeper.ui.StoreQualifier
import com.y9vad9.minesweeper.ui.appStoresModule
import com.y9vad9.minesweeper.ui.stringsFor
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import pro.respawn.flowmvi.compose.dsl.subscribe
import javax.swing.SwingUtilities

fun main() = application {
    KoinApplication(application = { modules(desktopModule, appStoresModule) }) {
        AppWindow()
    }
}

@Suppress("FunctionName")
@Composable
private fun ApplicationScope.AppWindow() {
    val classicFonts = koinInject<ClassicFonts>()
    val cellInput = koinInject<CellInputModifier>()
    val lineChart = koinInject<LineChartPainter>()
    val settings = koinInject<SettingsStore>(qualifier = named(StoreQualifier.SETTINGS))
    val game = koinInject<GameStore>(qualifier = named(StoreQualifier.GAME))
    val history = koinInject<HistoryStore>(qualifier = named(StoreQualifier.HISTORY))

    val windowState = rememberWindowState(size = DpSize(900.dp, 700.dp))
    Window(
        onCloseRequest = ::exitApplication,
        title = "Minesweeper",
        state = windowState,
    ) {
        val state by settings.subscribe()
        val strings = stringsFor(state.settings.locale)

        val appearance = resolveWindowAppearance(state.settings.skin, state.settings.theme)
        LaunchedEffect(appearance, window) {
            if (!System.getProperty("os.name", "").lowercase().contains("mac")) return@LaunchedEffect
            SwingUtilities.invokeLater {
                window.rootPane.putClientProperty("apple.awt.windowAppearance", appearance)
            }
        }

        MenuBar {
            Menu(strings.menuGame) {
                Item(strings.menuNewGame, onClick = { game.intent(GameIntent.NewGameRequested) })
                Separator()
                Item(strings.menuQuit, onClick = ::exitApplication)
            }
            Menu(strings.menuSettings) {
                Menu(strings.skinLabel) {
                    for (skin in Skin.entries) {
                        CheckboxItem(
                            text = strings.skin(skin),
                            checked = state.settings.skin == skin,
                            onCheckedChange = { settings.intent(SettingsIntent.SkinChanged(skin)) },
                        )
                    }
                }
                if (state.settings.skin != Skin.Classic) {
                    Menu(strings.themeLabel) {
                        for (mode in ThemeMode.entries) {
                            CheckboxItem(
                                text = strings.theme(mode),
                                checked = state.settings.theme == mode,
                                onCheckedChange = { settings.intent(SettingsIntent.ThemeChanged(mode)) },
                            )
                        }
                    }
                }
                Menu(strings.languageLabel) {
                    for (locale in AppLocale.entries) {
                        CheckboxItem(
                            text = locale.nativeName,
                            checked = state.settings.locale == locale,
                            onCheckedChange = { settings.intent(SettingsIntent.LocaleChanged(locale)) },
                        )
                    }
                }
            }
        }

        App(
            settingsStore = settings,
            gameStore = game,
            historyStore = history,
            classicFonts = classicFonts,
            cellInput = cellInput,
            lineChart = lineChart,
        )
    }
}

private fun resolveWindowAppearance(skin: Skin, theme: ThemeMode): String? = when {
    skin == Skin.Classic -> "NSAppearanceNameAqua"
    theme == ThemeMode.Dark -> "NSAppearanceNameDarkAqua"
    theme == ThemeMode.Light -> "NSAppearanceNameAqua"
    else -> null
}
