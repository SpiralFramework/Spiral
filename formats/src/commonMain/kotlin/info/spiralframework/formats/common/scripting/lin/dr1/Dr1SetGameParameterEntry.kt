package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

inline class Dr1SetGameParameterEntry(override val rawArguments: IntArray) : MutableLinEntry {
    companion object {
        const val OPERATOR_SET = 0x00
        const val OPERATOR_PlUS = 0x01
        const val OPERATOR_MINUS = 0x02
        const val OPERATOR_TIMES = 0x03
        const val OPERATOR_DIVIDE = 0x04
    }

    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    constructor(variable: Int, operation: Int, value: Int) : this(intArrayOf(variable, operation, (value shr 8) and 0xFF, value and 0xFF))

    override val opcode: Int
        get() = 0x33

    var variable: Int
        get() = get(0)
        set(value) = set(0, value)

    var operation: Int
        get() = get(1)
        set(value) = set(1, value)

    var value: Int
        get() = getInt16BE(2) //rawArguments[2] shl 8) or rawArguments[3]
        set(value) = setInt16BE(2, value)

    override fun format(): String = "Set Game Parameter|$variable, $operation, ${rawArguments[2]}, ${rawArguments[3]}"
}