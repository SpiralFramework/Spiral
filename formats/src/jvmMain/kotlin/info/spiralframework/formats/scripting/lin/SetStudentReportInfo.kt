package info.spiralframework.formats.scripting.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry

data class SetStudentReportInfo(val characterID: Int, val arg2: Int, val state: Int): LinEntry {
    constructor(opCode: Int, args: IntArray): this(args[0], args[1], args[2])

    override val opcode: Int = 0x10
    override val rawArguments: IntArray = intArrayOf(characterID, arg2, state)

    override fun format(): String = "Set Report Info|$characterID, $arg2, $state"
}