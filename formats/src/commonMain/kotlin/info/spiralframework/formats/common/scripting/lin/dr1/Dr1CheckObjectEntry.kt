package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

inline class Dr1CheckObjectEntry(override val rawArguments: IntArray) : MutableLinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    constructor(objectID: Int): this(intArrayOf(objectID))

    override val opcode: Int
        get() = 0x29

    var objectID: Int
        get() = rawArguments[0]
        set(value) = set(0, value)

    override fun format(): String = "Check Object|$objectID"
}