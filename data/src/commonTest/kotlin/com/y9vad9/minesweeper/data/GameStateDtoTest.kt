package com.y9vad9.minesweeper.data

import com.y9vad9.minesweeper.GameConfig
import com.y9vad9.minesweeper.GameEngine
import com.y9vad9.minesweeper.GameState
import com.y9vad9.minesweeper.PersistedGame
import com.y9vad9.minesweeper.Position
import com.y9vad9.minesweeper.Seed
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

class GameStateDtoTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val fixedClock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(42_000)
    }
    private val engine = GameEngine(fixedClock)
    private val config = GameConfig(rows = 9, cols = 9, mines = 10, seed = Seed(1234))

    private fun roundTrip(game: PersistedGame): PersistedGame? {
        val encoded = json.encodeToString(PersistedGameDto.serializer(), game.toDto())
        return json.decodeFromString(PersistedGameDto.serializer(), encoded).toDomain()
    }

    @Test
    fun idle_game_survives_round_trip() {
        val original = PersistedGame(engine.newGame(config), flagMode = true)

        val restored = assertNotNull(roundTrip(original))

        assertIs<GameState.Idle>(restored.game)
        assertEquals(config, restored.game.config)
        assertEquals(true, restored.flagMode)
    }

    @Test
    fun playing_board_layout_survives_round_trip() {
        val playing = engine.reveal(engine.newGame(config), Position(4, 4))
        assertIs<GameState.Playing>(playing)
        val original = PersistedGame(playing, flagMode = false)

        val restored = roundTrip(original)

        val restoredGame = restored?.game
        assertIs<GameState.Playing>(restoredGame)
        assertEquals(playing.board.snapshot(), restoredGame.board.snapshot())
        assertEquals(playing.startedAt, restoredGame.startedAt)
        assertEquals(config, restoredGame.config)
    }

    @Test
    fun lost_game_preserves_trigger_and_elapsed() {
        var state: GameState = engine.reveal(engine.newGame(config), Position(4, 4))
        val playing = assertIs<GameState.Playing>(state)
        val mine = (0 until config.rows)
            .flatMap { r -> (0 until config.cols).map { c -> Position(r, c) } }
            .first { playing.board.isMine(it) }
        state = engine.reveal(playing, mine)
        val lost = assertIs<GameState.Lost>(state)

        val restored = roundTrip(PersistedGame(lost, flagMode = false))

        val restoredGame = assertIs<GameState.Lost>(restored?.game)
        assertEquals(lost.triggeredAt, restoredGame.triggeredAt)
        assertEquals(lost.elapsed, restoredGame.elapsed)
        assertEquals(lost.board.snapshot(), restoredGame.board.snapshot())
    }

    @Test
    fun malformed_blob_decodes_to_null() {
        val broken = PersistedGameDto(
            status = "Playing",
            config = GameConfigDto(9, 9, 10, 1234),
            board = null,
            startedAtEpochMillis = null,
        )
        assertNull(broken.toDomain())

        assertNull(broken.copy(status = "Bogus").toDomain())
    }

    @Test
    fun unknown_json_keys_are_tolerated() {
        val encoded = json.encodeToString(
            PersistedGameDto.serializer(),
            PersistedGame(engine.newGame(config), flagMode = false).toDto(),
        )
        val withExtra = encoded.dropLast(1) + ""","futureField":123}"""
        val restored = json.decodeFromString(PersistedGameDto.serializer(), withExtra).toDomain()
        assertTrue(restored?.game is GameState.Idle)
    }
}
