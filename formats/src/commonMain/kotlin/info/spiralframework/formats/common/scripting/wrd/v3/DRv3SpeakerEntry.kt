package info.spiralframework.formats.common.scripting.wrd.v3

import info.spiralframework.formats.common.scripting.wrd.WordScriptValue
import info.spiralframework.formats.common.scripting.wrd.WrdEntry

public data class DRv3SpeakerEntry(val speaker: WordScriptValue.Parameter) : WrdEntry {
    public constructor(opcode: Int, arguments: Array<WordScriptValue>) : this(arguments[0] as WordScriptValue.Parameter)

    override val opcode: Int = 0x1D
    override val arguments: Array<WordScriptValue> = arrayOf(speaker)
}