package info.spiralframework.formats.scripting.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry

class StopScriptEntry(): LinEntry {
    companion object static {
        @JvmStatic
        fun fromOp(opCode: Int, args: IntArray): StopScriptEntry = StopScriptEntry()
    }

    constructor(opCode: Int, args: IntArray): this()

    override val opcode: Int = 0x1A
    override val rawArguments: IntArray = intArrayOf()

    override fun format(): String = "Stop Script|"
}