package info.spiralframework.formats.scripting.wrd

data class VoiceLineEntry(val voiceLine: Int, val volumeControl: Int): WrdScript {
    constructor(opCode: Int, args: IntArray): this(args[0], args[1])

    override val opCode: Int = 0x19
    override val rawArguments: IntArray = intArrayOf(voiceLine, volumeControl)
}