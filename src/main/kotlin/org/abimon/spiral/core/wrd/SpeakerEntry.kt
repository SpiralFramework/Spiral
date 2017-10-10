package org.abimon.spiral.core.wrd

import org.abimon.spiral.util.shortToIntArray

data class SpeakerEntry(val charID: Int): WRDScript {
    override fun getOpCode(): Int = 0x53
    override fun getRawArguments(): IntArray = shortToIntArray(charID, true, false)
}