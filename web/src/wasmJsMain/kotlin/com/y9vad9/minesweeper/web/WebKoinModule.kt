package com.y9vad9.minesweeper.web

import com.y9vad9.minesweeper.data.webGameStateRepository
import com.y9vad9.minesweeper.data.webHistoryRepository
import com.y9vad9.minesweeper.data.webSettingsRepository
import com.y9vad9.minesweeper.logic.GameStateRepository
import com.y9vad9.minesweeper.logic.HistoryRepository
import com.y9vad9.minesweeper.logic.SettingsRepository
import com.y9vad9.minesweeper.ui.AayLineChartPainter
import com.y9vad9.minesweeper.ui.CellInputModifier
import com.y9vad9.minesweeper.ui.LineChartPainter
import com.y9vad9.minesweeper.ui.WebCellInput
import com.y9vad9.minesweeper.ui.ClassicFonts
import com.y9vad9.minesweeper.ui.WebClassicFonts
import org.koin.dsl.module

val webModule = module {
    single<SettingsRepository> { webSettingsRepository() }
    single<HistoryRepository> { webHistoryRepository() }
    single<GameStateRepository> { webGameStateRepository() }
    single<ClassicFonts> { WebClassicFonts }
    single<CellInputModifier> { WebCellInput }
    single<LineChartPainter> { AayLineChartPainter }
}
