package com.y9vad9.minesweeper.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.y9vad9.minesweeper.GameState
import com.y9vad9.minesweeper.GameRecord
import com.y9vad9.minesweeper.computeStats
import com.y9vad9.minesweeper.threeBV
import pro.respawn.flowmvi.compose.dsl.subscribe
import kotlin.time.Clock

@Composable
fun App(
    settingsStore: SettingsStore,
    gameStore: GameStore,
    historyStore: HistoryStore,
    classicFonts: ClassicFonts,
    cellInput: CellInputModifier,
    lineChart: LineChartPainter,
) {
    val settingsState by settingsStore.subscribe()

    CompositionLocalProvider(
        LocalClassicFonts provides classicFonts,
        LocalCellInput provides cellInput,
        LocalLineChart provides lineChart,
    ) {
        MinesweeperTheme(
            themeMode = settingsState.settings.theme,
            skin = settingsState.settings.skin,
        ) {
            CompositionLocalProvider(LocalStrings provides stringsFor(settingsState.settings.locale)) {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    Box(
                        Modifier.fillMaxSize().windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                        ),
                    ) {
                        AppRoot(settingsStore, gameStore, historyStore)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppRoot(
    settingsStore: SettingsStore,
    gameStore: GameStore,
    historyStore: HistoryStore,
) {
    val gameState by gameStore.subscribe()
    val historyState by historyStore.subscribe()
    val settingsState by settingsStore.subscribe()

    LaunchedEffect(gameState.game) {
        val terminal = gameState.game
        val board = when (terminal) {
            is GameState.Won -> terminal.board
            is GameState.Lost -> terminal.board
            else -> null
        }
        if (board != null) {
            val threeBV = board.threeBV()
            GameRecord.fromFinished(
                state = terminal,
                nowEpochMillis = Clock.System.now().toEpochMilliseconds(),
                threeBV = threeBV,
            )?.let { historyStore.intent(HistoryIntent.Add(it)) }
        }
    }

    var currentScreen by remember { mutableStateOf(Screen.Game) }

    val onReplay: (GameRecord) -> Unit = { record ->
        gameStore.intent(
            GameIntent.ReplayRequested(record.rows, record.cols, record.mines, record.seed)
        )
        currentScreen = Screen.Game
    }
    val onClearHistory = { historyStore.intent(HistoryIntent.Clear) }

    val stats = remember(historyState.records) { computeStats(historyState.records) }
    val currentDifficulty = gameState.difficulty
    val bestTimeMillis = stats.forDifficulty(currentDifficulty).bestTimeMillis

    val gameContent: @Composable () -> Unit = {
        GameScreen(gameStore, Modifier.fillMaxSize(), bestTimeMillis = bestTimeMillis)
    }
    val historyContent: @Composable () -> Unit = {
        HistoryScreen(
            records = historyState.records,
            onReplay = onReplay,
            onClear = onClearHistory,
            modifier = Modifier.fillMaxSize(),
        )
    }
    val settingsContent: @Composable () -> Unit = {
        SettingsScreen(
            settings = settingsState.settings,
            onLocaleChange = { settingsStore.intent(SettingsIntent.LocaleChanged(it)) },
            onThemeChange = { settingsStore.intent(SettingsIntent.ThemeChanged(it)) },
            onSkinChange = { settingsStore.intent(SettingsIntent.SkinChanged(it)) },
            modifier = Modifier.fillMaxSize(),
        )
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val widthClass = classifyWidth(maxWidth)
        when (widthClass) {
            WindowWidthClass.Compact -> CompactLayout(
                screen = currentScreen,
                onScreen = { currentScreen = it },
                gameContent = gameContent,
                historyContent = historyContent,
                settingsContent = settingsContent,
            )
            WindowWidthClass.Medium, WindowWidthClass.Expanded -> TwoPaneLayout(
                screen = currentScreen,
                onScreen = { currentScreen = it },
                gameContent = gameContent,
                historyContent = historyContent,
                settingsContent = settingsContent,
                sidePaneWidth = if (widthClass == WindowWidthClass.Expanded) 320.dp else 280.dp,
            )
        }
    }
}

@Composable
private fun CompactLayout(
    screen: Screen,
    onScreen: (Screen) -> Unit,
    gameContent: @Composable () -> Unit,
    historyContent: @Composable () -> Unit,
    settingsContent: @Composable () -> Unit,
) {
    val strings = LocalStrings.current
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().weight(1f)) {
            when (screen) {
                Screen.Game -> gameContent()
                Screen.History -> historyContent()
                Screen.Settings -> settingsContent()
            }
        }
        AppBottomNav(
            items = listOf(
                BottomNavItem(
                    selected = screen == Screen.Game,
                    onClick = { onScreen(Screen.Game) },
                    label = strings.gameTab,
                    icon = MaterialIcons.VideogameAsset,
                ),
                BottomNavItem(
                    selected = screen == Screen.History,
                    onClick = { onScreen(Screen.History) },
                    label = strings.historyTab,
                    icon = MaterialIcons.History,
                ),
                BottomNavItem(
                    selected = screen == Screen.Settings,
                    onClick = { onScreen(Screen.Settings) },
                    label = strings.settingsTab,
                    icon = MaterialIcons.Settings,
                ),
            ),
        )
    }
}

@Composable
private fun TwoPaneLayout(
    screen: Screen,
    onScreen: (Screen) -> Unit,
    gameContent: @Composable () -> Unit,
    historyContent: @Composable () -> Unit,
    settingsContent: @Composable () -> Unit,
    sidePaneWidth: Dp,
) {
    val strings = LocalStrings.current
    val sideScreen = when (screen) {
        Screen.Settings -> Screen.Settings
        else -> Screen.History
    }
    Row(
        Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
    ) {
        Box(Modifier.weight(1f).fillMaxHeight()) { gameContent() }
        AppVerticalDivider()
        Column(Modifier.width(sidePaneWidth).fillMaxHeight()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SidePaneTab(
                    selected = sideScreen == Screen.History,
                    label = strings.historyTab,
                    onClick = { onScreen(Screen.History) },
                )
                SidePaneTab(
                    selected = sideScreen == Screen.Settings,
                    label = strings.settingsTab,
                    onClick = { onScreen(Screen.Settings) },
                )
            }
            AppHorizontalDivider()
            Box(Modifier.fillMaxWidth().weight(1f)) {
                when (sideScreen) {
                    Screen.Settings -> settingsContent()
                    else -> historyContent()
                }
            }
        }
    }
}

@Composable
private fun SidePaneTab(selected: Boolean, label: String, onClick: () -> Unit) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    AppTextButton(onClick = onClick) {
        Text(label, color = color, style = MaterialTheme.typography.labelMedium)
    }
}
