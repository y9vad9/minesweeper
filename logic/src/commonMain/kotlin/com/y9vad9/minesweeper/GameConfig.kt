package com.y9vad9.minesweeper

data class GameConfig(
    val rows: Int,
    val cols: Int,
    val mines: Int,
    val seed: Seed,
) {
    init {
        require(rows >= 4) { "rows must be >= 4, was $rows" }
        require(cols >= 4) { "cols must be >= 4, was $cols" }
        val cells = rows * cols
        require(mines in 1 until (cells - 9)) {
            "mines must be in 1..${cells - 10}, was $mines"
        }
    }

    val totalCells: Int get() = rows * cols
}
