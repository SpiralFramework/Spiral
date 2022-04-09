package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.formats.common.scripting.osl.RawNumberValue

public class Dr1SetGameParameterEntry(override val rawArguments: IntArray) : MutableLinEntry {
    public companion object {
        public const val OPERATOR_SET: Int = 0x00
        public const val OPERATOR_PLUS: Int = 0x01
        public const val OPERATOR_MINUS: Int = 0x02
        public const val OPERATOR_TIMES: Int = 0x03
        public const val OPERATOR_DIVIDE: Int = 0x04

        public const val GAME_PARAMETER_WAIT_FORCE: Int = 6
    }

    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    public constructor(variable: Int, operation: Int, value: Int) : this(intArrayOf(variable, operation, (value shr 8) and 0xFF, value and 0xFF))

    override val opcode: Int
        get() = 0x33

    public var variable: Int
        get() = get(0)
        set(value) = set(0, value)

    public var operation: Int
        get() = get(1)
        set(value) = set(1, value)

    public var value: Int
        get() = getInt16BE(2) //rawArguments[2] shl 8) or rawArguments[3]
        set(value) = setInt16BE(2, value)

    public var valueUpper: Int
        get() = get(2)
        set(value) = set(2, value)

    public var valueLower: Int
        get() = get(3)
        set(value) = set(3, value)

    override fun LinTranspiler.transpile(indent: Int) {
        addOutput {
            repeat(indent) { append('\t') }
            if (variable == GAME_PARAMETER_WAIT_FORCE) {
                //TODO: Better handle names for forced wait
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

    override fun LinTranspiler.transpileArguments(builder: StringBuilder) {
        with(builder) {
            val parameterName = game?.getNameOfLinGameParameter(variable)
                ?.lowercase()
                ?.replace(' ', '_')
                ?.replace(LinTranspiler.ILLEGAL_VARIABLE_NAME_CHARACTER_REGEX, "")

            if (parameterName != null) {
                val parameterVariable = "game_parameter_$parameterName"
                if (parameterVariable !in variables)
                    variables[parameterVariable] = RawNumberValue(variable)

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
                    variables[operationVariable] = RawNumberValue(operation)

                append('$')
                append(operationVariable)
                append(", ")
            } else {
                append(operation)
                append(", ")
            }

            val valueName = game?.getNameOfLinGameParameterValue(variable, value)
                ?.lowercase()
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
                        variables[upperVariable] = RawNumberValue(valueUpper)
                    if (lowerVariable !in variables)
                        variables[lowerVariable] = RawNumberValue(valueLower)

                    append('$')
                    append(upperVariable)
                    append(", $")
                    append(lowerVariable)
                } else {
                    if (valueVariable !in variables)
                        variables[valueVariable] = RawNumberValue(value)

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