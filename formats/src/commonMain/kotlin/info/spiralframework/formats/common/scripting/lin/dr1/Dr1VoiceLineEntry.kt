package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry
import info.spiralframework.formats.common.scripting.osl.LinTranspiler

public class Dr1VoiceLineEntry(override val rawArguments: IntArray) : MutableLinEntry {
    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    public constructor(characterID: Int, chapterID: Int, voiceLineID: Int, volume: Int) : this(
        intArrayOf(
            characterID,
            chapterID,
            (voiceLineID shl 8),
            (voiceLineID and 0xFF),
            volume
        )
    )

    override val opcode: Int
        get() = 0x08

    public var characterID: Int
        get() = get(0)
        set(value) = set(0, value)

    public var chapterID: Int
        get() = get(1)
        set(value) = set(1, value)

    public var voiceLineID: Int
        get() = getInt16BE(2)
        set(value) = setInt16BE(2, value)

    public var volume: Int
        get() = get(4)
        set(value) = set(4, value)

    override fun LinTranspiler.transpile(indent: Int) {
        addOutput {
            repeat(indent) { append('\t') }
            val fileID = game?.getLinVoiceFileID(characterID, chapterID, voiceLineID)
            if (fileID != null) {
                append("Speak(")
                append(fileID)
                append(", ")
                append(volume)
                append(") //")
                append(characterID)
                append(", ")
                append(chapterID)
                append(", ")
                append(voiceLineID)
            } else {
                append(nameFor(this@Dr1VoiceLineEntry))
                append('|')
                transpileArguments(this)
            }
        }
    }
}