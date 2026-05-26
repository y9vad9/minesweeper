package com.y9vad9.minesweeper.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.y9vad9.minesweeper.Difficulty
import com.y9vad9.minesweeper.DifficultyStats
import com.y9vad9.minesweeper.Stats
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun StatsPanel(
    stats: Stats,
    modifier: Modifier = Modifier,
    onDifficultyClick: (Difficulty) -> Unit = {},
) {
    val strings = LocalStrings.current
    val mono = LocalLookAndFeel.current.monoFont

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                strings.statsHeader,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Spacer(Modifier.weight(1f))
            StatChip(
                label = strings.statsCurrentStreak,
                value = stats.currentWinStreak.toString(),
                mono = mono,
            )
            Spacer(Modifier.width(12.dp))
            StatChip(
                label = strings.statsLongestStreak,
                value = stats.longestWinStreak.toString(),
                mono = mono,
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
        ) {
            items(Difficulty.entries.toList(), key = { it.name }) { d ->
                DifficultyCard(strings, stats.forDifficulty(d), mono, onClick = { onDifficultyClick(d) })
            }
        }
    }
}

@Composable
private fun DifficultyCard(
    strings: Strings,
    s: DifficultyStats,
    mono: FontFamily,
    onClick: () -> Unit,
) {
    AppListCard(onClick = onClick, modifier = Modifier.width(220.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                strings.difficulty(s.difficulty),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            StatLine(strings.statsBestTime, s.bestTimeMillis?.let { it.milliseconds.formatClock() }, mono)
            StatLine(strings.statsAverageTime, s.averageTimeMillis?.let { it.milliseconds.formatClock() }, mono)
            StatLine(
                strings.statsWinRate,
                s.winRate?.let { "${(it * 100).toInt()}%" },
                mono,
            )
            StatLine(strings.statsTotalGames, s.gamesPlayed.takeIf { it > 0 }?.toString(), mono)
            StatLine(
                strings.statsThreeBVPerSecond,
                s.bestThreeBVPerSecond?.let { ((it * 100).toInt() / 100.0).toString() },
                mono,
            )
        }
    }
}

@Composable
private fun StatLine(label: String, value: String?, mono: FontFamily) {
    val strings = LocalStrings.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
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

@Composable
private fun StatChip(label: String, value: String, mono: FontFamily) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 4.dp),
        )
        Text(
            value,
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = mono),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
