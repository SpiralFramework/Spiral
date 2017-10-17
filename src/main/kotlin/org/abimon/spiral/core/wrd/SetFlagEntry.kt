package org.abimon.spiral.core.wrd

import org.abimon.spiral.util.intArrayOfPairs
import org.abimon.spiral.util.shortToIntPair

data class SetFlagEntry(val valueID: Int, val flagID: Int): WRDScript {
    override val opCode: Int = 0x00
    override val rawArguments: IntArray = intArrayOfPairs(shortToIntPair(valueID, true, false), shortToIntPair(flagID, true, false))
    override val cmdArguments: IntArray = intArrayOf(valueID, flagID)
}