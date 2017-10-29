package org.abimon.spiral.core.formats.scripting

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.text.TXTFormat
import org.abimon.visi.io.DataSource

object NonstopFormat: SpiralFormat {
    override val name: String = "Nonstop Debate"
    override val extension: String? = ".dat"
    override val conversions: Array<SpiralFormat> = arrayOf(TXTFormat)

    override fun isFormat(source: DataSource): Boolean = false
}