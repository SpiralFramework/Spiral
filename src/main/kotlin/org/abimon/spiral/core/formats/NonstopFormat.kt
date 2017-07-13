package org.abimon.spiral.core.formats

import org.abimon.visi.io.DataSource

object NonstopFormat: SpiralFormat {
    override val name: String = "Nonstop Debate"
    override val extension: String? = ".dat"

    override fun isFormat(source: DataSource): Boolean = false

    override fun canConvert(format: SpiralFormat): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}