package com.y9vad9.minesweeper.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun BombGlyph(
    size: Dp,
    bodyColor: Color,
    highlightColor: Color = Color.White.copy(alpha = 0.85f),
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(size)) {
        val s = min(this.size.width, this.size.height)
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val bodyRadius = s * 0.32f
        val spikeReach = s * 0.46f
        val spikeStroke = s * 0.11f

        val angles = listOf(0.0, PI / 4, PI / 2, 3 * PI / 4)
        for (angle in angles) {
            val dx = (cos(angle) * spikeReach).toFloat()
            val dy = (sin(angle) * spikeReach).toFloat()
            drawLine(
                color = bodyColor,
                start = Offset(center.x - dx, center.y - dy),
                end = Offset(center.x + dx, center.y + dy),
                strokeWidth = spikeStroke,
                cap = StrokeCap.Square,
            )
        }

        drawCircle(bodyColor, radius = bodyRadius, center = center)

        val hlSide = bodyRadius * 0.42f
        drawRect(
            highlightColor,
            topLeft = Offset(center.x - bodyRadius * 0.55f, center.y - bodyRadius * 0.55f),
            size = Size(hlSide, hlSide),
        )
    }
}

val LocalEmojiGlyphs = staticCompositionLocalOf { true }

@Composable
fun EmojiBombGlyph(size: Dp, modifier: Modifier = Modifier) {
    if (!LocalEmojiGlyphs.current) {
        BombGlyph(size = size, bodyColor = LocalContentColor.current, modifier = modifier)
        return
    }
    val fontSize: TextUnit = (size.value * 0.85f).sp
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        CompositionLocalProvider(LocalContentColor provides Color.Unspecified) {
            Text(
                "💣",
                style = TextStyle(fontFamily = FontFamily.Default, fontSize = fontSize),
            )
        }
    }
}

@Composable
fun EmojiFlagGlyph(size: Dp, modifier: Modifier = Modifier) {
    if (!LocalEmojiGlyphs.current) {
        FlagGlyph(
            size = size,
            flagColor = MaterialTheme.colorScheme.error,
            poleColor = LocalContentColor.current,
            modifier = modifier,
        )
        return
    }
    val fontSize: TextUnit = (size.value * 0.85f).sp
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        CompositionLocalProvider(LocalContentColor provides Color.Unspecified) {
            Text(
                "🚩",
                style = TextStyle(fontFamily = FontFamily.Default, fontSize = fontSize),
            )
        }
    }
}

@Composable
fun FlagGlyph(
    size: Dp,
    flagColor: Color,
    poleColor: Color = flagColor,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(size)) {
        val s = min(this.size.width, this.size.height)
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val poleStroke = s * 0.10f

        val poleTop = Offset(cx - s * 0.06f, cy - s * 0.36f)
        val poleBottom = Offset(cx - s * 0.06f, cy + s * 0.30f)
        drawLine(poleColor, poleTop, poleBottom, strokeWidth = poleStroke, cap = StrokeCap.Round)

        val baseW = s * 0.46f
        val baseH = s * 0.10f
        drawRect(
            poleColor,
            topLeft = Offset(cx - baseW / 2f, poleBottom.y),
            size = Size(baseW, baseH),
        )
        drawRect(
            poleColor,
            topLeft = Offset(cx - baseW * 0.7f / 2f, poleBottom.y + baseH),
            size = Size(baseW * 0.7f, baseH * 0.6f),
        )

        val pennant = Path().apply {
            moveTo(poleTop.x, poleTop.y)
            lineTo(poleTop.x + s * 0.42f, poleTop.y + s * 0.16f)
            lineTo(poleTop.x, poleTop.y + s * 0.32f)
            close()
        }
        drawPath(pennant, color = flagColor)
    }
}
