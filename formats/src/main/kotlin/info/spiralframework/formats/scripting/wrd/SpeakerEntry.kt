package info.spiralframework.formats.scripting.wrd

data class SpeakerEntry(val charID: Int): WrdScript {
    constructor(opCode: Int, args: IntArray): this(args[0])

    override val opCode: Int = 0x1D
    override val rawArguments: IntArray = intArrayOf(charID)
}