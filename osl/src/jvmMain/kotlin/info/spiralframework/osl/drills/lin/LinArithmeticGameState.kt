package info.spiralframework.osl.drills.lin

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.DrillHead
import info.spiralframework.formats.common.scripting.lin.LinEntry
import org.parboiled.Rule
import kotlin.reflect.KClass

object LinArithmeticGameState: DrillHead<Array<LinEntry>> {
    override val klass: KClass<Array<LinEntry>> = Array<LinEntry>::class
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
                            FirstOf(info.spiralframework.osl.EnumArithmetic.NAMES),
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

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): Array<LinEntry> {
        val variable = rawParams[0].toString().toInt()

        val operationName = rawParams[1].toString()
        val operation = info.spiralframework.osl.EnumArithmetic.values().first { arithmetic -> operationName in arithmetic.names }

        val amount = rawParams[2].toString().toInt()

        return operation(parser, variable, amount)
    }
}
