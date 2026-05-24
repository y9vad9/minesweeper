package com.y9vad9.minesweeper.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

fun Modifier.bevel(
    raised: Boolean,
    light: Color,
    dark: Color,
    thickness: Dp,
): Modifier = drawBehind {
    val t = thickness.toPx()
    val w = size.width
    val h = size.height
    val topLeft = if (raised) light else dark
    val bottomRight = if (raised) dark else light
    drawRect(topLeft, topLeft = Offset.Zero, size = Size(w, t))
    drawRect(topLeft, topLeft = Offset.Zero, size = Size(t, h))
    drawRect(bottomRight, topLeft = Offset(0f, h - t), size = Size(w, t))
    drawRect(bottomRight, topLeft = Offset(w - t, 0f), size = Size(t, h))
}

fun Modifier.gridDivider(color: Color, thickness: Dp): Modifier = drawBehind {
    val t = thickness.toPx()
    val w = size.width
    val h = size.height
    drawRect(color, topLeft = Offset.Zero, size = Size(w, t))
    drawRect(color, topLeft = Offset.Zero, size = Size(t, h))
}
