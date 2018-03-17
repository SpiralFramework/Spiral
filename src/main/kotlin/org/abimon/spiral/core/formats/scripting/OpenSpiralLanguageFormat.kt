package org.abimon.spiral.core.formats.scripting

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.game.DRGame
import java.io.InputStream

object OpenSpiralLanguageFormat: SpiralFormat {
    override val name: String = "Open Spiral Language"
    override val extension: String = "osl"
    override val conversions: Array<SpiralFormat> = arrayOf(LINFormat, WRDFormat)

    override fun isFormat(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Boolean {
        val text = String(dataSource().use { stream -> stream.readBytes() }, Charsets.UTF_8)

        val parser = OpenSpiralLanguageParser { fileName -> context(fileName)?.invoke()?.use { stream -> stream.readBytes() }}
        val result = parser.parse(text)
        return !result.hasErrors() && !result.valueStack.isEmpty
    }
}