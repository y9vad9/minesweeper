package com.y9vad9.minesweeper.data

import com.y9vad9.minesweeper.logic.SettingsRepository
import kotlinx.browser.localStorage
import org.w3c.dom.get

private class LocalStorageBlobStore(private val key: String) : SettingsBlobStore {
    override suspend fun read(): String? = localStorage[key]
    override suspend fun write(json: String) {
        localStorage.setItem(key, json)
    }
}

fun webSettingsRepository(): SettingsRepository =
    JsonSettingsRepository(LocalStorageBlobStore("minesweeper:settings"))
