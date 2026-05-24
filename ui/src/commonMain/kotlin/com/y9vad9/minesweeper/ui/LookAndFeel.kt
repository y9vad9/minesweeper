package com.y9vad9.minesweeper.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp

data class LookAndFeel(
    val colors: GameColors,
    val gridGap: Dp,
    val monoFont: FontFamily,

    val cell: CellRenderer,
    val boardFrame: BoardFrameRenderer,

    val bomb: BombRenderer,
    val flag: FlagRenderer,

    val button: ButtonRenderer,
    val textButton: ButtonRenderer,
    val filterChip: FilterChipRenderer,
    val textField: TextFieldRenderer,
    val flagToggle: FlagToggleRenderer,
    val verticalDivider: @Composable () -> Unit,
    val horizontalDivider: @Composable () -> Unit,
    val bottomNav: BottomNavRenderer,
    val dropdownMenu: DropdownMenuRenderer,
    val listCard: ListCardRenderer,
    val compactNewGame: CompactNewGameRenderer,
)

typealias BombRenderer = @Composable (size: Dp, color: Color) -> Unit

typealias FlagRenderer =
    @Composable (size: Dp, flagColor: Color, poleColor: Color) -> Unit

typealias CellRenderer = @Composable (
    modifier: Modifier,
    isHidden: Boolean,
    background: Color,
    content: @Composable () -> Unit,
) -> Unit

typealias BoardFrameRenderer = @Composable (content: @Composable () -> Unit) -> Unit

typealias ButtonRenderer = @Composable (
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier,
    content: @Composable () -> Unit,
) -> Unit

typealias FilterChipRenderer = @Composable (
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier,
    label: @Composable () -> Unit,
) -> Unit

typealias TextFieldRenderer = @Composable (
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String?,
    singleLine: Boolean,
    modifier: Modifier,
) -> Unit

typealias FlagToggleRenderer = @Composable (
    count: Int,
    active: Boolean,
    modifier: Modifier,
) -> Unit

data class BottomNavItem(
    val selected: Boolean,
    val onClick: () -> Unit,
    val label: String,
    val icon: ImageVector,
)

typealias BottomNavRenderer = @Composable (items: List<BottomNavItem>) -> Unit

typealias DropdownMenuRenderer = @Composable (
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) -> Unit

typealias ListCardRenderer = @Composable (
    onClick: () -> Unit,
    modifier: Modifier,
    content: @Composable () -> Unit,
) -> Unit

typealias CompactNewGameRenderer = @Composable BoxScope.(
    label: String,
    isOpen: Boolean,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    form: @Composable () -> Unit,
) -> Unit

val LocalLookAndFeel = staticCompositionLocalOf { MinimalDarkLookAndFeel }
