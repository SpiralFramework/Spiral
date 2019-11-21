package info.spiralframework.osl

import info.spiralframework.formats.game.hpa.DR1
import info.spiralframework.formats.game.hpa.DR2
import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.scripting.lin.UnknownEntry

enum class EnumArithmetic(private val perform: (Int, Int) -> Int, vararg val names: String) {
    PLUS(Int::plus, "plus", "+", "add", "+="),
    MINUS(Int::minus, "minus", "-", "subtract", "-="),
    DIVIDE(Int::div, "divide", "/", "÷", "/="),
    MULTIPLY(Int::times, "times", "*", "multiply", "*="),
    REM(Int::rem, "remainder", "rem", "%", "modulo", "%="),
    AND(Int::and, "&", "and"),
    OR(Int::or, "|", "or"),
    XOR(Int::xor, "^", "xor");

    companion object {
        val NAMES: Array<String> by lazy { values().flatMap{ enum -> enum.names.toList() }.toTypedArray() }
    }

    operator fun invoke(parser: info.spiralframework.osl.OpenSpiralLanguageParser, variable: Int, amount: Int): Array<LinEntry> {
        return when (this) {
            info.spiralframework.osl.EnumArithmetic.PLUS -> when (parser.hopesPeakGame) {
                DR1 -> arrayOf(UnknownEntry(0x33, intArrayOf(variable, 1, (amount shr 8) and 0xFF, amount and 0xFF)) as LinEntry)
                DR2 -> arrayOf(UnknownEntry(0x3A, intArrayOf(variable, 1, (amount shr 8) and 0xFF, amount and 0xFF)) as LinEntry)
                else -> info.spiralframework.osl.ArithmeticOperations.operate(parser, variable, amount, perform)
            }
            info.spiralframework.osl.EnumArithmetic.MINUS -> when (parser.hopesPeakGame) {
                DR1 -> arrayOf(UnknownEntry(0x33, intArrayOf(variable, 2, (amount shr 8) and 0xFF, amount and 0xFF)) as LinEntry)
                DR2 -> arrayOf(UnknownEntry(0x3A, intArrayOf(variable, 2, (amount shr 8) and 0xFF, amount and 0xFF)) as LinEntry)
                else -> info.spiralframework.osl.ArithmeticOperations.operate(parser, variable, amount, perform)
            }
            info.spiralframework.osl.EnumArithmetic.MULTIPLY -> when (parser.hopesPeakGame) {
                DR1 -> arrayOf(UnknownEntry(0x33, intArrayOf(variable, 3, (amount shr 8) and 0xFF, amount and 0xFF)) as LinEntry)
                DR2 -> arrayOf(UnknownEntry(0x3A, intArrayOf(variable, 3, (amount shr 8) and 0xFF, amount and 0xFF)) as LinEntry)
                else -> info.spiralframework.osl.ArithmeticOperations.operate(parser, variable, amount, perform)
            }
            info.spiralframework.osl.EnumArithmetic.DIVIDE -> when (parser.hopesPeakGame) {
                DR1 -> arrayOf(UnknownEntry(0x33, intArrayOf(variable, 4, (amount shr 8) and 0xFF, amount and 0xFF)) as LinEntry)
                DR2 -> arrayOf(UnknownEntry(0x3A, intArrayOf(variable, 4, (amount shr 8) and 0xFF, amount and 0xFF)) as LinEntry)
                else -> info.spiralframework.osl.ArithmeticOperations.operate(parser, variable, amount, perform)
            }
            else -> info.spiralframework.osl.ArithmeticOperations.operate(parser, variable, amount, perform)
        }
    }
}
