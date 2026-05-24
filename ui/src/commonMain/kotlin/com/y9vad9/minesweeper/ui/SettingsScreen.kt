package com.y9vad9.minesweeper.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.y9vad9.minesweeper.logic.AppLocale
import com.y9vad9.minesweeper.logic.Settings
import com.y9vad9.minesweeper.logic.Skin
import com.y9vad9.minesweeper.logic.ThemeMode
import com.y9vad9.minesweeper.ui.AppFilterChip
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settings: Settings,
    onLocaleChange: (AppLocale) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onSkinChange: (Skin) -> Unit,
    modifier: Modifier = Modifier,
    showHeader: Boolean = true,
) {
    val strings = LocalStrings.current
    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (showHeader) {
            Text(
                strings.settingsHeader,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                strings.languageLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (locale in AppLocale.entries) {
                    AppFilterChip(
                        selected = settings.locale == locale,
                        onClick = { onLocaleChange(locale) },
                        label = { Text(locale.nativeName) },
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                strings.skinLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (skin in Skin.entries) {
                    AppFilterChip(
                        selected = settings.skin == skin,
                        onClick = { onSkinChange(skin) },
                        label = { Text(strings.skin(skin)) },
                    )
                }
            }
        }

        if (settings.skin != Skin.Classic) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    strings.themeLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (mode in ThemeMode.entries) {
                        AppFilterChip(
                            selected = settings.theme == mode,
                            onClick = { onThemeChange(mode) },
                            label = { Text(strings.theme(mode)) },
                        )
                    }
                }
            }
        }
    }
}
