package org.abimon.spiral.core.formats.audio

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.game.DRGame
import java.io.InputStream

object OggFormat: SpiralFormat {
    override val name: String = "Ogg"
    override val extension: String? = "ogg"
    override val conversions: Array<SpiralFormat> = emptyArray()
    val header = byteArrayOf(0x4F, 0x67, 0x67, 0x53, 0x00, 0x02, 0x00, 0x00)

    override fun isFormat(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Boolean = dataSource().use { stream -> ByteArray(8).apply { stream.read(this) } contentEquals header }
}