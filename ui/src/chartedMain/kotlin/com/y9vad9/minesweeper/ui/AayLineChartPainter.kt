package com.y9vad9.minesweeper.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.aay.compose.baseComponents.model.LegendPosition
import com.aay.compose.lineChart.LineChart
import com.aay.compose.lineChart.model.LineParameters
import com.aay.compose.lineChart.model.LineType

object AayLineChartPainter : LineChartPainter {
    @Composable
    override fun Render(
        values: List<Double>,
        yMax: Int,
        lineColor: Color,
        modifier: Modifier,
    ) {
        val xLabels = remember(values.size) { List(values.size) { (it + 1).toString() } }
        val gridColor = MaterialTheme.colorScheme.outlineVariant
        val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
        LineChart(
            modifier = modifier,
            linesParameters = listOf(
                LineParameters(
                    label = "",
                    data = values,
                    lineColor = lineColor,
                    lineType = LineType.CURVED_LINE,
                    lineShadow = true,
                ),
            ),
            xAxisData = xLabels,
            yAxisRange = yMax,
            gridColor = gridColor,
            xAxisStyle = MaterialTheme.typography.labelSmall.copy(color = onSurfaceVariant),
            yAxisStyle = MaterialTheme.typography.labelSmall.copy(color = onSurfaceVariant),
            legendPosition = LegendPosition.DISAPPEAR,
            showXAxis = false,
            showYAxis = false,
            isGrid = false,
        )
    }
}
