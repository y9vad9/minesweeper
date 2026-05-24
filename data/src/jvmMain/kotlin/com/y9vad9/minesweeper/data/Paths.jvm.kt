package com.y9vad9.minesweeper.data

import java.io.File

internal fun minesweeperHomeDir(): File {
    val home = System.getProperty("user.home") ?: "."
    return File(home, ".minesweeper")
}
