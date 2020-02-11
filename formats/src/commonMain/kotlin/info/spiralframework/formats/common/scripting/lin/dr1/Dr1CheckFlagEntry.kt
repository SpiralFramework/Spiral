package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.formats.common.scripting.osl.NumberValue

inline class Dr1CheckFlagEntry(override val rawArguments: IntArray) : LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x35

    fun conditions(): String = buildString {
        //TODO: Figure out a way to properly represent this
        append("if (")
        val firstGroup = Triple("(${rawArguments[0]},${rawArguments[1]})", rawArguments[2], rawArguments[3])
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
            val groupN = Triple("(${rawArguments[(i * 5) + 5]},${rawArguments[(i * 5) + 6]})", rawArguments[(i * 5) + 7], rawArguments[(i * 5) + 8])
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

    @ExperimentalUnsignedTypes
    override fun LinTranspiler.transpileArguments(builder: StringBuilder) =
            with(builder) {
                append(rawArguments[0])
                append(", ")
                append(rawArguments[1])
                append(", ")

                var comparisonN = rawArguments[2]
                var comparisonName = when (comparisonN) {
                    0 -> "condition_not_equal_to"
                    1 -> "condition_equal_to"
                    2 -> "condition_less_than_or_equal_to"
                    3 -> "condition_greater_than_or_equal_to"
                    4 -> "condition_less_than"
                    5 -> "condition_greater_than"
                    else -> null
                }

                @Suppress("DuplicatedCode")
                if (comparisonName != null) {
                    if (comparisonName !in variables) {
                        variables[comparisonName] = NumberValue(comparisonN)
                    }

                    append('$')
                    append(comparisonName)
                    append(", ")
                } else {
                    append(comparisonN)
                    append(", ")
                }

                append(rawArguments[3])

                var logicalName: String?
                var logicalN: Int

                for (i in 0 until (rawArguments.size - 4) / 5) {
                    logicalN = rawArguments[(i * 5) + 4]
                    logicalName = when (logicalN) {
                        6 -> "logical_or"
                        7 -> "logical_and"
                        else -> null
                    }
                    append(", ")

                    @Suppress("DuplicatedCode")
                    if (logicalName != null) {
                        if (logicalName !in variables) {
                            variables[logicalName] = NumberValue(logicalN)
                        }

                        append('$')
                        append(logicalName)
                        append(", ")
                    } else {
                        append(logicalN)
                        append(", ")
                    }

                    append(rawArguments[(i * 5) + 5])
                    append(", ")
                    append(rawArguments[(i * 5) + 6])
                    append(", ")

                    comparisonN = rawArguments[(i * 5) + 7]
                    comparisonName = when (comparisonN) {
                        0 -> "condition_not_equal_to"
                        1 -> "condition_equal_to"
                        2 -> "condition_less_than_or_equal_to"
                        3 -> "condition_greater_than_or_equal_to"
                        4 -> "condition_less_than"
                        5 -> "condition_greater_than"
                        else -> null
                    }

                    @Suppress("DuplicatedCode")
                    if (comparisonName != null) {
                        if (comparisonName !in variables) {
                            variables[comparisonName] = NumberValue(comparisonN)
                        }

                        append('$')
                        append(comparisonName)
                        append(", ")
                    } else {
                        append(comparisonN)
                        append(", ")
                    }

                    append(rawArguments[(i * 5) + 8])
                }
            }
}