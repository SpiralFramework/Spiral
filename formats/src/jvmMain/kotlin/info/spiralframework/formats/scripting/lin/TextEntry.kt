package info.spiralframework.formats.scripting.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry

data class TextEntry(override var text: String?, override var textID: Int): LinEntry, LinTextScript {
    constructor(op: Int, args: IntArray): this(null, (args[0] shl 8) or args[1])

    override val opcode: Int = 0x02
    override val rawArguments: IntArray
        get() = intArrayOf(textID shr 8 and 0xFF, textID and 0xFF)
    override val writeByteOrderMarker: Boolean = true

    override fun format(): String = "Text|${text?.replace("\n", "&br")}"
}