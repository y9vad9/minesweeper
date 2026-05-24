package com.y9vad9.minesweeper.ios

import com.y9vad9.minesweeper.data.iosGameStateRepository
import com.y9vad9.minesweeper.data.iosHistoryRepository
import com.y9vad9.minesweeper.data.iosSettingsRepository
import com.y9vad9.minesweeper.logic.GameStateRepository
import com.y9vad9.minesweeper.logic.HistoryRepository
import com.y9vad9.minesweeper.logic.SettingsRepository
import com.y9vad9.minesweeper.ui.CellInputModifier
import com.y9vad9.minesweeper.ui.FallbackLineChartPainter
import com.y9vad9.minesweeper.ui.IosCellInput
import com.y9vad9.minesweeper.ui.LineChartPainter
import com.y9vad9.minesweeper.ui.ClassicFonts
import com.y9vad9.minesweeper.ui.IosClassicFonts
import org.koin.dsl.module

val iosModule = module {
    single<SettingsRepository> { iosSettingsRepository() }
    single<HistoryRepository> { iosHistoryRepository() }
    single<GameStateRepository> { iosGameStateRepository() }
    single<ClassicFonts> { IosClassicFonts }
    single<CellInputModifier> { IosCellInput }
    single<LineChartPainter> { FallbackLineChartPainter }
}
