package com.y9vad9.minesweeper.desktop

import com.y9vad9.minesweeper.data.jvmGameStateRepository
import com.y9vad9.minesweeper.data.jvmHistoryRepository
import com.y9vad9.minesweeper.ui.jvmSettingsRepository
import com.y9vad9.minesweeper.GameStateRepository
import com.y9vad9.minesweeper.HistoryRepository
import com.y9vad9.minesweeper.ui.SettingsRepository
import com.y9vad9.minesweeper.ui.AayLineChartPainter
import com.y9vad9.minesweeper.ui.CellInputModifier
import com.y9vad9.minesweeper.ui.JvmCellInput
import com.y9vad9.minesweeper.ui.LineChartPainter
import com.y9vad9.minesweeper.ui.ClassicFonts
import com.y9vad9.minesweeper.ui.JvmClassicFonts
import org.koin.dsl.module

val desktopModule = module {
    single<SettingsRepository> { jvmSettingsRepository() }
    single<HistoryRepository> { jvmHistoryRepository() }
    single<GameStateRepository> { jvmGameStateRepository() }
    single<ClassicFonts> { JvmClassicFonts }
    single<CellInputModifier> { JvmCellInput }
    single<LineChartPainter> { AayLineChartPainter }
}
