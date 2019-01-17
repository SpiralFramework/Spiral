package info.spiralframework.osl

import info.spiralframework.formats.scripting.lin.*

object ArithmeticOperations {
    val MIN = 0
    val MAX = 255

    var OVERFLOW = false

    fun operate(parser: info.spiralframework.osl.OpenSpiralLanguageParser, variable: Int, amount: Int, operation: (Int, Int) -> Int): Array<LinScript> {
        val operations = ArrayList<LinScript>(info.spiralframework.osl.ArithmeticOperations.MAX - info.spiralframework.osl.ArithmeticOperations.MIN * 8)
        val endLabel = parser.findLabel()

        for (i in info.spiralframework.osl.ArithmeticOperations.MIN until info.spiralframework.osl.ArithmeticOperations.MAX) {
            val ifTrue = parser.findLabel()
            val ifFalse = parser.findLabel()

            operations.add(UnknownEntry(0x36, intArrayOf(0, variable, 1, 0, i)))
            operations.add(EndFlagCheckEntry())
            operations.add(GoToLabelEntry(ifTrue))
            operations.add(GoToLabelEntry(ifFalse))
            operations.add(SetLabelEntry(ifTrue))
            operations.add(UnknownEntry(0x33, intArrayOf(variable, 0, 0, operation(i, amount).coerceAtMost(info.spiralframework.osl.ArithmeticOperations.MAX).coerceAtLeast(info.spiralframework.osl.ArithmeticOperations.MIN))))
            operations.add(GoToLabelEntry(endLabel))
            operations.add(SetLabelEntry(ifFalse))
        }

        val ifTrue = parser.findLabel()

        operations.add(UnknownEntry(0x36, intArrayOf(0, variable, 1, 0, info.spiralframework.osl.ArithmeticOperations.MAX)))
        operations.add(EndFlagCheckEntry())
        operations.add(GoToLabelEntry(ifTrue))
        operations.add(GoToLabelEntry(endLabel))
        operations.add(SetLabelEntry(ifTrue))
        operations.add(UnknownEntry(0x33, intArrayOf(variable, 0, 0, operation(info.spiralframework.osl.ArithmeticOperations.MAX, amount).coerceAtMost(info.spiralframework.osl.ArithmeticOperations.MAX).coerceAtLeast(info.spiralframework.osl.ArithmeticOperations.MIN))))
        operations.add(GoToLabelEntry(endLabel))
        operations.add(SetLabelEntry(endLabel))

        return operations.toTypedArray()
    }
}
