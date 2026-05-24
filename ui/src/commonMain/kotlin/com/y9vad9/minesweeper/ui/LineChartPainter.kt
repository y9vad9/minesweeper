package com.y9vad9.minesweeper.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap

fun interface LineChartPainter {
    @Composable
    fun Render(
        values: List<Double>,
        yMax: Int,
        lineColor: Color,
        modifier: Modifier,
    )
}

object FallbackLineChartPainter : LineChartPainter {
    @Composable
    override fun Render(
        values: List<Double>,
        yMax: Int,
        lineColor: Color,
        modifier: Modifier,
    ) {
        Canvas(modifier = modifier) {
            if (values.isEmpty()) return@Canvas
            val w = size.width
            val h = size.height
            val yScale = if (yMax <= 0) 1.0 else yMax.toDouble()
            if (values.size == 1) {
                val v = values.first().coerceAtMost(yScale)
                val y = (h - (v / yScale * h)).toFloat()
                drawCircle(color = lineColor, radius = 3f, center = Offset(w / 2f, y))
                return@Canvas
            }
            val xStep = w / (values.size - 1).toFloat()
            val path = Path()
            values.forEachIndexed { i, raw ->
                val v = raw.coerceAtMost(yScale)
                val x = i * xStep
                val y = (h - (v / yScale * h)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = lineColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.5f,
                    cap = StrokeCap.Round,
                ),
            )
        }
    }
}

val LocalLineChart = staticCompositionLocalOf<LineChartPainter> { FallbackLineChartPainter }
