package com.y9vad9.minesweeper.data

import android.content.Context
import com.y9vad9.minesweeper.GameStateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

internal class FileGameStateBlobStore(private val file: File) : GameStateBlobStore {
    override suspend fun read(): String? = withContext(Dispatchers.IO) {
        if (file.exists()) file.readText() else null
    }

    override suspend fun write(json: String) = withContext(Dispatchers.IO) {
        file.parentFile?.mkdirs()
        file.writeText(json)
    }

    override suspend fun clear() {
        withContext(Dispatchers.IO) { if (file.exists()) file.delete() }
    }
}

fun androidGameStateRepository(context: Context): GameStateRepository {
    val file = File(context.applicationContext.filesDir, "game.json")
    return JsonGameStateRepository(FileGameStateBlobStore(file))
}
