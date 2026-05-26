package com.y9vad9.minesweeper.data

import com.y9vad9.minesweeper.HistoryRepository
import kotlinx.browser.localStorage
import org.w3c.dom.get

private class LocalStorageHistoryBlobStore(private val key: String) : HistoryBlobStore {
    override suspend fun read(): String? = localStorage[key]
    override suspend fun write(json: String) {
        localStorage.setItem(key, json)
    }
}

fun webHistoryRepository(): HistoryRepository =
    JsonHistoryRepository(LocalStorageHistoryBlobStore("minesweeper:history"))
