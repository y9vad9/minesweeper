package com.y9vad9.minesweeper.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.y9vad9.minesweeper.logic.Difficulty
import com.y9vad9.minesweeper.logic.GameOutcome
import com.y9vad9.minesweeper.logic.GameRecord
import com.y9vad9.minesweeper.logic.computeStats

@Composable
fun HistoryScreen(
    records: List<GameRecord>,
    onReplay: (GameRecord) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val stats = remember(records) { computeStats(records) }

    var detailFor by remember { mutableStateOf<Difficulty?>(null) }

    detailFor?.let { d ->
        DifficultyDetailScreen(
            difficulty = d,
            allRecords = records,
            onBack = { detailFor = null },
            onReplay = onReplay,
            modifier = modifier,
        )
        return
    }

    if (records.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                strings.historyEmpty,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "header") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    strings.historyHeader,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.weight(1f))
                AppTextButton(onClick = onClear) { Text(strings.historyClear) }
            }
        }

        item(key = "stats") {
            StatsPanel(
                stats = stats,
                modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 4.dp),
                onDifficultyClick = { detailFor = it },
            )
        }

        items(records, key = { it.playedAtEpochMillis }) { record ->
            HistoryItem(
                record = record,
                onReplay = { onReplay(record) },
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }

        item(key = "footer-spacer") { Spacer(Modifier.padding(bottom = 12.dp)) }
    }
}

@Composable
private fun HistoryItem(record: GameRecord, onReplay: () -> Unit, modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    val outcomeColor = when (record.outcome) {
        GameOutcome.Won -> MaterialTheme.colorScheme.primary
        GameOutcome.Lost -> MaterialTheme.colorScheme.error
    }
    val outcomeText = when (record.outcome) {
        GameOutcome.Won -> strings.outcomeWon
        GameOutcome.Lost -> strings.outcomeLost
    }
    val mono = LocalLookAndFeel.current.monoFont

    AppListCard(onClick = onReplay, modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                outcomeText,
                style = MaterialTheme.typography.labelMedium,
                color = outcomeColor,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "${record.rows}×${record.cols} · ${record.mines}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            record.threeBV?.let { bv ->
                Spacer(Modifier.width(8.dp))
                Text(
                    "${strings.statsThreeBV} $bv",
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = mono),
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
