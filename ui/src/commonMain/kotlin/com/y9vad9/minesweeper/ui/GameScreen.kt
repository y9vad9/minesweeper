package com.y9vad9.minesweeper.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.y9vad9.minesweeper.Cell
import com.y9vad9.minesweeper.GameState
import com.y9vad9.minesweeper.Position
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
fun GameScreen(
    store: GameStore,
    modifier: Modifier = Modifier,
    bestTimeMillis: Long? = null,
) {
    val state by store.subscribe()
    GameScreenContent(state, { store.intent(it) }, modifier, bestTimeMillis)
}

@Composable
private fun GameScreenContent(
    state: GameScreenState,
    send: (GameIntent) -> Unit,
    modifier: Modifier,
    bestTimeMillis: Long? = null,
) {
    val elapsed by rememberElapsed(state.game)

    val rows: Int
    val cols: Int
    val cellAt: (Position) -> Cell
    val flagCount: Int
    when (val g = state.game) {
        is GameState.Idle -> {
            rows = g.config.rows; cols = g.config.cols
            cellAt = { Cell.Hidden(flagged = false) }
            flagCount = 0
        }
        is GameState.Playing -> {
            rows = g.board.rows; cols = g.board.cols
            cellAt = g.board::cellAt
            flagCount = g.board.flagCount
        }
        is GameState.Won -> {
            rows = g.board.rows; cols = g.board.cols
            cellAt = g.board::cellAt
            flagCount = g.board.flagCount
        }
        is GameState.Lost -> {
            rows = g.board.rows; cols = g.board.cols
            cellAt = g.board::cellAt
            flagCount = g.board.flagCount
        }
    }
    val minesRemaining = (state.game.config.mines - flagCount).coerceAtLeast(0)
    val cellSize = cellSizeFor(rows, cols)
    val laf = LocalLookAndFeel.current
    val cellGap = laf.gridGap

    var dragWindow by remember { mutableStateOf<Offset?>(null) }
    var boardRect by remember { mutableStateOf(Rect.Zero) }
    val density = LocalDensity.current
    val cellStridePx = with(density) { (cellSize + cellGap).toPx() }

    fun cellAtWindow(windowOffset: Offset): Position? {
        if (!boardRect.contains(windowOffset)) return null
        val local = windowOffset - boardRect.topLeft
        val r = (local.y / cellStridePx).toInt()
        val c = (local.x / cellStridePx).toInt()
        return if (r in 0 until rows && c in 0 until cols) Position(r, c) else null
    }

    var rootOrigin by remember { mutableStateOf(Offset.Zero) }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 24.dp)
            .onGloballyPositioned { rootOrigin = it.positionInWindow() }
    ) {
        val isCompact = classifyWidth(maxWidth) == WindowWidthClass.Compact
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GameTopBar(
                clock = elapsed.formatClock(),
                bestTimeMillis = bestTimeMillis,
                minesRemaining = minesRemaining,
                flagMode = state.flagMode,
                currentCode = state.currentCode,
                currentDifficulty = state.difficulty,
                seedInput = state.seedInput,
                gameInProgress = state.game is GameState.Playing,
                onSeedInputChange = { send(GameIntent.SeedInputChanged(it)) },
                onDifficultyPicked = { send(GameIntent.DifficultyPicked(it)) },
                onCustomGameRequested = { r, c, m, s -> send(GameIntent.CustomGameRequested(r, c, m, s)) },
                onFlagModeToggle = { send(GameIntent.FlagModeToggled) },
                onFlagDragStart = { dragWindow = it },
                onFlagDrag = { dragWindow = it },
                onFlagDragEnd = {
                    dragWindow?.let { cellAtWindow(it) }?.let { send(GameIntent.CellFlagged(it)) }
                    dragWindow = null
                },
                onFlagDragCancel = { dragWindow = null },
                onNewGame = { send(GameIntent.NewGameRequested) },
                showNewGameButton = !isCompact,
            )
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                val lazyBoard = isLazyBoard(rows, cols)
                val gapsW = cellGap * (cols - 1).coerceAtLeast(0)
                val gapsH = cellGap * (rows - 1).coerceAtLeast(0)
                val boardWidth = cellSize * cols + gapsW
                val boardHeight = cellSize * rows + gapsH
                val needsHScroll = !lazyBoard && boardWidth > maxWidth
                val needsVScroll = !lazyBoard && boardHeight > maxHeight

                val container = Modifier
                    .fillMaxSize()
                    .let { if (needsVScroll) it.verticalScroll(rememberScrollState()) else it }
                    .let { if (needsHScroll) it.horizontalScroll(rememberScrollState()) else it }

                Box(
                    modifier = container,
                    contentAlignment = Alignment.Center,
                ) {
                    laf.boardFrame {
                        Box(
                            modifier = (if (lazyBoard) Modifier.fillMaxSize() else Modifier)
                                .onGloballyPositioned {
                                    boardRect = Rect(
                                        it.positionInWindow(),
                                        Size(it.size.width.toFloat(), it.size.height.toFloat()),
                                    )
                                }
                        ) {
                            BoardGrid(
                                rows = rows,
                                cols = cols,
                                cellAt = cellAt,
                                cellSize = cellSize,
                                onReveal = { pos ->
                                    if (state.flagMode) send(GameIntent.CellFlagged(pos))
                                    else send(GameIntent.CellRevealed(pos))
                                },
                                onFlag = { send(GameIntent.CellFlagged(it)) },
                                onChord = { send(GameIntent.CellChorded(it)) },
                                modifier = if (lazyBoard) Modifier.fillMaxSize() else Modifier,
                            )
                        }
                    }
                }
            }
        }

        val strings = LocalStrings.current
        when (val g = state.game) {
            is GameState.Won -> GameOverlay(
                headline = strings.gameWonHeadline,
                subtitle = "${strings.clearedInPrefix} ${g.elapsed.formatClock()}",
                onNewGame = { send(GameIntent.NewGameRequested) },
            )
            is GameState.Lost -> GameOverlay(
                headline = strings.gameLostHeadline,
                subtitle = "${strings.lastedPrefix} ${g.elapsed.formatClock()}",
                onNewGame = { send(GameIntent.NewGameRequested) },
            )
            else -> Unit
        }

        dragWindow?.let { window ->
            val gameColors = LocalGameColors.current
            Box(
                modifier = Modifier.graphicsLayer {
                    translationX = window.x - rootOrigin.x - 12f
                    translationY = window.y - rootOrigin.y - 12f
                },
            ) {
                laf.flag(24.dp, gameColors.flag, gameColors.mine)
            }
        }

        if (isCompact && state.game !is GameState.Won && state.game !is GameState.Lost) {
            NewGameCompactAffordance(
                currentDifficulty = state.difficulty,
                seedInput = state.seedInput,
                gameInProgress = state.game is GameState.Playing,
                onSeedInputChange = { send(GameIntent.SeedInputChanged(it)) },
                onDifficultyPicked = { send(GameIntent.DifficultyPicked(it)) },
                onCustomGameRequested = { r, c, m, s -> send(GameIntent.CustomGameRequested(r, c, m, s)) },
                onConfirm = { send(GameIntent.NewGameRequested) },
            )
        }
    }
}

private fun cellSizeFor(rows: Int, cols: Int): Dp = when {
    rows <= 10 && cols <= 10 -> 36.dp
    cols <= 20 -> 30.dp
    else -> 24.dp
}
