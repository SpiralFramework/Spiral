package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

inline class Dr1TextEntry(override val rawArguments: IntArray) : MutableLinEntry {
    constructor(textID: Int) : this(intArrayOf((textID shr 8) and 0xFF, textID and 0xFF))
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x02

    var textID: Int
        get() = getInt16BE(0)
        set(value) = setInt16BE(0, value)
}