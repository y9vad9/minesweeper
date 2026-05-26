package com.y9vad9.minesweeper.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.y9vad9.minesweeper.Difficulty
import com.y9vad9.minesweeper.DifficultyStats
import com.y9vad9.minesweeper.GameOutcome
import com.y9vad9.minesweeper.GameRecord
import com.y9vad9.minesweeper.computeStats
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun DifficultyDetailScreen(
    difficulty: Difficulty,
    allRecords: List<GameRecord>,
    onBack: () -> Unit,
    onReplay: (GameRecord) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val records = remember(allRecords, difficulty) {
        allRecords.filter { it.matchesDifficulty(difficulty) }
    }
    val stats = remember(records) { computeStats(records).forDifficulty(difficulty) }
    val mono = LocalLookAndFeel.current.monoFont

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "header") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppTextButton(onClick = onBack) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        BackArrow()
                        Text(strings.statsBack)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    strings.difficulty(difficulty),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        item(key = "hero") {
            HeroBest(stats = stats, mono = mono, modifier = Modifier.padding(horizontal = 12.dp))
        }

        item(key = "grid") {
            StatsGrid(stats = stats, records = records, mono = mono, modifier = Modifier.padding(horizontal = 12.dp))
        }

        if (records.isNotEmpty()) {
            item(key = "recent-header") {
                Text(
                    strings.statsRecentGames,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                )
            }
            items(records.take(50), key = { it.playedAtEpochMillis }) { record ->
                DetailHistoryRow(
                    record = record,
                    onReplay = { onReplay(record) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
        }

        item(key = "footer-spacer") { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun HeroBest(stats: DifficultyStats, mono: FontFamily, modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    AppListCard(onClick = {}, modifier = modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(vertical = 4.dp),
        ) {
            Text(
                strings.statsBestTime,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stats.bestTimeMillis?.milliseconds?.formatClock() ?: strings.statsNoData,
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = mono),
                color = MaterialTheme.colorScheme.primary,
            )
            stats.bestThreeBVPerSecond?.let { bvs ->
                Text(
                    "${strings.statsThreeBVPerSecond}: ${bvs.formatRate()}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = mono),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatsGrid(
    stats: DifficultyStats,
    records: List<GameRecord>,
    mono: FontFamily,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current

    val chronological = remember(records) { records.sortedBy { it.playedAtEpochMillis } }
    val wins = remember(chronological) { chronological.filter { it.outcome == GameOutcome.Won } }
    val losses = remember(chronological) { chronological.filter { it.outcome == GameOutcome.Lost } }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        StatRow(
            label = strings.statsAverageTime,
            value = stats.averageTimeMillis?.let { it.milliseconds.formatClock() },
            mono = mono,
            expandedContent = avgBreakdownContent(stats, chronological, mono),
        )
        StatRow(strings.statsMedianTime, stats.medianWinTimeMillis?.let { it.milliseconds.formatClock() }, mono)
        StatRow(strings.statsWinRate, stats.winRate?.let { "${(it * 100).toInt()}%" }, mono)
        StatRow(strings.statsTotalGames, stats.gamesPlayed.takeIf { it > 0 }?.toString(), mono)
        StatRow(strings.statsTotalWins, stats.wins.takeIf { stats.gamesPlayed > 0 }?.toString(), mono)
        StatRow(strings.statsTotalLosses, stats.losses.takeIf { stats.gamesPlayed > 0 }?.toString(), mono)
        StatRow(
            label = strings.statsAverageThreeBVPerSecond,
            value = stats.averageThreeBVPerSecond?.formatRate(),
            mono = mono,
            expandedContent = threeBvsChartContentFor(wins, strings.statsThreeBVPerSecond),
        )
        StatRow(strings.statsCurrentStreak, stats.currentWinStreak.toString(), mono)
        StatRow(strings.statsLongestStreak, stats.longestWinStreak.toString(), mono)
    }
}

private fun avgBreakdownContent(
    stats: DifficultyStats,
    chronological: List<GameRecord>,
    mono: FontFamily,
): (@Composable () -> Unit)? {
    if (chronological.isEmpty()) return null
    return {
        val s = LocalStrings.current
        val winsList = chronological.filter { it.outcome == GameOutcome.Won }
        val lossList = chronological.filter { it.outcome == GameOutcome.Lost }
        val winColor = MaterialTheme.colorScheme.primary
        val lossColor = MaterialTheme.colorScheme.error
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SubStatLine(
                label = s.statsAverageWinTime,
                value = stats.averageWinTimeMillis?.let { it.milliseconds.formatClock() },
                mono = mono,
            )
            SubStatLine(
                label = s.statsAverageLossTime,
                value = stats.averageLossTimeMillis?.let { it.milliseconds.formatClock() },
                mono = mono,
            )
            if (chronological.size >= 2) {
                DurationLineChart(records = chronological, title = s.statsChartAllGames, lineColor = winColor)
            }
            if (winsList.size >= 2) {
                DurationLineChart(records = winsList, title = s.statsChartWinsOnly, lineColor = winColor)
            }
            if (lossList.size >= 2) {
                DurationLineChart(records = lossList, title = s.statsChartLossesOnly, lineColor = lossColor)
            }
        }
    }
}

@Composable
private fun SubStatLine(label: String, value: String?, mono: FontFamily) {
    val strings = LocalStrings.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        Text(
            value ?: strings.statsNoData,
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = mono),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun threeBvsChartContentFor(wins: List<GameRecord>, title: String): (@Composable () -> Unit)? {
    val withRate = wins.mapNotNull { rec -> rec.threeBVPerSecond?.let { rec to it } }
    if (withRate.size < 2) return null
    return { ThreeBvsLineChart(values = withRate.map { it.second }, title = title) }
}

@Composable
private fun StatRow(
    label: String,
    value: String?,
    mono: FontFamily,
    expandedContent: (@Composable () -> Unit)? = null,
) {
    val strings = LocalStrings.current
    var expanded by remember { mutableStateOf(false) }
    val expandable = expandedContent != null

    AppListCard(
        onClick = if (expandable) ({ expanded = !expanded }) else ({}),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    value ?: strings.statsNoData,
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = mono),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (expandable) {
                    Spacer(Modifier.width(8.dp))
                    ExpandChevron(
                        expanded = expanded,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (expandedContent != null) {
                AnimatedVisibility(visible = expanded) {
                    expandedContent()
                }
            }
        }
    }
}

@Composable
private fun DetailHistoryRow(record: GameRecord, onReplay: () -> Unit, modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    val mono = LocalLookAndFeel.current.monoFont
    val outcomeColor = when (record.outcome) {
        GameOutcome.Won -> MaterialTheme.colorScheme.primary
        GameOutcome.Lost -> MaterialTheme.colorScheme.error
    }
    val outcomeText = when (record.outcome) {
        GameOutcome.Won -> strings.outcomeWon
        GameOutcome.Lost -> strings.outcomeLost
    }
    AppListCard(onClick = onReplay, modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(outcomeText, style = MaterialTheme.typography.labelMedium, color = outcomeColor)
            Spacer(Modifier.width(12.dp))
            record.threeBV?.let { bv ->
                Text(
                    "${strings.statsThreeBV} $bv",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = mono),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                record.duration.formatClock(),
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = mono),
            )
        }
    }
}

@Composable
private fun DurationLineChart(
    records: List<GameRecord>,
    title: String,
    lineColor: Color = MaterialTheme.colorScheme.primary,
) {
    val seconds = remember(records) { records.map { it.durationMillis / 1000.0 } }
    SmallLineChart(
        values = seconds,
        title = title,
        yStep = 10,
        lineColor = lineColor,
        formatYBound = { ms -> (ms.toLong() * 1000L).milliseconds.formatClock() },
    )
}

@Composable
private fun ThreeBvsLineChart(values: List<Double>, title: String) {
    SmallLineChart(
        values = values,
        title = title,
        yStep = 1,
        lineColor = MaterialTheme.colorScheme.primary,
        formatYBound = { it.formatRate() },
    )
}

@Composable
private fun SmallLineChart(
    values: List<Double>,
    title: String,
    yStep: Int,
    lineColor: Color,
    formatYBound: (Double) -> String,
) {
    val yMax = remember(values, yStep) {
        val raw = values.maxOrNull() ?: 0.0
        ((raw / yStep).toInt() + 1) * yStep
    }
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val painter = LocalLineChart.current

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))

            Text(
                "0 — ${formatYBound(yMax.toDouble())}",
                style = MaterialTheme.typography.labelSmall,
                color = onSurfaceVariant,
            )
        }
        painter.Render(
            values = values,
            yMax = yMax,
            lineColor = lineColor,
            modifier = Modifier.fillMaxWidth().height(140.dp),
        )
    }
}

