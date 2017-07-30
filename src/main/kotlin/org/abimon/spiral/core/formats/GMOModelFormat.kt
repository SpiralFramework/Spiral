package org.abimon.spiral.core.formats

import org.abimon.spiral.core.readString
import org.abimon.visi.io.DataSource

object GMOModelFormat: SpiralFormat {
    override val name: String = "GMO Model Format"
    override val extension: String? = "gmo"
    override val conversions: Array<SpiralFormat> = emptyArray()

    override fun isFormat(source: DataSource): Boolean = source.use { stream -> stream.readString(12) == "OMG.00.1PSP\u0000" }
}