package com.y9vad9.minesweeper.logic

import com.y9vad9.minesweeper.logic.Difficulty
import com.y9vad9.minesweeper.logic.Seed
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StatsTest {

    private fun record(
        difficulty: Difficulty,
        outcome: GameOutcome,
        durationMillis: Long,
        playedAt: Long,
        threeBV: Int? = null,
        threeBVSolved: Int? = null,
    ) = GameRecord(
        seed = Seed(playedAt),
        rows = difficulty.rows,
        cols = difficulty.cols,
        mines = difficulty.mines,
        outcome = outcome,
        durationMillis = durationMillis,
        playedAtEpochMillis = playedAt,
        threeBV = threeBV,
        threeBVSolved = threeBVSolved,
    )

    @Test fun `empty history yields zero counters and no per-difficulty data`() {
        val s = computeStats(emptyList())
        assertEquals(0, s.totalGames)
        assertEquals(0, s.totalWins)
        assertEquals(0, s.currentWinStreak)
        assertEquals(0, s.longestWinStreak)
        assertNull(s.forDifficulty(Difficulty.Easy).bestTimeMillis)
        assertNull(s.forDifficulty(Difficulty.Easy).winRate)
    }

    @Test fun `best time is the lowest winning duration for that difficulty`() {
        val records = listOf(
            record(Difficulty.Easy, GameOutcome.Won, durationMillis = 12_000, playedAt = 1),
            record(Difficulty.Easy, GameOutcome.Won, durationMillis = 9_000, playedAt = 2),
            record(Difficulty.Easy, GameOutcome.Lost, durationMillis = 3_000, playedAt = 3),
        )
        val easy = computeStats(records).forDifficulty(Difficulty.Easy)
        assertEquals(9_000L, easy.bestTimeMillis)
        assertEquals(2, easy.wins)
        assertEquals(1, easy.losses)
        assertEquals(3, easy.gamesPlayed)
    }

    @Test fun `current and longest streaks are computed from chronological order`() {
        val records = listOf(
            record(Difficulty.Easy, GameOutcome.Won, 10_000, playedAt = 5),
            record(Difficulty.Easy, GameOutcome.Won, 10_000, playedAt = 4),
            record(Difficulty.Easy, GameOutcome.Won, 10_000, playedAt = 3),
            record(Difficulty.Easy, GameOutcome.Lost, 10_000, playedAt = 2),
            record(Difficulty.Easy, GameOutcome.Won, 10_000, playedAt = 1),
        )
        val s = computeStats(records)
        assertEquals(3, s.currentWinStreak)
        assertEquals(3, s.longestWinStreak)
    }

    @Test fun `best 3BVs is the maximum across wins with known threeBV`() {
        val records = listOf(
            record(Difficulty.Medium, GameOutcome.Won, durationMillis = 5_000, playedAt = 1, threeBV = 10, threeBVSolved = 10),
            record(Difficulty.Medium, GameOutcome.Won, durationMillis = 5_000, playedAt = 2, threeBV = 20, threeBVSolved = 20),
            record(Difficulty.Medium, GameOutcome.Won, durationMillis = 1_000, playedAt = 3),
        )
        val medium = computeStats(records).forDifficulty(Difficulty.Medium)
        assertEquals(4.0, medium.bestThreeBVPerSecond)
    }

    @Test fun `average time spans wins and losses, per-outcome averages are split`() {
        val records = listOf(
            record(Difficulty.Easy, GameOutcome.Won, durationMillis = 10_000, playedAt = 1),
            record(Difficulty.Easy, GameOutcome.Won, durationMillis = 20_000, playedAt = 2),
            record(Difficulty.Easy, GameOutcome.Lost, durationMillis = 4_000, playedAt = 3),
            record(Difficulty.Easy, GameOutcome.Lost, durationMillis = 8_000, playedAt = 4),
        )
        val easy = computeStats(records).forDifficulty(Difficulty.Easy)
        assertEquals(10_500L, easy.averageTimeMillis)
        assertEquals(15_000L, easy.averageWinTimeMillis)
        assertEquals(6_000L, easy.averageLossTimeMillis)
    }

    @Test fun `per-outcome averages are null when that outcome never happened`() {
        val winsOnly = computeStats(
            listOf(record(Difficulty.Easy, GameOutcome.Won, 10_000, playedAt = 1))
        ).forDifficulty(Difficulty.Easy)
        assertNull(winsOnly.averageLossTimeMillis)

        val lossesOnly = computeStats(
            listOf(record(Difficulty.Easy, GameOutcome.Lost, 5_000, playedAt = 1))
        ).forDifficulty(Difficulty.Easy)
        assertNull(lossesOnly.averageWinTimeMillis)
        assertEquals(5_000L, lossesOnly.averageTimeMillis)
    }

    @Test fun `non-canonical board dimensions are excluded from rollups`() {
        val custom = GameRecord(
            seed = Seed(1),
            rows = 7, cols = 7, mines = 5,
            outcome = GameOutcome.Won,
            durationMillis = 1_000,
            playedAtEpochMillis = 1,
        )
        val s = computeStats(listOf(custom))
        assertEquals(0, s.totalGames)
        assertEquals(0, s.totalWins)
    }
}
