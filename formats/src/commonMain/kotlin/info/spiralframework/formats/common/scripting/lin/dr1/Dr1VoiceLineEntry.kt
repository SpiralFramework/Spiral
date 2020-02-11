package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

inline class Dr1VoiceLineEntry(override val rawArguments: IntArray): MutableLinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    constructor(characterID: Int, chapterID: Int, voiceLineID: Int, volume: Int): this(intArrayOf(characterID, chapterID, voiceLineID, volume))

    override val opcode: Int
        get() = 0x08

    var characterID: Int
        get() = get(0)
        set(value) = set(0, value)

    var chapterID: Int
        get() = get(1)
        set(value) = set(1, value)

    var voiceLineID: Int
        get() = getInt16BE(2)
        set(value) = setInt16BE(2, value)

    var volume: Int
        get() = get(4)
        set(value) = set(4, value)
}