private fun Double.formatRate(): String {
    val rounded = (this * 100).toInt() / 100.0
    return rounded.toString()
}

private fun GameRecord.matchesDifficulty(d: Difficulty): Boolean =
    rows == d.rows && cols == d.cols && mines == d.mines

@Composable
private fun ExpandChevron(expanded: Boolean, color: Color) {
    Canvas(modifier = Modifier.size(width = 10.dp, height = 6.dp)) {
        val path = androidx.compose.ui.graphics.Path().apply {
            if (expanded) {
                moveTo(0f, size.height)
                lineTo(size.width, size.height)
                lineTo(size.width / 2f, 0f)
            } else {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
            }
            close()
        }
        drawPath(path, color)
    }
}

@Composable
private fun BackArrow() {
    val color = LocalContentColor.current
    Canvas(modifier = Modifier.size(width = 12.dp, height = 10.dp)) {
        val cy = size.height / 2f
        val tipX = 0f
        val tailX = size.width
        val headLen = size.width * 0.45f
        val stroke = size.height * 0.18f
        drawLine(
            color = color,
            start = Offset(tipX, cy),
            end = Offset(tailX, cy),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(tipX, cy),
            end = Offset(tipX + headLen, cy - headLen),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(tipX, cy),
            end = Offset(tipX + headLen, cy + headLen),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}
