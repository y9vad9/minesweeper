package com.y9vad9.minesweeper.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.y9vad9.minesweeper.ui.BombGlyph
import com.y9vad9.minesweeper.ui.EmojiBombGlyph
import com.y9vad9.minesweeper.ui.EmojiFlagGlyph
import com.y9vad9.minesweeper.ui.FlagGlyph

private val MinimalCellRenderer: CellRenderer =
    { modifier, _, background, content ->
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(3.dp))
                .background(background),
            contentAlignment = Alignment.Center,
            content = { content() },
        )
    }

private val MinimalBoardFrame: BoardFrameRenderer = { content -> content() }

private val MinimalBombRenderer: BombRenderer =
    { size, _ -> EmojiBombGlyph(size = size) }

private val MinimalFlagRenderer: FlagRenderer =
    { size, _, _ -> EmojiFlagGlyph(size = size) }

private val MaterialButtonRenderer: ButtonRenderer =
    { onClick, enabled, modifier, content ->
        Button(onClick = onClick, modifier = modifier, enabled = enabled, content = { content() })
    }

private val MaterialTextButtonRenderer: ButtonRenderer =
    { onClick, enabled, modifier, content ->
        TextButton(onClick = onClick, modifier = modifier, enabled = enabled, content = { content() })
    }

private val MaterialFilterChipRenderer: FilterChipRenderer =
    { selected, onClick, enabled, modifier, label ->
        FilterChip(
            selected = selected,
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            label = { label() },
            colors = FilterChipDefaults.filterChipColors(),
        )
    }

private val MinimalFlagToggleRenderer: FlagToggleRenderer =
    { count, active, modifier ->
        val colors = LocalGameColors.current
        val borderColor = if (active) colors.flag else MaterialTheme.colorScheme.outline
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = modifier
                .clip(RoundedCornerShape(6.dp))
                .border(width = if (active) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(6.dp))
                .background(if (active) colors.flag.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            EmojiFlagGlyph(size = 16.dp)
            Text(
                count.toString().padStart(2, '0'),
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace),
            )
        }
    }

private val MinimalVerticalDivider: @Composable () -> Unit = { VerticalDivider() }
private val MinimalHorizontalDivider: @Composable () -> Unit = { HorizontalDivider() }

private val MinimalListCardRenderer: ListCardRenderer =
    { onClick, modifier, content ->
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) { content() }
    }

@OptIn(ExperimentalMaterial3Api::class)
private val MinimalCompactNewGameRenderer: CompactNewGameRenderer =
    { label, isOpen, onOpen, onDismiss, form ->
        ExtendedFloatingActionButton(
            text = { Text(label) },
            icon = { Text("+", style = MaterialTheme.typography.titleLarge) },
            onClick = onOpen,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
        )
        if (isOpen) {
            ModalBottomSheet(
                onDismissRequest = onDismiss,
                sheetState = rememberModalBottomSheetState(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 8.dp, bottom = 24.dp),
                ) { form() }
            }
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
private fun classicCompactNewGameRenderer(chrome: ClassicChrome): CompactNewGameRenderer =
    { label, isOpen, onOpen, onDismiss, form ->
        BeveledChromeButton(
            chrome = chrome,
            primary = true,
            onClick = onOpen,
            enabled = true,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            content = { Text(label) },
        )
        if (isOpen) {
            ModalBottomSheet(
                onDismissRequest = onDismiss,
                sheetState = rememberModalBottomSheetState(),
                shape = RectangleShape,
                containerColor = chrome.face,
                contentColor = chrome.text,
                tonalElevation = 0.dp,
                dragHandle = null,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(chrome.face)
                        .bevel(
                            raised = true,
                            light = chrome.light,
                            dark = chrome.dark,
                            thickness = chrome.thickness,
                        )
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp, bottom = 24.dp),
                ) {
                    CompositionLocalProvider(LocalContentColor provides chrome.text) {
                        form()
                    }
                }
            }
        }
    }

private val MinimalDropdownMenuRenderer: DropdownMenuRenderer =
    { expanded, onDismissRequest, content ->
        DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
            content()
        }
    }

