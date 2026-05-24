package com.y9vad9.minesweeper.data

import com.y9vad9.minesweeper.logic.GameStateRepository
import kotlinx.browser.localStorage
import org.w3c.dom.get

private class LocalStorageGameBlobStore(private val key: String) : GameStateBlobStore {
    override suspend fun read(): String? = localStorage[key]
    override suspend fun write(json: String) {
        localStorage.setItem(key, json)
    }

    override suspend fun clear() {
        localStorage.removeItem(key)
    }
}

fun webGameStateRepository(): GameStateRepository =
    JsonGameStateRepository(LocalStorageGameBlobStore("minesweeper:game"))
