package info.spiralframework.formats.scripting.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry

class EndFlagCheckEntry(): LinEntry {
    constructor(opCode: Int, args: IntArray): this()

    override val opcode: Int = 0x3C
    override val rawArguments: IntArray = intArrayOf()

    override fun format(): String = "End Flag Check|"
}