package com.y9vad9.minesweeper.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

fun interface CellInputModifier {
    fun apply(
        modifier: Modifier,
        onReveal: () -> Unit,
        onFlag: () -> Unit,
        onChord: () -> Unit,
        onHoverChange: (Boolean) -> Unit,
    ): Modifier
}

private object NoopCellInput : CellInputModifier {
    override fun apply(
        modifier: Modifier,
        onReveal: () -> Unit,
        onFlag: () -> Unit,
        onChord: () -> Unit,
        onHoverChange: (Boolean) -> Unit,
    ): Modifier = modifier
}

val LocalCellInput = staticCompositionLocalOf<CellInputModifier> { NoopCellInput }