private val MinimalBottomNavRenderer: BottomNavRenderer =
    { items ->
        NavigationBar {
            for (item in items) {
                NavigationBarItem(
                    selected = item.selected,
                    onClick = item.onClick,
                    label = { Text(item.label) },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                )
            }
        }
    }

private val MaterialTextFieldRenderer: TextFieldRenderer =
    { value, onValueChange, placeholder, singleLine, modifier ->
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            placeholder = placeholder?.let { { Text(it) } },
            singleLine = singleLine,
        )
    }

private fun classicCellRenderer(style: ClassicCellStyle): CellRenderer =
    { modifier, isHidden, background, content ->
        Box(
            modifier = modifier
                .background(background)
                .let {
                    if (isHidden) it.bevel(raised = true, light = style.light, dark = style.dark, thickness = style.thickness)
                    else it.gridDivider(color = style.gridLine, thickness = style.gridLineThickness)
                },
            contentAlignment = Alignment.Center,
            content = { content() },
        )
    }

private fun classicBoardFrame(thickness: Dp, light: Color, dark: Color): BoardFrameRenderer =
    { content ->
        Box(
            Modifier
                .bevel(raised = false, light = light, dark = dark, thickness = thickness)
                .padding(thickness),
            content = { content() },
        )
    }

private val ClassicBombRenderer: BombRenderer =
    { size, color -> BombGlyph(size = size, bodyColor = color) }

private val ClassicFlagRenderer: FlagRenderer =
    { size, flagColor, poleColor -> FlagGlyph(size, flagColor, poleColor) }

private fun classicButtonRenderer(chrome: ClassicChrome, primary: Boolean): ButtonRenderer =
    { onClick, enabled, modifier, content ->
        BeveledChromeButton(chrome = chrome, primary = primary, onClick = onClick, enabled = enabled, modifier = modifier, content = content)
    }

private fun classicFilterChipRenderer(chrome: ClassicChrome): FilterChipRenderer =
    { selected, onClick, enabled, modifier, label ->
        BeveledChromeChip(chrome = chrome, selected = selected, onClick = onClick, enabled = enabled, modifier = modifier, label = label)
    }

private fun classicFlagToggleRenderer(
    chrome: ClassicChrome,
    flagColor: Color,
): FlagToggleRenderer = { count, active, modifier ->
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .background(chrome.face)
            .bevel(raised = !active, light = chrome.light, dark = chrome.dark, thickness = chrome.thickness)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        FlagGlyph(size = 16.dp, flagColor = flagColor, poleColor = chrome.text)
        Box(
            Modifier
                .background(ClassicVeryDark)
                .bevel(raised = false, light = chrome.dark, dark = chrome.light, thickness = 1.dp)
                .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
            Text(
                count.toString().padStart(3, '0'),
                color = ClassicDanger,
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = LocalClassicFonts.current.mono),
            )
        }
    }
}

private fun classicVerticalDivider(light: Color, dark: Color): @Composable () -> Unit = {
    Row(Modifier.fillMaxHeight()) {
        Box(Modifier.width(1.dp).fillMaxHeight().background(dark))
        Box(Modifier.width(1.dp).fillMaxHeight().background(light))
    }
}

private fun classicHorizontalDivider(light: Color, dark: Color): @Composable () -> Unit = {
    Column(Modifier.fillMaxWidth()) {
        Box(Modifier.height(1.dp).fillMaxWidth().background(dark))
        Box(Modifier.height(1.dp).fillMaxWidth().background(light))
    }
}

