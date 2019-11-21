package info.spiralframework.formats.scripting.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry

data class TruthBulletEntry(val arg1: Int, val arg2: Int): LinEntry {
    constructor(opCode: Int, args: IntArray): this(args[0], args[1])
    
    override val opcode: Int = 0x0C
    override val rawArguments: IntArray = intArrayOf(arg1, arg2)

    override fun format(): String = "Truth Bullet|$arg1, $arg2"
}