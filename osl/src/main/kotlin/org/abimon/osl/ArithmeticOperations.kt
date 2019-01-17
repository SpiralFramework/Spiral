package org.abimon.osl

import org.abimon.spiral.core.objects.scripting.lin.*

object ArithmeticOperations {
    val MIN = 0
    val MAX = 255

    var OVERFLOW = false

    fun operate(parser: OpenSpiralLanguageParser, variable: Int, amount: Int, operation: (Int, Int) -> Int): Array<LinScript> {
        val operations = ArrayList<LinScript>(MAX - MIN * 8)
        val endLabel = parser.findLabel()

        for (i in MIN until MAX) {
            val ifTrue = parser.findLabel()
            val ifFalse = parser.findLabel()

            operations.add(UnknownEntry(0x36, intArrayOf(0, variable, 1, 0, i)))
            operations.add(EndFlagCheckEntry())
            operations.add(GoToLabelEntry(ifTrue))
            operations.add(GoToLabelEntry(ifFalse))
            operations.add(SetLabelEntry(ifTrue))
            operations.add(UnknownEntry(0x33, intArrayOf(variable, 0, 0, operation(i, amount).coerceAtMost(MAX).coerceAtLeast(MIN))))
            operations.add(GoToLabelEntry(endLabel))
            operations.add(SetLabelEntry(ifFalse))
        }

        val ifTrue = parser.findLabel()

        operations.add(UnknownEntry(0x36, intArrayOf(0, variable, 1, 0, MAX)))
        operations.add(EndFlagCheckEntry())
        operations.add(GoToLabelEntry(ifTrue))
        operations.add(GoToLabelEntry(endLabel))
        operations.add(SetLabelEntry(ifTrue))
        operations.add(UnknownEntry(0x33, intArrayOf(variable, 0, 0, operation(MAX, amount).coerceAtMost(MAX).coerceAtLeast(MIN))))
        operations.add(GoToLabelEntry(endLabel))
        operations.add(SetLabelEntry(endLabel))

        return operations.toTypedArray()
    }
}