package com.y9vad9.minesweeper.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.MouseButton
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.y9vad9.minesweeper.logic.Cell
import com.y9vad9.minesweeper.logic.Position
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class BoardGridUiTest {

    @Test fun `primary click on a hidden cell invokes onReveal with that cell's Position`() = runComposeUiTest {
        // GIVEN
        var revealed: Position? = null
        setContent {
            CompositionLocalProvider(LocalCellInput provides JvmCellInput) {
                BoardGrid(
                    rows = 3,
                    cols = 3,
                    cellAt = { Cell.Hidden(flagged = false) },
                    cellSize = 32.dp,
                    onReveal = { revealed = it },
                    onFlag = {},
                    onChord = {},
                )
            }
        }

        // WHEN
        onNodeWithTag(cellTag(1, 2)).performMouseInput {
            moveTo(center)
            press(MouseButton.Primary)
            release(MouseButton.Primary)
        }
        waitForIdle()

        // THEN
        assertEquals(Position(1, 2), revealed)
    }

    @Test fun `secondary click on a hidden cell invokes onFlag with that cell's Position`() = runComposeUiTest {
        // GIVEN
        var flagged: Position? = null
        setContent {
            CompositionLocalProvider(LocalCellInput provides JvmCellInput) {
                BoardGrid(
                    rows = 2,
                    cols = 2,
                    cellAt = { Cell.Hidden(flagged = false) },
                    cellSize = 32.dp,
                    onReveal = {},
                    onFlag = { flagged = it },
                    onChord = {},
                )
            }
        }

        // WHEN
        onNodeWithTag(cellTag(0, 1)).performMouseInput {
            moveTo(center)
            press(MouseButton.Secondary)
            release(MouseButton.Secondary)
        }
        waitForIdle()

        // THEN
        assertEquals(Position(0, 1), flagged)
    }

    @Test fun `rendering a 2x2 grid emits one testTag per cell`() = runComposeUiTest {
        // GIVEN / WHEN
        setContent {
            BoardGrid(
                rows = 2,
                cols = 2,
                cellAt = { Cell.Hidden(flagged = false) },
                cellSize = 32.dp,
                onReveal = {},
                onFlag = {},
                onChord = {},
            )
        }

        // THEN
        onNodeWithTag(cellTag(0, 0)).assertExists()
        onNodeWithTag(cellTag(0, 1)).assertExists()
        onNodeWithTag(cellTag(1, 0)).assertExists()
        onNodeWithTag(cellTag(1, 1)).assertExists()
    }
}
