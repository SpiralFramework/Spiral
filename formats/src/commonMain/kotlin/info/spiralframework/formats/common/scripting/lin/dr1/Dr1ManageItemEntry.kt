package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.formats.common.scripting.osl.RawNumberValue

public class Dr1ManageItemEntry(override val rawArguments: IntArray): MutableLinEntry {
    public companion object {
        public const val OPERATOR_SET: Int = 0x00
        public const val OPERATOR_PLUS: Int = 0x01
        public const val OPERATOR_MINUS: Int = 0x02
        public const val OPERATOR_TIMES: Int = 0x03
        public const val OPERATOR_DIVIDE: Int = 0x04
    }

    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    public constructor(arg1: Int, arg2: Int): this(intArrayOf(arg1, arg2))

    override val opcode: Int
        get() = 0x0D

    public var itemID: Int
        get() = get(0)
        set(value) = set(0, value)

    public var operation: Int
        get() = get(1)
        set(value) = set(1, value)

    public var quantity: Int
        get() = get(2)
        set(value) = set(2, value)

    override fun LinTranspiler.transpileArguments(builder: StringBuilder) {
        with(builder) {
            val itemName = game?.linItemNames
                ?.getOrNull(itemID)
                ?.lowercase()
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