private fun classicBottomNavRenderer(chrome: ClassicChrome): BottomNavRenderer = { items ->
    Column {
        Box(Modifier.height(1.dp).fillMaxWidth().background(chrome.dark))
        Box(Modifier.height(1.dp).fillMaxWidth().background(chrome.light))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(chrome.face)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for (item in items) {
                ClassicTaskbarButton(chrome = chrome, item = item, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ClassicTaskbarButton(
    chrome: ClassicChrome,
    item: BottomNavItem,
    modifier: Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val raised = !(item.selected || pressed)
    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
            .background(chrome.face)
            .bevel(raised = raised, light = chrome.light, dark = chrome.dark, thickness = chrome.thickness)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = item.onClick,
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides chrome.text) {
            Text(item.label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

private fun classicListCardRenderer(chrome: ClassicChrome): ListCardRenderer =
    { onClick, modifier, content ->
        val interaction = remember { MutableInteractionSource() }
        val pressed by interaction.collectIsPressedAsState()
        val raised = !pressed
        Box(
            modifier = modifier
                .background(chrome.face)
                .bevel(raised = raised, light = chrome.light, dark = chrome.dark, thickness = chrome.thickness)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = onClick,
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            CompositionLocalProvider(LocalContentColor provides chrome.text) {
                content()
            }
        }
    }

private fun classicDropdownMenuRenderer(chrome: ClassicChrome): DropdownMenuRenderer =
    { expanded, onDismissRequest, content ->
        if (expanded) {
            Popup(
                onDismissRequest = onDismissRequest,
                properties = PopupProperties(focusable = true),
                offset = IntOffset(0, 32),
            ) {
                Box(
                    Modifier
                        .background(chrome.face)
                        .bevel(raised = true, light = chrome.light, dark = chrome.dark, thickness = chrome.thickness)
                        .padding(chrome.thickness),
                ) {
                    CompositionLocalProvider(LocalContentColor provides chrome.text) {
                        content()
                    }
                }
            }
        }
    }

private fun classicTextFieldRenderer(chrome: ClassicChrome): TextFieldRenderer =
    { value, onValueChange, placeholder, singleLine, modifier ->
        BeveledChromeTextField(chrome = chrome, value = value, onValueChange = onValueChange, placeholder = placeholder, singleLine = singleLine, modifier = modifier)
    }

internal data class ClassicCellStyle(
    val light: Color,
    val dark: Color,
    val thickness: Dp,
    val gridLine: Color,
    val gridLineThickness: Dp,
)

internal data class ClassicChrome(
    val face: Color,
    val light: Color,
    val dark: Color,
    val text: Color,
    val mutedText: Color,
    val thickness: Dp,
)

@Composable
private fun BeveledChromeButton(
    chrome: ClassicChrome,
    primary: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val raised = !pressed
    val textColor = if (enabled) chrome.text else chrome.mutedText
    @Suppress("UNUSED_VARIABLE") val unused = primary
    Box(
        modifier = modifier
            .heightIn(min = 28.dp)
            .widthIn(min = 64.dp)
            .background(chrome.face)
            .bevel(raised = raised, light = chrome.light, dark = chrome.dark, thickness = chrome.thickness)
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides textColor) {
            content()
        }
    }
}

@Composable
private fun BeveledChromeChip(
    chrome: ClassicChrome,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier,
    label: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val raised = !(selected || pressed)
    val textColor = if (enabled) chrome.text else chrome.mutedText
    Box(
        modifier = modifier
            .heightIn(min = 28.dp)
            .background(chrome.face)
            .bevel(raised = raised, light = chrome.light, dark = chrome.dark, thickness = chrome.thickness)
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides textColor) {
            label()
        }
    }
}

@Composable
private fun BeveledChromeTextField(
    chrome: ClassicChrome,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String?,
    singleLine: Boolean,
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .bevel(raised = false, light = chrome.light, dark = chrome.dark, thickness = chrome.thickness)
            .padding(chrome.thickness + 2.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = TextStyle(
                color = chrome.text,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            ),
            decorationBox = { inner ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                placeholder,
                                color = chrome.mutedText,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Start,
                            )
                        }
                        inner()
                    }
                }
            },
        )
    }
}

internal val ClassicGray = Color(0xFFC0C0C0)
internal val ClassicLight = Color(0xFFFFFFFF)
internal val ClassicDark = Color(0xFF808080)
internal val ClassicVeryDark = Color(0xFF000000)
internal val ClassicAccent = Color(0xFF000080)
internal val ClassicDanger = Color(0xFFFF0000)

