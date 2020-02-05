package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

inline class Dr1GoToLabelEntry(override val rawArguments: IntArray) : MutableLinEntry {
    constructor(id: Int) : this(intArrayOf((id shr 0) and 0xFF, (id shr 8) and 0xFF))
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x34

    var id: Int
        get() = getInt16LE(0)
        set(value) = setInt16LE(0, value)

    override fun format(): String = "Go To Label|${rawArguments[0]}, ${rawArguments[1]}"
}