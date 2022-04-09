package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.formats.common.scripting.osl.RawNumberValue

public class Dr1CheckFlagEntry(override val rawArguments: IntArray) : LinEntry {
    public companion object {
        public const val EQUALITY_NOT_EQUAL: Int = 0
        public const val EQUALITY_EQUAL: Int = 1
        public const val EQUALITY_LESS_THAN_EQUAL_TO: Int = 2
        public const val EQUALITY_GREATER_THAN_EQUAL_TO: Int = 3
        public const val EQUALITY_LESS_THAN: Int = 4
        public const val EQUALITY_GREATER_THAN: Int = 5

        public const val LOGICAL_OR: Int = 6
        public const val LOGICAL_AND: Int = 7

        public fun formatEquality(equality: Int): String = when (equality) {
            EQUALITY_NOT_EQUAL -> "!="
            EQUALITY_EQUAL -> "=="
            EQUALITY_LESS_THAN_EQUAL_TO -> "<="
            EQUALITY_GREATER_THAN_EQUAL_TO -> ">="
            EQUALITY_LESS_THAN -> "<"
            EQUALITY_GREATER_THAN -> ">"
            else -> "{$equality}"
        }

        public fun formatLogical(logical: Int): String = when (logical) {
            LOGICAL_OR -> "||"
            LOGICAL_AND -> "&&"
            else -> "{$logical}"
        }

        public fun formatInvertedEquality(equality: Int): String = when (equality) {
            EQUALITY_NOT_EQUAL -> "=="
            EQUALITY_EQUAL -> "!="
            EQUALITY_LESS_THAN_EQUAL_TO -> ">"
            EQUALITY_GREATER_THAN_EQUAL_TO -> "<"
            EQUALITY_LESS_THAN -> ">="
            EQUALITY_GREATER_THAN -> "<="
            else -> "{$equality}"
        }

        public fun formatInvertedLogical(logical: Int): String = when (logical) {
            LOGICAL_OR -> "&&"
            LOGICAL_AND -> "||"
            else -> "{$logical}"
        }
    }

    public data class LogicalCondition(val logicalOp: Int, val check: Int, val op: Int, val value: Int)
    public data class Condition(
        val check: Int,
        val op: Int,
        val value: Int,
        val extraConditions: List<LogicalCondition>
    )

    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x35

    public fun conditions(): Condition {
        //TODO: Figure out a way to properly represent this
        val extraConditions: MutableList<LogicalCondition> = ArrayList()
        for (i in 0 until (rawArguments.size - 4) / 5) {
            extraConditions.add(
                LogicalCondition(
                    get((i * 5) + 4),
                    getInt16BE((i * 5) + 5),
                    get((i * 5) + 7),
                    get((i * 5) + 8)
                )
            )
        }

        return Condition(getInt16BE(0), get(2), get(3), extraConditions)
    }

    @Suppress("DuplicatedCode")
    override fun LinTranspiler.transpileArguments(builder: StringBuilder): Unit =
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

            if (comparisonName != null) {
                if (comparisonName !in variables) {
                    variables[comparisonName] = RawNumberValue(comparisonN)
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
                        variables[logicalName] = RawNumberValue(logicalN)
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
                        variables[comparisonName] = RawNumberValue(comparisonN)
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