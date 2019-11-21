package info.spiralframework.formats.scripting.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry

class WaitFrameEntry(override val opcode: Int): LinEntry {
    companion object {
        val DR1: WaitFrameEntry
            get() = WaitFrameEntry(0x3B)

        val DR2: WaitFrameEntry
            get() = WaitFrameEntry(0x4C)
    }
    constructor(opCode: Int, args: IntArray): this(opCode)

    override val rawArguments: IntArray = intArrayOf()

    override fun format(): String = "Wait Frame|"
}