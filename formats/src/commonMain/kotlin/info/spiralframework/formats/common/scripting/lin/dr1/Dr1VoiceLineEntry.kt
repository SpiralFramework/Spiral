package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry

inline class Dr1VoiceLineEntry(override val rawArguments: IntArray): LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x08

    val characterID: Int
        get() = rawArguments[0]

    val chapterID: Int
        get() = rawArguments[1]

    val voiceLineID: Int
        get() = (rawArguments[2] shl 8) or rawArguments[3]

    val volume: Int
        get() = rawArguments[4]

    override fun format(): String = "Voice Line|$characterID, $chapterID, ${voiceLineID shr 8}, ${voiceLineID and 0xFF}, $volume"
}