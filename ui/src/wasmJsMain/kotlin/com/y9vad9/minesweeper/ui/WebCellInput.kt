package com.y9vad9.minesweeper.ui

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(ExperimentalComposeUiApi::class)
object WebCellInput : CellInputModifier {
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
            if (event.changes.firstOrNull()?.type == PointerType.Touch) return@onPointerEvent
            when (event.button) {
                PointerButton.Primary -> onReveal()
                PointerButton.Secondary -> onFlag()
                PointerButton.Tertiary -> onChord()
                else -> Unit
            }
        }
        .pointerInput(onReveal, onFlag) {
            val longPressTimeout = viewConfiguration.longPressTimeoutMillis
            awaitEachGesture {
                val down = awaitFirstDown()
                if (down.type != PointerType.Touch) return@awaitEachGesture
                val up = withTimeoutOrNull(longPressTimeout) { waitForUpOrCancellation() }
                if (up == null) {
                    onFlag()
                } else {
                    onReveal()
                }
            }
        }
}
