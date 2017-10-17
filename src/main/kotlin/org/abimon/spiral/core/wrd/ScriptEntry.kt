package org.abimon.spiral.core.wrd

import org.abimon.spiral.util.intArrayOfPairs
import org.abimon.spiral.util.shortToIntPair

data class ScriptEntry(val scriptID: Int, val labelID: Int): WRDScript {
    override val opCode: Int = 0x10
    override val rawArguments: IntArray = intArrayOfPairs(shortToIntPair(scriptID, true, false), shortToIntPair(labelID, true, false))
    override val cmdArguments: IntArray = intArrayOf(scriptID, labelID)
}