package info.spiralframework.osl

import info.spiralframework.formats.scripting.lin.*

object ArithmeticOperations {
    val MIN = 0
    val MAX = 255

    var OVERFLOW = false

    //TODO: Make this work on other games
    fun operate(parser: OpenSpiralLanguageParser, variable: Int, amount: Int, operation: (Int, Int) -> Int): Array<LinScript> {
        val operations = ArrayList<LinScript>(MAX - MIN * 8)
        val endLabel = parser.findLabel()

        for (i in MIN until MAX) {
            val ifTrue = parser.findLabel()
            val ifFalse = parser.findLabel()

            operations.add(UnknownEntry(0x36, intArrayOf(0, variable, 1, 0, i)))
            operations.add(EndFlagCheckEntry())
            operations.add(GoToLabelEntry.forGame(parser.drGame, ifTrue))
            operations.add(GoToLabelEntry.forGame(parser.drGame, ifFalse))
            operations.add(SetLabelEntry.forGame(parser.drGame, ifTrue))
            operations.add(UnknownEntry(0x33, intArrayOf(variable, 0, 0, operation(i, amount).coerceAtMost(MAX).coerceAtLeast(MIN))))
            operations.add(GoToLabelEntry.forGame(parser.drGame, endLabel))
            operations.add(SetLabelEntry.forGame(parser.drGame, ifFalse))
        }

        val ifTrue = parser.findLabel()

        operations.add(UnknownEntry(0x36, intArrayOf(0, variable, 1, 0, MAX)))
        operations.add(EndFlagCheckEntry())
        operations.add(GoToLabelEntry.forGame(parser.drGame, ifTrue))
        operations.add(GoToLabelEntry.forGame(parser.drGame, endLabel))
        operations.add(SetLabelEntry.forGame(parser.drGame, ifTrue))
        operations.add(UnknownEntry(0x33, intArrayOf(variable, 0, 0, operation(MAX, amount).coerceAtMost(MAX).coerceAtLeast(MIN))))
        operations.add(GoToLabelEntry.forGame(parser.drGame, endLabel))
        operations.add(SetLabelEntry.forGame(parser.drGame, endLabel))

        return operations.toTypedArray()
    }
}
