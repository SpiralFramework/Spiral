package org.abimon.spiral.core.formats.scripting

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.readString
import org.abimon.visi.io.DataSource

object SFLFormat: SpiralFormat {
    override val name: String = "SFL"
    override val extension: String? = "sfl"
    override val conversions: Array<SpiralFormat> = emptyArray()

    override fun isFormat(source: DataSource): Boolean = source.use { stream -> stream.readString(4) == "LLFS" }
}