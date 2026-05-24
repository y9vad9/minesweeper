package com.y9vad9.minesweeper.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowWidthClass { Compact, Medium, Expanded }

fun classifyWidth(width: Dp): WindowWidthClass = when {
    width < 600.dp -> WindowWidthClass.Compact
    width < 1100.dp -> WindowWidthClass.Medium
    else -> WindowWidthClass.Expanded
}

@Composable
fun rememberWindowWidthClass(widthPx: Int): WindowWidthClass {
    val density = LocalDensity.current
    return remember(widthPx, density) {
        with(density) { classifyWidth(widthPx.toDp()) }
    }
}
