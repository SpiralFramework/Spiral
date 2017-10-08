package org.abimon.spiral.core.formats

import org.abimon.visi.collections.byteArrayOf
import org.abimon.visi.io.DataSource

object OggFormat: SpiralFormat {
    override val name: String = "Ogg"
    override val extension: String? = "ogg"
    override val conversions: Array<SpiralFormat> = emptyArray()
    val header = byteArrayOf(0x4F, 0x67, 0x67, 0x53, 0x00, 0x02)

    override fun isFormat(source: DataSource): Boolean = source.use { stream -> ByteArray(8).apply { stream.read(this) } contentEquals header }
}