package org.abimon.spiral.core.objects.scripting.lin

data class SpeakerEntry(val characterID: Int): LinScript {
    constructor(opCode: Int, args: IntArray): this(args[0])

    override val opCode: Int = 0x21
    override val rawArguments: IntArray = intArrayOf(characterID)

    override fun format(): String = "Speaker|$characterID"
}