internal val ClassicGameColors = GameColors(
    hiddenCell = ClassicGray,
    hiddenCellHover = Color(0xFFCFCFCF),
    revealedCell = ClassicGray,
    flag = ClassicDanger,
    mine = ClassicVeryDark,
    mineExploded = ClassicDanger,
    numbers = listOf(
        Color(0xFF0000FF),
        Color(0xFF008000),
        Color(0xFFFF0000),
        Color(0xFF000080),
        Color(0xFF800000),
        Color(0xFF008080),
        Color(0xFF000000),
        Color(0xFF808080),
    ),
)

internal object ClassicChromeTokens {
    val background = ClassicGray
    val surface = ClassicGray
    val surfaceVariant = Color(0xFFD4D0C8)
    val onBackground = ClassicVeryDark
    val onSurface = ClassicVeryDark
    val onSurfaceVariant = Color(0xFF404040)
    val primary = ClassicAccent
    val onPrimary = Color.White
    val secondary = ClassicDark
    val error = ClassicDanger
}

internal val MinimalDarkLookAndFeel = LookAndFeel(
    colors = DarkGameColors,
    gridGap = 2.dp,
    monoFont = FontFamily.Monospace,
    cell = MinimalCellRenderer,
    boardFrame = MinimalBoardFrame,
    bomb = MinimalBombRenderer,
    flag = MinimalFlagRenderer,
    button = MaterialButtonRenderer,
    textButton = MaterialTextButtonRenderer,
    filterChip = MaterialFilterChipRenderer,
    textField = MaterialTextFieldRenderer,
    flagToggle = MinimalFlagToggleRenderer,
    verticalDivider = MinimalVerticalDivider,
    horizontalDivider = MinimalHorizontalDivider,
    bottomNav = MinimalBottomNavRenderer,
    dropdownMenu = MinimalDropdownMenuRenderer,
    listCard = MinimalListCardRenderer,
    compactNewGame = MinimalCompactNewGameRenderer,
)

internal val MinimalLightLookAndFeel = MinimalDarkLookAndFeel.copy(colors = LightGameColors)

@Composable
internal fun classicLookAndFeel(): LookAndFeel {
    val fonts = LocalClassicFonts.current
    return remember(fonts) { buildClassicLookAndFeel(fonts) }
}

private fun buildClassicLookAndFeel(fonts: ClassicFonts): LookAndFeel {
    val cellStyle = ClassicCellStyle(
        light = ClassicLight,
        dark = ClassicDark,
        thickness = 2.dp,
        gridLine = ClassicDark,
        gridLineThickness = 1.dp,
    )
    val chrome = ClassicChrome(
        face = ClassicGray,
        light = ClassicLight,
        dark = ClassicDark,
        text = ClassicVeryDark,
        mutedText = Color(0xFF404040),
        thickness = 2.dp,
    )
    return LookAndFeel(
        colors = ClassicGameColors,
        gridGap = 0.dp,
        monoFont = fonts.mono,
        cell = classicCellRenderer(cellStyle),
        boardFrame = classicBoardFrame(thickness = 3.dp, light = ClassicLight, dark = ClassicDark),
        bomb = ClassicBombRenderer,
        flag = ClassicFlagRenderer,
        button = classicButtonRenderer(chrome, primary = true),
        textButton = classicButtonRenderer(chrome, primary = false),
        filterChip = classicFilterChipRenderer(chrome),
        textField = classicTextFieldRenderer(chrome),
        flagToggle = classicFlagToggleRenderer(chrome, flagColor = ClassicDanger),
        verticalDivider = classicVerticalDivider(ClassicLight, ClassicDark),
        horizontalDivider = classicHorizontalDivider(ClassicLight, ClassicDark),
        bottomNav = classicBottomNavRenderer(chrome),
        dropdownMenu = classicDropdownMenuRenderer(chrome),
        listCard = classicListCardRenderer(chrome),
        compactNewGame = classicCompactNewGameRenderer(chrome),
    )
}
