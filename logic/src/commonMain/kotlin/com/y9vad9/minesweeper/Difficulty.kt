package com.y9vad9.minesweeper

enum class Difficulty(val label: String, val rows: Int, val cols: Int, val mines: Int) {
    Easy("Easy", 9, 9, 10),
    Medium("Medium", 16, 16, 40),
    Hard("Hard", 16, 30, 99);

    val code: Char get() = label[0]

    fun toConfig(seed: Seed): GameConfig = GameConfig(rows, cols, mines, seed)

    companion object {
        fun matchOrClosest(rows: Int, cols: Int, mines: Int): Difficulty =
            entries.firstOrNull { it.rows == rows && it.cols == cols && it.mines == mines } ?: Easy

        fun fromCode(c: Char): Difficulty? = entries.firstOrNull { it.code.equals(c, ignoreCase = true) }
    }
}
