package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

inline class Dr1MarkLabelEntry(override val rawArguments: IntArray) : MutableLinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    constructor(id: Int) : this(intArrayOf(id))

    override val opcode: Int
        get() = 0x2A

    var id: Int
        get() = getInt16LE(0)
        set(value) = setInt16LE(0, value)

    override fun format(): String = "Mark Label|${rawArguments[0]}, ${rawArguments[1]}"
}