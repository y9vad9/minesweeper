package com.y9vad9.minesweeper.ui

import platform.Foundation.NSUserDefaults

private class UserDefaultsBlobStore(private val key: String) : SettingsBlobStore {
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
    override suspend fun read(): String? = defaults.stringForKey(key)
    override suspend fun write(json: String) {
        defaults.setObject(json, forKey = key)
    }
}

fun iosSettingsRepository(): SettingsRepository =
    JsonSettingsRepository(UserDefaultsBlobStore("minesweeper:settings"))
