package org.abimon.spiral.core.formats

import org.abimon.spiral.core.readString
import org.abimon.visi.io.DataSource

object LLFSFormat: SpiralFormat {
    override val name: String = "LLFS"
    override val extension: String? = "llfs"

    override fun isFormat(source: DataSource): Boolean = source.use { stream -> stream.readString(4) == "LLFS" }

    override fun canConvert(format: SpiralFormat): Boolean = false
}