package com.y9vad9.minesweeper.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.y9vad9.minesweeper.logic.Difficulty
import com.y9vad9.minesweeper.logic.GameCodeParseResult
import com.y9vad9.minesweeper.logic.parseGameCode
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameTopBar(
    clock: String,
    bestTimeMillis: Long?,
    minesRemaining: Int,
    flagMode: Boolean,
    currentCode: String,
    currentDifficulty: Difficulty,
    seedInput: String,
    gameInProgress: Boolean,
    onSeedInputChange: (String) -> Unit,
    onDifficultyPicked: (Difficulty) -> Unit,
    onCustomGameRequested: (rows: Int, cols: Int, mines: Int, seedInput: String) -> Unit,
    onFlagModeToggle: () -> Unit,
    onFlagDragStart: (windowPosition: Offset) -> Unit,
    onFlagDrag: (windowPosition: Offset) -> Unit,
    onFlagDragEnd: () -> Unit,
    onFlagDragCancel: () -> Unit,
    onNewGame: () -> Unit,
    showNewGameButton: Boolean = true,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FlagChip(
                count = minesRemaining,
                active = flagMode,
                onTap = onFlagModeToggle,
                onDragStart = onFlagDragStart,
                onDrag = onFlagDrag,
                onDragEnd = onFlagDragEnd,
                onDragCancel = onFlagDragCancel,
            )
            Text(
                clock,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = LocalLookAndFeel.current.monoFont,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
            bestTimeMillis?.let { ms ->
                BestTimeChip(durationMillis = ms)
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SeedDisplay(currentCode)
            if (showNewGameButton) {
                NewGameMenu(
                    currentDifficulty = currentDifficulty,
                    seedInput = seedInput,
                    gameInProgress = gameInProgress,
                    onSeedInputChange = onSeedInputChange,
                    onDifficultyPicked = onDifficultyPicked,
                    onCustomGameRequested = onCustomGameRequested,
                    onConfirm = onNewGame,
                )
            }
        }
    }
}

