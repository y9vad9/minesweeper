package com.y9vad9.minesweeper.logic

import com.y9vad9.minesweeper.logic.Difficulty

data class DifficultyStats(
    val difficulty: Difficulty,
    val gamesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val bestTimeMillis: Long? = null,
    val bestThreeBVPerSecond: Double? = null,
    val averageThreeBVPerSecond: Double? = null,
    val averageTimeMillis: Long? = null,
    val averageWinTimeMillis: Long? = null,
    val averageLossTimeMillis: Long? = null,
    val medianWinTimeMillis: Long? = null,
    val currentWinStreak: Int = 0,
    val longestWinStreak: Int = 0,
) {
    val winRate: Double?
        get() = if (gamesPlayed == 0) null else wins.toDouble() / gamesPlayed
}

data class Stats(
    val byDifficulty: Map<Difficulty, DifficultyStats>,
    val totalGames: Int,
    val totalWins: Int,
    val currentWinStreak: Int,
    val longestWinStreak: Int,
) {
    fun forDifficulty(d: Difficulty): DifficultyStats =
        byDifficulty[d] ?: DifficultyStats(difficulty = d)

    val totalLosses: Int get() = totalGames - totalWins
    val winRate: Double? get() = if (totalGames == 0) null else totalWins.toDouble() / totalGames
}

private fun GameRecord.difficultyOrNull(): Difficulty? = when {
    rows == Difficulty.Easy.rows && cols == Difficulty.Easy.cols && mines == Difficulty.Easy.mines -> Difficulty.Easy
    rows == Difficulty.Medium.rows && cols == Difficulty.Medium.cols && mines == Difficulty.Medium.mines -> Difficulty.Medium
    rows == Difficulty.Hard.rows && cols == Difficulty.Hard.cols && mines == Difficulty.Hard.mines -> Difficulty.Hard
    else -> null
}

fun computeStats(records: List<GameRecord>): Stats {
    val byDifficulty = mutableMapOf<Difficulty, MutableList<GameRecord>>()
    for (r in records) {
        val d = r.difficultyOrNull() ?: continue
        byDifficulty.getOrPut(d) { mutableListOf() } += r
    }

    val cards = byDifficulty.mapValues { (d, list) ->
        val wins = list.filter { it.outcome == GameOutcome.Won }
        val lossList = list.filter { it.outcome == GameOutcome.Lost }
        val losses = lossList.size
        val bestTime = wins.minOfOrNull { it.durationMillis }
        val bvsValues = wins.mapNotNull { it.threeBVPerSecond }
        val bestBVS = bvsValues.maxOrNull()
        val avgBVS = if (bvsValues.isEmpty()) null else bvsValues.sum() / bvsValues.size
        val avgWin = if (wins.isEmpty()) null else wins.sumOf { it.durationMillis } / wins.size
        val avgLoss = if (lossList.isEmpty()) null else lossList.sumOf { it.durationMillis } / lossList.size
        val avgAll = if (list.isEmpty()) null else list.sumOf { it.durationMillis } / list.size
        val medianTime = wins.takeIf { it.isNotEmpty() }?.let { ws ->
            val sorted = ws.map { it.durationMillis }.sorted()
            sorted[sorted.size / 2]
        }
        val chrono = list.sortedBy { it.playedAtEpochMillis }
        var longestForD = 0
        var runForD = 0
        for (r in chrono) {
            if (r.outcome == GameOutcome.Won) {
                runForD++
                if (runForD > longestForD) longestForD = runForD
            } else runForD = 0
        }
        DifficultyStats(
            difficulty = d,
            gamesPlayed = list.size,
            wins = wins.size,
            losses = losses,
            bestTimeMillis = bestTime,
            bestThreeBVPerSecond = bestBVS,
            averageThreeBVPerSecond = avgBVS,
            averageTimeMillis = avgAll,
            averageWinTimeMillis = avgWin,
            averageLossTimeMillis = avgLoss,
            medianWinTimeMillis = medianTime,
            currentWinStreak = runForD,
            longestWinStreak = longestForD,
        )
    }

    val totalGames = records.count { it.difficultyOrNull() != null }
    val totalWins = records.count { it.difficultyOrNull() != null && it.outcome == GameOutcome.Won }

    val chronological = records
        .filter { it.difficultyOrNull() != null }
        .sortedBy { it.playedAtEpochMillis }

    var longest = 0
    var run = 0
    for (r in chronological) {
        if (r.outcome == GameOutcome.Won) {
            run++
            if (run > longest) longest = run
        } else {
            run = 0
        }
    }
    val current = run

    return Stats(
        byDifficulty = cards,
        totalGames = totalGames,
        totalWins = totalWins,
        currentWinStreak = current,
        longestWinStreak = longest,
    )
}
