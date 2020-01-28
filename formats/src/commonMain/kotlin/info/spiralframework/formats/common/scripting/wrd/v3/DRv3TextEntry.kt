package info.spiralframework.formats.common.scripting.wrd.v3

import info.spiralframework.formats.common.scripting.wrd.WordScriptValue
import info.spiralframework.formats.common.scripting.wrd.WrdEntry

data class DRv3TextEntry(val text: WordScriptValue.Text): WrdEntry {
    constructor(opcode: Int, arguments: Array<WordScriptValue>): this(arguments[0] as WordScriptValue.Text)

    override val opcode: Int = 0x46
    override val arguments: Array<WordScriptValue> = arrayOf(text)
}