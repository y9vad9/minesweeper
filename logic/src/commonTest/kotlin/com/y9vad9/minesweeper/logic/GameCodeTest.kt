package com.y9vad9.minesweeper.logic

import com.y9vad9.minesweeper.Difficulty
import com.y9vad9.minesweeper.GameCodeParseResult
import com.y9vad9.minesweeper.GameConfig
import com.y9vad9.minesweeper.Seed
import com.y9vad9.minesweeper.parseGameCode
import com.y9vad9.minesweeper.toCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GameCodeTest {

    @Test fun `parseGameCode of empty string returns Blank`() {
        assertEquals(GameCodeParseResult.Blank, parseGameCode(""))
    }

    @Test fun `parseGameCode of whitespace-only string returns Blank`() {
        assertEquals(GameCodeParseResult.Blank, parseGameCode("   "))
    }

    @Test fun `parseGameCode of E-prefixed code returns Easy preset config`() {
        val parsed = parseGameCode("E-2A") as GameCodeParseResult.Success
        assertEquals(Difficulty.Easy.rows, parsed.config.rows)
        assertEquals(Difficulty.Easy.cols, parsed.config.cols)
        assertEquals(Difficulty.Easy.mines, parsed.config.mines)
        assertEquals(0x2AL, parsed.config.seed.value)
    }

    @Test fun `parseGameCode of M-prefixed code returns Medium preset config`() {
        val parsed = parseGameCode("M-DEADBEEF") as GameCodeParseResult.Success
        assertEquals(Difficulty.Medium.rows, parsed.config.rows)
        assertEquals(Difficulty.Medium.cols, parsed.config.cols)
    }

    @Test fun `parseGameCode is case-insensitive for prefix and hex`() {
        val upper = parseGameCode("H-ABC123") as GameCodeParseResult.Success
        val lower = parseGameCode("h-abc123") as GameCodeParseResult.Success
        assertEquals(upper.config, lower.config)
        assertEquals(Difficulty.Hard.rows, upper.config.rows)
    }

    @Test fun `parseGameCode of explicit dimensions returns a custom config`() {
        val parsed = parseGameCode("20x30x120-7A3F") as GameCodeParseResult.Success
        assertEquals(20, parsed.config.rows)
        assertEquals(30, parsed.config.cols)
        assertEquals(120, parsed.config.mines)
        assertEquals(0x7A3FL, parsed.config.seed.value)
    }

    @Test fun `toCode of a preset config uses its letter alias`() {
        val easy = Difficulty.Easy.toConfig(Seed(0x2A))
        assertEquals("E-2A", easy.toCode())
    }

    @Test fun `toCode of a custom config uses explicit dimensions`() {
        val custom = GameConfig(rows = 20, cols = 30, mines = 120, seed = Seed(0x7A3F))
        assertEquals("20x30x120-7A3F", custom.toCode())
    }

    @Test fun `toCode then parseGameCode roundtrips a custom config`() {
        val original = GameConfig(rows = 12, cols = 18, mines = 50, seed = Seed(0x123456789ABCDEFL))
        val parsed = parseGameCode(original.toCode()) as GameCodeParseResult.Success
        assertEquals(original, parsed.config)
    }

    @Test fun `parseGameCode of unknown prefix returns InvalidFormat`() {
        assertIs<GameCodeParseResult.InvalidFormat>(parseGameCode("X-DEAD"))
    }

    @Test fun `parseGameCode of bare decimal returns InvalidFormat`() {
        assertIs<GameCodeParseResult.InvalidFormat>(parseGameCode("42"))
    }

    @Test fun `parseGameCode of out-of-range custom dimensions returns InvalidFormat`() {
        assertIs<GameCodeParseResult.InvalidFormat>(parseGameCode("3x3x1-AA"))
    }

    @Test fun `parseGameCode of too many mines returns InvalidFormat`() {
        assertIs<GameCodeParseResult.InvalidFormat>(parseGameCode("9x9x80-AA"))
    }
}
