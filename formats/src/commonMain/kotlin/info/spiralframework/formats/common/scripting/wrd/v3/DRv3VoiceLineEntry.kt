package info.spiralframework.formats.common.scripting.wrd.v3

import info.spiralframework.formats.common.scripting.wrd.WordScriptValue
import info.spiralframework.formats.common.scripting.wrd.WrdEntry

data class DRv3VoiceLineEntry(val voiceLine: WordScriptValue.Parameter, val volumeControl: WordScriptValue.Parameter): WrdEntry {
    constructor(opcode: Int, arguments: Array<WordScriptValue>): this(arguments[0] as WordScriptValue.Parameter, arguments[1] as WordScriptValue.Parameter)

    override val opcode: Int = 0x19
    override val arguments: Array<WordScriptValue> = arrayOf(voiceLine, volumeControl)
}