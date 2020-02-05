package info.spiralframework.osl

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.lin.UnknownLinEntry
import info.spiralframework.formats.common.scripting.lin.dr1.Dr1EndFlagCheckEntry
import info.spiralframework.formats.common.scripting.lin.dr1.Dr1GoToLabelEntry
import info.spiralframework.formats.common.scripting.lin.dr1.Dr1MarkLabelEntry

object ArithmeticOperations {
    val MIN = 0
    val MAX = 255

    var OVERFLOW = false

    //TODO: Make this work on other games
    fun operate(parser: OpenSpiralLanguageParser, variable: Int, amount: Int, operation: (Int, Int) -> Int): Array<LinEntry> {
        val operations = ArrayList<LinEntry>(MAX - MIN * 8)
        val endLabel = parser.findLabel()

        for (i in MIN until MAX) {
            val ifTrue = parser.findLabel()
            val ifFalse = parser.findLabel()

            operations.add(UnknownLinEntry(0x36, intArrayOf(0, variable, 1, 0, i)))
            operations.add(Dr1EndFlagCheckEntry())
            operations.add(Dr1GoToLabelEntry(ifTrue))
            operations.add(Dr1GoToLabelEntry(ifFalse))
            operations.add(Dr1MarkLabelEntry(ifTrue))
            operations.add(UnknownLinEntry(0x33, intArrayOf(variable, 0, 0, operation(i, amount).coerceAtMost(MAX).coerceAtLeast(MIN))))
            operations.add(Dr1GoToLabelEntry(endLabel))
            operations.add(Dr1MarkLabelEntry(ifFalse))
        }

        val ifTrue = parser.findLabel()

        operations.add(UnknownLinEntry(0x36, intArrayOf(0, variable, 1, 0, MAX)))
        operations.add(Dr1EndFlagCheckEntry())
        operations.add(Dr1GoToLabelEntry(ifTrue))
        operations.add(Dr1GoToLabelEntry(endLabel))
        operations.add(Dr1MarkLabelEntry(ifTrue))
        operations.add(UnknownLinEntry(0x33, intArrayOf(variable, 0, 0, operation(MAX, amount).coerceAtMost(MAX).coerceAtLeast(MIN))))
        operations.add(Dr1GoToLabelEntry(endLabel))
        operations.add(Dr1MarkLabelEntry(endLabel))

        return operations.toTypedArray()
    }
}
