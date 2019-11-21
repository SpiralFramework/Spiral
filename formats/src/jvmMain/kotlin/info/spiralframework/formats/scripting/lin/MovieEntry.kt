package info.spiralframework.formats.scripting.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry

data class MovieEntry(val id: Int): LinEntry {
    constructor(op: Int, args: IntArray) : this((args[0] shl 8) or args[1])

    override val opcode: Int = 0x05
    override val rawArguments: IntArray = intArrayOf(id shr 8, id % 256)

    override fun format(): String = "Movie|${id shr 8}, ${id % 256}"
}