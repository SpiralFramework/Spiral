package org.abimon.osl

import org.abimon.spiral.core.objects.scripting.lin.LinScript

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

    operator fun invoke(parser: OpenSpiralLanguageParser, variable: Int, amount: Int): Array<LinScript> = ArithmeticOperations.operate(parser, variable, amount, perform)
}