package info.spiralframework.formats.common.scripting.wrd.v3

import info.spiralframework.formats.common.scripting.wrd.WordScriptValue
import info.spiralframework.formats.common.scripting.wrd.WrdEntry

data class DRv3LabelEntry(val label: WordScriptValue.Label) : WrdEntry {
    constructor(opcode: Int, rawArguments: Array<WordScriptValue>): this(rawArguments[0] as WordScriptValue.Label)

    override val opcode: Int = 0x14
    override val arguments: Array<WordScriptValue> = arrayOf(label)
}