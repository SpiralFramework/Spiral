package info.spiralframework.formats.scripting.lin.udg

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.scripting.lin.LinTextScript

data class UDGTextEntry(override var text: String?, override var textID: Int): LinEntry, LinTextScript {
    constructor(op: Int, args: IntArray): this(null, (args[0] shl 8) or args[1])

    override val opcode: Int = 0x01
    override val rawArguments: IntArray = intArrayOf(textID shr 8 and 0xFF, textID and 0xFF)
    override val writeByteOrderMarker: Boolean = false
}