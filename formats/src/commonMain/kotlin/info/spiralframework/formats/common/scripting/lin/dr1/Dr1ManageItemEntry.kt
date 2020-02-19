package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.formats.common.scripting.osl.RawNumberValue

inline class Dr1ManageItemEntry(override val rawArguments: IntArray): MutableLinEntry {
    companion object {
        const val OPERATOR_SET = 0x00
        const val OPERATOR_PLUS = 0x01
        const val OPERATOR_MINUS = 0x02
        const val OPERATOR_TIMES = 0x03
        const val OPERATOR_DIVIDE = 0x04
    }

    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    constructor(arg1: Int, arg2: Int): this(intArrayOf(arg1, arg2))

    override val opcode: Int
        get() = 0x0D

    var itemID: Int
        get() = get(0)
        set(value) = set(0, value)

    var operation: Int
        get() = get(1)
        set(value) = set(1, value)

    var quantity: Int
        get() = get(2)
        set(value) = set(2, value)

    override fun LinTranspiler.transpileArguments(builder: StringBuilder) {
        with(builder) {
            val itemName = game?.linItemNames
                    ?.getOrNull(itemID)
                    ?.toLowerCase()
                    ?.replace(' ', '_')
                    ?.replace(LinTranspiler.ILLEGAL_VARIABLE_NAME_CHARACTER_REGEX, "")

            if (itemName != null) {
                val itemVariable = "item_$itemName"
                if (itemVariable !in variables)
                    variables[itemVariable] = RawNumberValue(itemID)

                append('$')
                append(itemVariable)
                append(", ")
            } else {
                append(itemID)
                append(", ")
            }

            val operationVariable = when (operation) {
                OPERATOR_SET -> "item_operator_set"
                OPERATOR_PLUS -> "item_operator_plus"
                OPERATOR_MINUS -> "item_operator_minus"
                OPERATOR_TIMES -> "item_operator_times"
                OPERATOR_DIVIDE -> "item_operator_divide"
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

            append(quantity)
        }
    }
}