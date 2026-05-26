package com.y9vad9.minesweeper

private val PresetCodeRegex = Regex("^([EeMmHh])-([0-9A-Fa-f]{1,16})$")
private val CustomCodeRegex = Regex("^(\\d{1,3})x(\\d{1,3})x(\\d{1,5})-([0-9A-Fa-f]{1,16})$")

fun GameConfig.toCode(): String {
    val hex = seed.value.toULong().toString(16).uppercase()
    val preset = Difficulty.entries.firstOrNull {
        it.rows == rows && it.cols == cols && it.mines == mines
    }
    return if (preset != null) "${preset.code}-$hex" else "${rows}x${cols}x${mines}-$hex"
}

sealed interface GameCodeParseResult {
    data class Success(val config: GameConfig) : GameCodeParseResult
    data object Blank : GameCodeParseResult
    data object InvalidFormat : GameCodeParseResult
}

fun parseGameCode(raw: String): GameCodeParseResult {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return GameCodeParseResult.Blank

    PresetCodeRegex.matchEntire(trimmed)?.let { match ->
        val difficulty = Difficulty.fromCode(match.groupValues[1][0])
            ?: return GameCodeParseResult.InvalidFormat
        val seed = match.groupValues[2].toULongOrNull(16)?.toLong()
            ?: return GameCodeParseResult.InvalidFormat
        return GameCodeParseResult.Success(difficulty.toConfig(Seed(seed)))
    }

    CustomCodeRegex.matchEntire(trimmed)?.let { match ->
        val rows = match.groupValues[1].toIntOrNull() ?: return GameCodeParseResult.InvalidFormat
        val cols = match.groupValues[2].toIntOrNull() ?: return GameCodeParseResult.InvalidFormat
        val mines = match.groupValues[3].toIntOrNull() ?: return GameCodeParseResult.InvalidFormat
        val seed = match.groupValues[4].toULongOrNull(16)?.toLong()
            ?: return GameCodeParseResult.InvalidFormat
        val config = runCatching { GameConfig(rows, cols, mines, Seed(seed)) }.getOrNull()
            ?: return GameCodeParseResult.InvalidFormat
        return GameCodeParseResult.Success(config)
    }

    return GameCodeParseResult.InvalidFormat
}
