package org.abimon.osl.drills.lin

import org.abimon.osl.EnumArithmetic
import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.parboiled.Rule
import kotlin.reflect.KClass

object LinArithmeticGameState: DrillHead<Array<LinScript>> {
    override val klass: KClass<Array<LinScript>> = Array<LinScript>::class
    val cmd = "LIN-ARITHMETIC"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            '[',
                            pushDrillHead(cmd, this@LinArithmeticGameState),
                            OptionalInlineWhitespace(),
                            GameState(),
                            pushTmpFromStack(cmd),
                            OptionalInlineWhitespace(),
                            FirstOf(EnumArithmetic.NAMES),
                            pushTmpAction(cmd),
                            OptionalInlineWhitespace(),
                            RuleWithVariables(OneOrMore(Digit())),
                            OptionalInlineWhitespace(),
                            ']',
                            pushTmpFromStack(cmd),
                            pushTmpFromStack(cmd)
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): Array<LinScript> {
        val variable = rawParams[0].toString().toInt()

        val operationName = rawParams[1].toString()
        val operation = EnumArithmetic.values().first { arithmetic -> operationName in arithmetic.names }

        val amount = rawParams[2].toString().toInt()

        return operation(parser, variable, amount)
    }
}