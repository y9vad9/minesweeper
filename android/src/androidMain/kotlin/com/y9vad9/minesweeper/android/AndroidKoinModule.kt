package com.y9vad9.minesweeper.android

import com.y9vad9.minesweeper.data.androidGameStateRepository
import com.y9vad9.minesweeper.data.androidHistoryRepository
import com.y9vad9.minesweeper.data.androidSettingsRepository
import com.y9vad9.minesweeper.logic.GameStateRepository
import com.y9vad9.minesweeper.logic.HistoryRepository
import com.y9vad9.minesweeper.logic.SettingsRepository
import com.y9vad9.minesweeper.ui.AayLineChartPainter
import com.y9vad9.minesweeper.ui.AndroidCellInput
import com.y9vad9.minesweeper.ui.CellInputModifier
import com.y9vad9.minesweeper.ui.LineChartPainter
import com.y9vad9.minesweeper.ui.AndroidClassicFonts
import com.y9vad9.minesweeper.ui.ClassicFonts
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    single<SettingsRepository> { androidSettingsRepository(androidContext()) }
    single<HistoryRepository> { androidHistoryRepository(androidContext()) }
    single<GameStateRepository> { androidGameStateRepository(androidContext()) }
    single<ClassicFonts> { AndroidClassicFonts }
    single<CellInputModifier> { AndroidCellInput }
    single<LineChartPainter> { AayLineChartPainter }
}
