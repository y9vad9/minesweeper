package com.y9vad9.minesweeper.android

import android.app.Application
import com.y9vad9.minesweeper.ui.appStoresModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MinesweeperApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MinesweeperApp)
            modules(androidModule, appStoresModule)
        }
    }
}
