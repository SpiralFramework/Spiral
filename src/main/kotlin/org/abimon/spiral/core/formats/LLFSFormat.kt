package org.abimon.spiral.core.formats

import org.abimon.spiral.core.readString
import org.abimon.visi.io.DataSource

object LLFSFormat: SpiralFormat {
    override val name: String = "LLFS"
    override val extension: String? = "sfl"
    override val conversions: Array<SpiralFormat> = emptyArray()

    override fun isFormat(source: DataSource): Boolean = source.use { stream -> stream.readString(4) == "LLFS" }
}