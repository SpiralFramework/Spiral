package org.abimon.spiral.core.wrd

import org.abimon.spiral.util.shortToIntArray

data class TextEntry(val id: Int): WRDScript {
    override fun getOpCode(): Int = 0x46

    override fun getRawArguments(): IntArray = shortToIntArray(id, true, false)
}