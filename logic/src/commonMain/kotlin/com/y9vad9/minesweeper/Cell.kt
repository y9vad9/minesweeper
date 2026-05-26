package com.y9vad9.minesweeper

sealed interface Cell {
    data class Hidden(val flagged: Boolean) : Cell
    data class Revealed(val adjacentMines: Int) : Cell
    data object ExplodedMine : Cell
    data object RevealedMine : Cell
}
