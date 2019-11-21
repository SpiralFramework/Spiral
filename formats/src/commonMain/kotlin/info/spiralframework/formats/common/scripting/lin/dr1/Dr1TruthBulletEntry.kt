package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry

/** Give truth bullet? */
inline class Dr1TruthBulletEntry(override val rawArguments: IntArray): LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x0C

    val arg1: Int
        get() = rawArguments[0]

    val arg2: Int
        get() = rawArguments[1]

    override fun format(): String = "Truth Bullet|$arg1, $arg2"
}