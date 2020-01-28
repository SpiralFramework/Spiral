package info.spiralframework.formats.common.scripting.wrd.v3

import info.spiralframework.formats.common.scripting.wrd.WordScriptValue
import info.spiralframework.formats.common.scripting.wrd.WrdEntry

data class DRv3ScriptEntry(val scriptName: WordScriptValue.Parameter, val label: WordScriptValue.Label): WrdEntry {
    constructor(opcode: Int, arguments: Array<WordScriptValue>): this(arguments[0] as WordScriptValue.Parameter, arguments[1] as WordScriptValue.Label)

    override val opcode: Int = 0x10
    override val arguments: Array<WordScriptValue> = arrayOf(scriptName, label)
}