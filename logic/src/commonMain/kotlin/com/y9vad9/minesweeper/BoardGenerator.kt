package com.y9vad9.minesweeper

import kotlin.random.Random

internal object BoardGenerator {

    fun generate(config: GameConfig, safePos: Position): Board {
        val random = Random(config.seed.value)
        val safeZone = buildSafeZone(config, safePos)
        val candidates = allPositions(config) - safeZone
        val mines = candidates.shuffled(random).take(config.mines).toSet()
        return Board(
            rows = config.rows,
            cols = config.cols,
            mines = mines,
            revealed = emptySet(),
            flagged = emptySet(),
            explodedAt = null,
        )
    }

    private fun buildSafeZone(config: GameConfig, center: Position): Set<Position> {
        val zone = mutableSetOf<Position>()
        for (dr in -1..1) for (dc in -1..1) {
            val r = center.row + dr
            val c = center.col + dc
            if (r in 0 until config.rows && c in 0 until config.cols) {
                zone += Position(r, c)
            }
        }
        return zone
    }

    private fun allPositions(config: GameConfig): List<Position> =
        buildList(config.totalCells) {
            for (r in 0 until config.rows) for (c in 0 until config.cols) {
                add(Position(r, c))
            }
        }
}