@Composable
private fun BestTimeChip(durationMillis: Long) {
    val strings = LocalStrings.current
    val mono = LocalLookAndFeel.current.monoFont
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            strings.statsBestTime,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            durationMillis.milliseconds.formatClock(),
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = mono),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SeedDisplay(code: String) {
    val strings = LocalStrings.current
    val text = code
    val clipboard = LocalClipboardManager.current

    var justCopied by remember { mutableStateOf(false) }
    LaunchedEffect(justCopied) {
        if (justCopied) {
            delay(2_000)
            justCopied = false
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 10.dp, top = 2.dp, bottom = 2.dp, end = 4.dp)
            .widthIn(max = 280.dp),
    ) {
        Text(
            strings.seedLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = LocalLookAndFeel.current.monoFont,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        AppTextButton(
            onClick = {
                clipboard.setText(AnnotatedString(text))
                justCopied = true
            },
        ) {
            Text(
                if (justCopied) strings.seedCopiedToast else strings.copySeedButton,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun NewGameMenu(
    currentDifficulty: Difficulty,
    seedInput: String,
    gameInProgress: Boolean,
    onSeedInputChange: (String) -> Unit,
    onDifficultyPicked: (Difficulty) -> Unit,
    onCustomGameRequested: (rows: Int, cols: Int, mines: Int, seedInput: String) -> Unit,
    onConfirm: () -> Unit,
) {
    val strings = LocalStrings.current
    var open by remember { mutableStateOf(false) }

    Box {
        AppButton(onClick = { open = true }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(strings.newGameButton)
                DropdownChevron()
            }
        }
        AppDropdownMenu(
            expanded = open,
            onDismissRequest = { open = false },
        ) {
            NewGameForm(
                currentDifficulty = currentDifficulty,
                seedInput = seedInput,
                gameInProgress = gameInProgress,
                onSeedInputChange = onSeedInputChange,
                onDifficultyPicked = onDifficultyPicked,
                onCustomGameRequested = onCustomGameRequested,
                onConfirm = onConfirm,
                onClose = { open = false },
                modifier = Modifier
                    .widthIn(min = 280.dp, max = 360.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    }
}

@Composable
fun BoxScope.NewGameCompactAffordance(
    currentDifficulty: Difficulty,
    seedInput: String,
    gameInProgress: Boolean,
    onSeedInputChange: (String) -> Unit,
    onDifficultyPicked: (Difficulty) -> Unit,
    onCustomGameRequested: (rows: Int, cols: Int, mines: Int, seedInput: String) -> Unit,
    onConfirm: () -> Unit,
) {
    val strings = LocalStrings.current
    var sheetOpen by rememberSaveable { mutableStateOf(false) }

    AppCompactNewGame(
        label = strings.newGameButton,
        isOpen = sheetOpen,
        onOpen = { sheetOpen = true },
        onDismiss = { sheetOpen = false },
        form = {
            NewGameForm(
                currentDifficulty = currentDifficulty,
                seedInput = seedInput,
                gameInProgress = gameInProgress,
                onSeedInputChange = onSeedInputChange,
                onDifficultyPicked = onDifficultyPicked,
                onCustomGameRequested = onCustomGameRequested,
                onConfirm = onConfirm,
                onClose = { sheetOpen = false },
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NewGameForm(
    currentDifficulty: Difficulty,
    seedInput: String,
    gameInProgress: Boolean,
    onSeedInputChange: (String) -> Unit,
    onDifficultyPicked: (Difficulty) -> Unit,
    onCustomGameRequested: (rows: Int, cols: Int, mines: Int, seedInput: String) -> Unit,
    onConfirm: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    var pending by remember(currentDifficulty) { mutableStateOf(currentDifficulty) }
    var customMode by remember(currentDifficulty) { mutableStateOf(false) }
    var rowsInput by remember { mutableStateOf("16") }
    var colsInput by remember { mutableStateOf("16") }
    var minesInput by remember { mutableStateOf("40") }
    var customSeedInput by remember { mutableStateOf("") }

    val rowsN = rowsInput.toIntOrNull()
    val colsN = colsInput.toIntOrNull()
    val minesN = minesInput.toIntOrNull()
    val customDimsValid = rowsN != null && colsN != null && minesN != null &&
            rowsN >= 4 && colsN >= 4 && minesN in 1 until (rowsN * colsN - 9)
    val customSeedValid = customSeedInput.isBlank() ||
            Regex("^[0-9A-Fa-f]{1,16}$").matches(customSeedInput.trim())
    val customValid = customDimsValid && customSeedValid

    val codeInvalid = parseGameCode(seedInput) is GameCodeParseResult.InvalidFormat

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            strings.difficultyLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (difficulty in Difficulty.entries) {
                AppFilterChip(
                    selected = !customMode && difficulty == pending,
                    onClick = { customMode = false; pending = difficulty },
                    label = { Text(strings.difficulty(difficulty)) },
                )
            }
            AppFilterChip(
                selected = customMode,
                onClick = { customMode = true },
                label = { Text("Custom") },
            )
        }
        if (customMode) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(
                    value = rowsInput,
                    onValueChange = { rowsInput = it.filter(Char::isDigit).take(3) },
                    placeholder = "rows",
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                AppTextField(
                    value = colsInput,
                    onValueChange = { colsInput = it.filter(Char::isDigit).take(3) },
                    placeholder = "cols",
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                AppTextField(
                    value = minesInput,
                    onValueChange = { minesInput = it.filter(Char::isDigit).take(5) },
                    placeholder = "mines",
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
            AppTextField(
                value = customSeedInput,
                onValueChange = { customSeedInput = it.filter { ch -> ch.isLetterOrDigit() }.take(16) },
                placeholder = "seed (hex, optional)",
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                if (!customSeedValid) strings.seedFormatError else strings.seedFormatHint,
                style = MaterialTheme.typography.bodyMedium,
                color = if (!customSeedValid) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                strings.orPasteSeed,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppTextField(
                value = seedInput,
                onValueChange = onSeedInputChange,
                placeholder = "E-7A3F19BDC0EE000",
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (codeInvalid) {
                Text(
                    strings.seedFormatError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                Text(
                    strings.seedFormatHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        AppButton(
            enabled = if (customMode) customValid else !codeInvalid,
            onClick = {
                when {
                    customMode -> if (customValid) onCustomGameRequested(rowsN!!, colsN!!, minesN!!, customSeedInput)
                    seedInput.isNotBlank() || pending == currentDifficulty -> onConfirm()
                    else -> onDifficultyPicked(pending)
                }
                onClose()
            },
        ) {
            Text(if (gameInProgress) strings.stopAndStartButton else strings.startButton)
        }
    }
}

@Composable
private fun FlagChip(
    count: Int,
    active: Boolean,
    onTap: () -> Unit,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
) {
    var origin by remember { mutableStateOf(Offset.Zero) }
    AppFlagToggle(
        count = count,
        active = active,
        modifier = Modifier
            .onGloballyPositioned { origin = it.positionInWindow() }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onTap() })
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { local -> onDragStart(origin + local) },
                    onDrag = { change, _ -> onDrag(origin + change.position) },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragCancel() },
                )
            },
    )
}

@Composable
private fun DropdownChevron() {
    val color = LocalContentColor.current
    Canvas(modifier = Modifier.size(width = 10.dp, height = 6.dp)) {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width / 2f, size.height)
            close()
        }
        drawPath(path, color)
    }
}
