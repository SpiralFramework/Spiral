package org.abimon.spiral.core.wrd

import org.abimon.spiral.util.shortToIntArray

data class LabelEntry(val labelID: Int): WRDScript {
    override val opCode: Int = 0x14
    override val rawArguments: IntArray = shortToIntArray(labelID, true, false)
    override val cmdArguments: IntArray = intArrayOf(labelID)
}