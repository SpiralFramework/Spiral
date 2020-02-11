package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry

inline class Dr1CheckGameParameterEntry(override val rawArguments: IntArray) : LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x36

    fun conditions(): String = buildString {
        //TODO: Figure out a way to properly represent this
        append("if-param (")
        val firstGroup = Triple(rawArguments[1], rawArguments[2], (rawArguments[3] shl 8) or rawArguments[4])
        append(firstGroup.first)
        append(" ")
        append(formatCondition(firstGroup.second))
        append(" ")
        append(firstGroup.third)
        for (i in 0 until (rawArguments.size - 5) / 6) {
            val comparatorN = rawArguments[(i * 6) + 5]
            append(" ")
            append(formatLogical(comparatorN))
            append(" ")
            val groupN = Triple(rawArguments[(i * 6) + 7], rawArguments[(i * 6) + 8], (rawArguments[(i * 6) + 9] shl 8) or rawArguments[(i * 6) + 10])
            append(groupN.first)
            append(" ")
            append(formatCondition(groupN.second))
            append(" ")
            append(groupN.third)
        }
        append(")")
    }

    private fun formatCondition(condition: Int): String = when (condition) {
        0 -> "!="
        1 -> "=="
        2 -> "<="
        3 -> ">="
        4 -> "<"
        5 -> ">"
        else -> "{$condition}"
    }

    private fun formatLogical(logical: Int): String = when (logical) {
        6 -> "||"
        7 -> "&&"
        else -> "{$logical}"
    }

    override fun format(): String = conditions()
}