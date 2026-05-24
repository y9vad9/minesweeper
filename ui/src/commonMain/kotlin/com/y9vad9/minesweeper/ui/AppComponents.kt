package com.y9vad9.minesweeper.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) = LocalLookAndFeel.current.button(onClick, enabled, modifier, content)

@Composable
fun AppTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) = LocalLookAndFeel.current.textButton(onClick, enabled, modifier, content)

@Composable
fun AppFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) = LocalLookAndFeel.current.filterChip(selected, onClick, enabled, modifier, label)

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
) = LocalLookAndFeel.current.textField(value, onValueChange, placeholder, singleLine, modifier)

@Composable
fun AppFlagToggle(
    count: Int,
    active: Boolean,
    modifier: Modifier = Modifier,
) = LocalLookAndFeel.current.flagToggle(count, active, modifier)

@Composable
fun AppVerticalDivider() = LocalLookAndFeel.current.verticalDivider()

@Composable
fun AppHorizontalDivider() = LocalLookAndFeel.current.horizontalDivider()

@Composable
fun AppBottomNav(items: List<BottomNavItem>) = LocalLookAndFeel.current.bottomNav(items)

@Composable
fun AppDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) = LocalLookAndFeel.current.dropdownMenu(expanded, onDismissRequest, content)

@Composable
fun AppListCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) = LocalLookAndFeel.current.listCard(onClick, modifier, content)

@Composable
fun BoxScope.AppCompactNewGame(
    label: String,
    isOpen: Boolean,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    form: @Composable () -> Unit,
) {
    val renderer = LocalLookAndFeel.current.compactNewGame
    renderer(label, isOpen, onOpen, onDismiss, form)
}
