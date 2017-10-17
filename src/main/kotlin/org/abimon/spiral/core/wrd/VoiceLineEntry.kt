package org.abimon.spiral.core.wrd

import org.abimon.spiral.util.intArrayOfPairs
import org.abimon.spiral.util.shortToIntPair

data class VoiceLineEntry(val voiceLine: Int, val volumeControl: Int): WRDScript {
    override val opCode: Int = 0x19
    override val rawArguments: IntArray = intArrayOfPairs(shortToIntPair(voiceLine, true, false), shortToIntPair(volumeControl, true, false))
}