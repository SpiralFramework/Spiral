package org.abimon.osl

import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.UnknownEntry

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

    operator fun invoke(parser: OpenSpiralLanguageParser, variable: Int, amount: Int): Array<LinScript> {
        return when (this) {
            PLUS -> when (parser.hopesPeakGame) {
                DR1 -> arrayOf(UnknownEntry(0x33, intArrayOf(variable, 1, (amount shr 8) and 0xFF, amount and 0xFF)) as LinScript)
                DR2 -> arrayOf(UnknownEntry(0x3A, intArrayOf(variable, 1, (amount shr 8) and 0xFF, amount and 0xFF)) as LinScript)
                else -> ArithmeticOperations.operate(parser, variable, amount, perform)
            }
            MINUS -> when (parser.hopesPeakGame) {
                DR1 -> arrayOf(UnknownEntry(0x33, intArrayOf(variable, 2, (amount shr 8) and 0xFF, amount and 0xFF)) as LinScript)
                DR2 -> arrayOf(UnknownEntry(0x3A, intArrayOf(variable, 2, (amount shr 8) and 0xFF, amount and 0xFF)) as LinScript)
                else -> ArithmeticOperations.operate(parser, variable, amount, perform)
            }
            MULTIPLY -> when (parser.hopesPeakGame) {
                DR1 -> arrayOf(UnknownEntry(0x33, intArrayOf(variable, 3, (amount shr 8) and 0xFF, amount and 0xFF)) as LinScript)
                DR2 -> arrayOf(UnknownEntry(0x3A, intArrayOf(variable, 3, (amount shr 8) and 0xFF, amount and 0xFF)) as LinScript)
                else -> ArithmeticOperations.operate(parser, variable, amount, perform)
            }
            DIVIDE -> when (parser.hopesPeakGame) {
                DR1 -> arrayOf(UnknownEntry(0x33, intArrayOf(variable, 4, (amount shr 8) and 0xFF, amount and 0xFF)) as LinScript)
                DR2 -> arrayOf(UnknownEntry(0x3A, intArrayOf(variable, 4, (amount shr 8) and 0xFF, amount and 0xFF)) as LinScript)
                else -> ArithmeticOperations.operate(parser, variable, amount, perform)
            }
            else -> ArithmeticOperations.operate(parser, variable, amount, perform)
        }
    }
}