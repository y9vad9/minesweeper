package com.y9vad9.minesweeper.ui

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent

@OptIn(ExperimentalComposeUiApi::class)
object JvmCellInput : CellInputModifier {
    override fun apply(
        modifier: Modifier,
        onReveal: () -> Unit,
        onFlag: () -> Unit,
        onChord: () -> Unit,
        onHoverChange: (Boolean) -> Unit,
    ): Modifier = modifier
        .onPointerEvent(PointerEventType.Enter) { onHoverChange(true) }
        .onPointerEvent(PointerEventType.Exit) { onHoverChange(false) }
        .onPointerEvent(PointerEventType.Release) { event ->
            when (event.button) {
                PointerButton.Primary -> onReveal()
                PointerButton.Secondary -> onFlag()
                PointerButton.Tertiary -> onChord()
                else -> Unit
            }
        }
}
