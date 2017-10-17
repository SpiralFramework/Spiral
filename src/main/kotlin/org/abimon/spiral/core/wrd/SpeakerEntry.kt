package org.abimon.spiral.core.wrd

import org.abimon.spiral.util.shortToIntArray

data class SpeakerEntry(val charID: Int): WRDScript {
    override val opCode: Int = 0x53
    override val rawArguments: IntArray = shortToIntArray(charID, true, false)
}