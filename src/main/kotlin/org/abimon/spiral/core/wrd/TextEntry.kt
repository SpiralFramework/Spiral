package org.abimon.spiral.core.wrd

import org.abimon.spiral.util.shortToIntArray

data class TextEntry(val id: Int): WRDScript {
    override val opCode: Int = 0x46
    override val rawArguments: IntArray = shortToIntArray(id, true, false)
}