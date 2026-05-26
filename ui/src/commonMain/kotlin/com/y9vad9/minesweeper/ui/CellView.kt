package com.y9vad9.minesweeper.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.y9vad9.minesweeper.Cell

@Composable
fun CellView(
    cell: Cell,
    size: Dp,
    onReveal: () -> Unit,
    onFlag: () -> Unit,
    onChord: () -> Unit,
    modifier: Modifier = Modifier,
    fill: Boolean = false,
) {
    val colors = LocalGameColors.current
    val laf = LocalLookAndFeel.current
    var hovered by remember { mutableStateOf(false) }

    val background = when (cell) {
        is Cell.Hidden -> if (hovered) colors.hiddenCellHover else colors.hiddenCell
        is Cell.Revealed -> colors.revealedCell
        Cell.ExplodedMine -> colors.mineExploded
        Cell.RevealedMine -> colors.revealedCell
    }
    val isHidden = cell is Cell.Hidden

    val cellInput = LocalCellInput.current
    val sized = if (fill) modifier.fillMaxWidth().aspectRatio(1f) else modifier.size(size)
    val outer = cellInput.apply(
        modifier = sized,
        onReveal = onReveal,
        onFlag = onFlag,
        onChord = onChord,
        onHoverChange = { hovered = it },
    )

    laf.cell(outer, isHidden, background) {
        CellContent(cell, size)
    }
}

@Composable
private fun CellContent(cell: Cell, cellSize: Dp) {
    val colors = LocalGameColors.current
    val laf = LocalLookAndFeel.current
    val glyphSize = cellSize * 0.72f
    when (cell) {
        is Cell.Hidden -> {
            if (cell.flagged) {
                laf.flag(glyphSize, colors.flag, colors.mine)
            } else {
                Box(Modifier.fillMaxSize())
            }
        }
        is Cell.Revealed -> {
            if (cell.adjacentMines > 0) {
                val color = colors.numbers[(cell.adjacentMines - 1).coerceIn(0, colors.numbers.lastIndex)]
                CompositionLocalProvider(LocalContentColor provides color) {
                    Text(cell.adjacentMines.toString(), fontWeight = FontWeight.Bold)
                }
            }
        }
        Cell.ExplodedMine, Cell.RevealedMine -> laf.bomb(glyphSize, colors.mine)
    }
}
