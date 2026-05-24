package com.y9vad9.minesweeper.data

import com.y9vad9.minesweeper.logic.GameStateRepository
import platform.Foundation.NSUserDefaults

private class UserDefaultsGameBlobStore(private val key: String) : GameStateBlobStore {
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
    override suspend fun read(): String? = defaults.stringForKey(key)
    override suspend fun write(json: String) {
        defaults.setObject(json, forKey = key)
    }

    override suspend fun clear() {
        defaults.removeObjectForKey(key)
    }
}

fun iosGameStateRepository(): GameStateRepository =
    JsonGameStateRepository(UserDefaultsGameBlobStore("minesweeper:game"))
