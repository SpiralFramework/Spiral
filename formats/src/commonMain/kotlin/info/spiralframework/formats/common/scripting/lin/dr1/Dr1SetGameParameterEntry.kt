package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.lin.MutableLinEntry
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.formats.common.scripting.osl.NumberValue

inline class Dr1SetGameParameterEntry(override val rawArguments: IntArray) : MutableLinEntry {
    companion object {
        const val OPERATOR_SET = 0x00
        const val OPERATOR_PLUS = 0x01
        const val OPERATOR_MINUS = 0x02
        const val OPERATOR_TIMES = 0x03
        const val OPERATOR_DIVIDE = 0x04

        const val GAME_PARAMETER_WAIT_FORCE = 6
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

    var valueUpper: Int
        get() = get(2)
        set(value) = set(2, value)

    var valueLower: Int
        get() = get(3)
        set(value) = set(3, value)

    override fun LinTranspiler.transpile(indent: Int) {
        addOutput {
            repeat(indent) { append('\t') }
            if (variable == GAME_PARAMETER_WAIT_FORCE) {
                append("Wait(")
                append(value)
                append(")")
            } else {
                append(nameFor(this@Dr1SetGameParameterEntry))
                append('|')
                transpileArguments(this)
            }
        }
    }

    @ExperimentalUnsignedTypes
    override fun LinTranspiler.transpileArguments(builder: StringBuilder) {
        with(builder) {
            val parameterName = game?.getNameOfGameParameter(variable)
                    ?.toLowerCase()
                    ?.replace(' ', '_')
                    ?.replace(LinTranspiler.ILLEGAL_VARIABLE_NAME_CHARACTER_REGEX, "")

            if (parameterName != null) {
                val parameterVariable = "game_parameter_$parameterName"
                if (parameterVariable !in variables)
                    variables[parameterVariable] = NumberValue(variable)

                append('$')
                append(parameterVariable)
                append(", ")
            } else {
                append(variable)
                append(", ")
            }

            val operationVariable = when (operation) {
                OPERATOR_SET -> "game_operator_set"
                OPERATOR_PLUS -> "game_operator_plus"
                OPERATOR_MINUS -> "game_operator_minus"
                OPERATOR_TIMES -> "game_operator_times"
                OPERATOR_DIVIDE -> "game_operator_divide"
                else -> null
            }

            @Suppress("DuplicatedCode")
            if (operationVariable != null) {
                if (operationVariable !in variables)
                    variables[operationVariable] = NumberValue(operation)

                append('$')
                append(operationVariable)
                append(", ")
            } else {
                append(operation)
                append(", ")
            }

            val valueName = game?.getNameOfGameParameterValue(variable, value)
                    ?.toLowerCase()
                    ?.replace(' ', '_')
                    ?.replace(LinTranspiler.ILLEGAL_VARIABLE_NAME_CHARACTER_REGEX, "")

            if (valueName != null) {
                val valueVariable =
                        if (parameterName != null) "${parameterName}_${valueName}"
                        else "game_parameter_value_${variable}_${valueName}"

                if (value > 0xFF) { //Unlikely, but used as a safeguard
                    val upperVariable = "${valueVariable}_upper"
                    val lowerVariable = "${valueVariable}_lower"

                    if (upperVariable !in variables)
                        variables[upperVariable] = NumberValue(valueUpper)
                    if (lowerVariable !in variables)
                        variables[lowerVariable] = NumberValue(valueLower)

                    append('$')
                    append(upperVariable)
                    append(", $")
                    append(lowerVariable)
                } else {
                    if (valueVariable !in variables)
                        variables[valueVariable] = NumberValue(value)

                    append("0, $")
                    append(valueVariable)
                }
            } else {
                append(valueUpper)
                append(", ")
                append(valueLower)
            }
        }
    }
}