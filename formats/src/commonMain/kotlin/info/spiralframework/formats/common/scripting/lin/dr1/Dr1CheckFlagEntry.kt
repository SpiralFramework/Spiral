package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry

inline class Dr1CheckFlagEntry(override val rawArguments: IntArray) : LinEntry {
    constructor(opcode: Int, rawArguments: IntArray): this(rawArguments)

    override val opcode: Int
        get() = 0x35

    fun conditions(): String = buildString {
        //TODO: Figure out a way to properly represent this
        append("if (")
        val firstGroup = Triple((rawArguments[0] shl 8) or rawArguments[1], rawArguments[2], rawArguments[3])
        append(firstGroup.first)
        append(" ")
        append(formatCondition(firstGroup.second))
        append(" ")
        append(firstGroup.third)
        for (i in 0 until (rawArguments.size - 4) / 5) {
            val comparatorN = rawArguments[(i * 5) + 4]
            append(" ")
            append(formatLogical(comparatorN))
            append(" ")
            val groupN = Triple((rawArguments[(i * 5) + 5] shl 8) or rawArguments[(i * 5) + 6], rawArguments[(i * 5) + 7], rawArguments[(i * 5) + 8])
            append(groupN.first)
            append(" ")
            append(formatCondition(groupN.second))
            append(" ")
            append(groupN.third)
        }
        append(") {}")
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