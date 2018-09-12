package org.abimon.osl.drills.headerCircuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.circuits.DrillCircuit
import org.parboiled.Action
import org.parboiled.Rule

object WaitDrill: DrillCircuit {
    val ALPHABET = "abcdefghijklmnopqrstuvwxyz".toCharArray()
    
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    "Wait for ",
                    FrameCount(),
                    Action<Any> { context -> push(arrayOf(this, "for (${context.level.toAlphaString(ALPHABET.size)} in 0 until ${pop()}) { \n Wait Frame| \n }"))}
            )

    fun Int.toAlphaString(radixO: Int): String {
        var i = this
        var radix = radixO
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            radix = 10

        val buf = CharArray(33)
        val negative = i < 0
        var charPos = 32

        if (!negative) {
            i = -i
        }

        while (i <= -radix) {
            buf[charPos--] = ALPHABET[-(i % radix)]
            i /= radix
        }
        buf[charPos] = ALPHABET[-i]

        if (negative) {
            buf[--charPos] = '-'
        }

        return String(buf, charPos, 33 - charPos)
    }
}