@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.y9vad9.minesweeper.ui

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.SystemFont
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle as SkiaFontStyle

private fun resolve(candidates: List<String>): FontFamily {
    val mgr = FontMgr.default
    for (name in candidates) {
        val tf = try {
            mgr.matchFamilyStyle(name, SkiaFontStyle.NORMAL)
        } catch (_: Exception) {
            null
        }
        if (tf != null && tf.familyName.equals(name, ignoreCase = true)) {
            return FontFamily(
                SystemFont(name, FontWeight.Normal, FontStyle.Normal),
                SystemFont(name, FontWeight.Bold, FontStyle.Normal),
            )
        }
    }
    return FontFamily.SansSerif
}

object JvmClassicFonts : ClassicFonts {
    override val sans: FontFamily = resolve(
        listOf(
            "Tahoma",
            "Verdana",
            "Microsoft Sans Serif",
            "Liberation Sans",
            "DejaVu Sans",
        ),
    )

    override val mono: FontFamily = resolve(
        listOf(
            "Lucida Console",
            "Consolas",
            "Courier New",
            "DejaVu Sans Mono",
        ),
    )
}
