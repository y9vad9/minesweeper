package com.y9vad9.minesweeper.data

import com.y9vad9.minesweeper.data.minesweeperHomeDir
import com.y9vad9.minesweeper.logic.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private class FileSettingsBlobStore(private val file: File) : SettingsBlobStore {
    override suspend fun read(): String? = withContext(Dispatchers.IO) {
        if (file.exists()) file.readText() else null
    }

    override suspend fun write(json: String) = withContext(Dispatchers.IO) {
        file.parentFile?.mkdirs()
        file.writeText(json)
    }
}

fun jvmSettingsRepository(): SettingsRepository {
    val file = File(minesweeperHomeDir(), "settings.json")
    return JsonSettingsRepository(FileSettingsBlobStore(file))
}
