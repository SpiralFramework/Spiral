package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.formats.common.scripting.osl.NumberValue

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

    @Suppress("DuplicatedCode")
    @ExperimentalUnsignedTypes
    override fun LinTranspiler.transpileArguments(builder: StringBuilder) =
            with(builder) {
                var parameterName = game?.getNameOfGameParameter(rawArguments[1])
                        ?.toLowerCase()
                        ?.replace(' ', '_')
                        ?.replace(LinTranspiler.ILLEGAL_VARIABLE_NAME_CHARACTER_REGEX, "")

                if (parameterName != null) {
                    val parameterVariable = "game_parameter_$parameterName"
                    if (parameterVariable !in variables)
                        variables[parameterVariable] = NumberValue(rawArguments[1])

                    append(rawArguments[0])
                    append(", $")
                    append(parameterVariable)
                    append(", ")
                } else {
                    append(rawArguments[0])
                    append(", ")
                    append(rawArguments[1])
                    append(", ")
                }

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
                        variables[comparisonName] = NumberValue(comparisonN)
                    }

                    append('$')
                    append(comparisonName)
                    append(", ")
                } else {
                    append(comparisonN)
                    append(", ")
                }

                var valueName = game?.getNameOfGameParameterValue(rawArguments[1], (rawArguments[3] shl 8) or rawArguments[4])
                        ?.toLowerCase()
                        ?.replace(' ', '_')
                        ?.replace(LinTranspiler.ILLEGAL_VARIABLE_NAME_CHARACTER_REGEX, "")

                if (valueName != null) {
                    val valueVariable =
                            if (parameterName != null) "${parameterName}_${valueName}"
                            else "game_parameter_value_${rawArguments[1]}_${valueName}"

                    if (rawArguments[3] > 0) { //Unlikely, but used as a safeguard
                        val upperVariable = "${valueVariable}_upper"
                        val lowerVariable = "${valueVariable}_lower"

                        if (upperVariable !in variables)
                            variables[upperVariable] = NumberValue(rawArguments[3])
                        if (lowerVariable !in variables)
                            variables[lowerVariable] = NumberValue(rawArguments[4])

                        append('$')
                        append(upperVariable)
                        append(", $")
                        append(lowerVariable)
                    } else {
                        if (valueVariable !in variables)
                            variables[valueVariable] = NumberValue(rawArguments[4])

                        append("0, $")
                        append(valueVariable)
                    }
                } else {
                    append(rawArguments[3])
                    append(", ")
                    append(rawArguments[4])
                }

                var logicalName: String?
                var logicalN: Int

                for (i in 0 until (rawArguments.size - 5) / 6) {
                    logicalN = rawArguments[(i * 6) + 5]
                    logicalName = when (logicalN) {
                        6 -> "logical_or"
                        7 -> "logical_and"
                        else -> null
                    }
                    append(", ")

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

                    parameterName = game?.getNameOfGameParameter(rawArguments[(i * 6) + 7])
                            ?.toLowerCase()
                            ?.replace(' ', '_')
                            ?.replace(LinTranspiler.ILLEGAL_VARIABLE_NAME_CHARACTER_REGEX, "")

                    if (parameterName != null) {
                        val parameterVariable = "game_parameter_$parameterName"
                        if (parameterVariable !in variables)
                            variables[parameterVariable] = NumberValue(rawArguments[(i * 6) + 7])

                        append(rawArguments[(i * 6) + 6])
                        append(", $")
                        append(parameterVariable)
                        append(", ")
                    } else {
                        append(rawArguments[(i * 6) + 6])
                        append(", ")
                        append(rawArguments[(i * 6) + 7])
                        append(", ")
                    }

                    comparisonN = rawArguments[(i * 6) + 8]
                    comparisonName = when (comparisonN) {
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
                            variables[comparisonName] = NumberValue(comparisonN)
                        }

                        append('$')
                        append(comparisonName)
                        append(", ")
                    } else {
                        append(comparisonN)
                        append(", ")
                    }

                    valueName = game?.getNameOfGameParameterValue(rawArguments[(i * 6) + 7], (rawArguments[(i * 6) + 9] shl 8) or rawArguments[(i * 6) + 10])
                            ?.toLowerCase()
                            ?.replace(' ', '_')
                            ?.replace(LinTranspiler.ILLEGAL_VARIABLE_NAME_CHARACTER_REGEX, "")

                    if (valueName != null) {
                        val valueVariable =
                                if (parameterName != null) "${parameterName}_${valueName}"
                                else "game_parameter_value_${rawArguments[(i * 6) + 7]}_${valueName}"

                        if (rawArguments[(i * 6) + 9] > 0) { //Unlikely, but used as a safeguard
                            val upperVariable = "${valueVariable}_upper"
                            val lowerVariable = "${valueVariable}_lower"

                            if (upperVariable !in variables)
                                variables[upperVariable] = NumberValue(rawArguments[(i * 6) + 9])
                            if (lowerVariable !in variables)
                                variables[lowerVariable] = NumberValue(rawArguments[(i * 6) + 10])

                            append('$')
                            append(upperVariable)
                            append(", $")
                            append(lowerVariable)
                        } else {
                            if (valueVariable !in variables)
                                variables[valueVariable] = NumberValue(rawArguments[(i * 6) + 10])

                            append("0, $")
                            append(valueVariable)
                        }
                    } else {
                        append(rawArguments[(i * 6) + 9])
                        append(", ")
                        append(rawArguments[(i * 6) + 10])
                    }
                }
            }
}