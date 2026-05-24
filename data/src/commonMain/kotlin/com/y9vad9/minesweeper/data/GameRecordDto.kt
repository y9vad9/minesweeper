package com.y9vad9.minesweeper.data

import com.y9vad9.minesweeper.logic.GameOutcome
import com.y9vad9.minesweeper.logic.GameRecord
import com.y9vad9.minesweeper.logic.Seed
import kotlinx.serialization.Serializable

@Serializable
internal data class GameRecordDto(
    val seed: Long,
    val rows: Int,
    val cols: Int,
    val mines: Int,
    val outcome: String,
    val durationMillis: Long,
    val playedAtEpochMillis: Long,
    val threeBV: Int? = null,
    val threeBVSolved: Int? = null,
)

internal fun GameRecord.toDto(): GameRecordDto = GameRecordDto(
    seed = seed.value,
    rows = rows,
    cols = cols,
    mines = mines,
    outcome = outcome.name,
    durationMillis = durationMillis,
    playedAtEpochMillis = playedAtEpochMillis,
    threeBV = threeBV,
    threeBVSolved = threeBVSolved,
)

internal fun GameRecordDto.toDomain(): GameRecord = GameRecord(
    seed = Seed(seed),
    rows = rows,
    cols = cols,
    mines = mines,
    outcome = runCatching { GameOutcome.valueOf(outcome) }.getOrDefault(GameOutcome.Lost),
    durationMillis = durationMillis,
    playedAtEpochMillis = playedAtEpochMillis,
    threeBV = threeBV,
    threeBVSolved = threeBVSolved,
)
