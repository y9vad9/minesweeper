package com.y9vad9.minesweeper.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

object IosCellInput : CellInputModifier {
    override fun apply(
        modifier: Modifier,
        onReveal: () -> Unit,
        onFlag: () -> Unit,
        onChord: () -> Unit,
        onHoverChange: (Boolean) -> Unit,
    ): Modifier = modifier.pointerInput(onReveal, onFlag) {
        detectTapGestures(
            onTap = { onReveal() },
            onLongPress = { onFlag() },
        )
    }
}
