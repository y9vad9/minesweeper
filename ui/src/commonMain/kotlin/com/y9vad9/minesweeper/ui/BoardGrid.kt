package com.y9vad9.minesweeper.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import com.y9vad9.minesweeper.logic.Cell
import com.y9vad9.minesweeper.logic.Position

private const val LAZY_CELL_THRESHOLD = 2500

fun isLazyBoard(rows: Int, cols: Int): Boolean = rows.toLong() * cols > LAZY_CELL_THRESHOLD

@Composable
fun BoardGrid(
    rows: Int,
    cols: Int,
    cellAt: (Position) -> Cell,
    cellSize: Dp,
    onReveal: (Position) -> Unit,
    onFlag: (Position) -> Unit,
    onChord: (Position) -> Unit,
    modifier: Modifier = Modifier,
    gap: Dp = LocalLookAndFeel.current.gridGap,
) {
    if (isLazyBoard(rows, cols)) {
        LazyBoardGrid(rows, cols, cellAt, cellSize, onReveal, onFlag, onChord, modifier, gap)
    } else {
        StaticBoardGrid(rows, cols, cellAt, cellSize, onReveal, onFlag, onChord, modifier, gap)
    }
}

@Composable
private fun StaticBoardGrid(
    rows: Int,
    cols: Int,
    cellAt: (Position) -> Cell,
    cellSize: Dp,
    onReveal: (Position) -> Unit,
    onFlag: (Position) -> Unit,
    onChord: (Position) -> Unit,
    modifier: Modifier,
    gap: Dp,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(gap)) {
        for (r in 0 until rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                for (c in 0 until cols) {
                    val pos = Position(r, c)
                    CellView(
                        cell = cellAt(pos),
                        size = cellSize,
                        onReveal = { onReveal(pos) },
                        onFlag = { onFlag(pos) },
                        onChord = { onChord(pos) },
                        modifier = Modifier.testTag(cellTag(r, c)),
                    )
                }
            }
        }
    }
}

@Composable
private fun LazyBoardGrid(
    rows: Int,
    cols: Int,
    cellAt: (Position) -> Cell,
    cellSize: Dp,
    onReveal: (Position) -> Unit,
    onFlag: (Position) -> Unit,
    onChord: (Position) -> Unit,
    modifier: Modifier,
    gap: Dp,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(cols),
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        items(count = rows * cols, key = { it }) { index ->
            val pos = Position(index / cols, index % cols)
            CellView(
                cell = cellAt(pos),
                size = cellSize,
                fill = true,
                onReveal = { onReveal(pos) },
                onFlag = { onFlag(pos) },
                onChord = { onChord(pos) },
                modifier = Modifier.testTag(cellTag(pos.row, pos.col)),
            )
        }
    }
}

fun cellTag(row: Int, col: Int): String = "cell-$row-$col"
