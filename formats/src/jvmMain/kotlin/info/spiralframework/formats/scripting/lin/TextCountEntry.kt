package info.spiralframework.formats.scripting.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry

data class TextCountEntry(val lines: Int): LinEntry {
    constructor(rem: Int, main: Int): this(rem + main * 256)
    constructor(op: Int, args: IntArray): this(args[0], args[1])

    override val opcode: Int = 0x00
    override val rawArguments: IntArray = intArrayOf(lines % 256, lines / 256)

    override fun format(): String = "Text Count|$lines"
}