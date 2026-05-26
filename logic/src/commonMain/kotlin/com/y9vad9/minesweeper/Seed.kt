package com.y9vad9.minesweeper

import kotlin.jvm.JvmInline
import kotlin.random.Random

@JvmInline
value class Seed(val value: Long) {
    companion object {
        fun random(): Seed = Seed(Random.nextLong())
    }
}
