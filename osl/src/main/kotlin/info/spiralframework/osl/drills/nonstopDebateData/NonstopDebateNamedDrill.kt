package info.spiralframework.osl.drills.nonstopDebateData

import info.spiralframework.osl.LineCodeMatcher
import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.data.nonstopDebate.NonstopDebateVariable
import info.spiralframework.osl.drills.DrillHead
import info.spiralframework.formats.game.hpa.UnknownHopesPeakGame
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

object NonstopDebateNamedDrill : DrillHead<NonstopDebateVariable> {
    val cmd = "NONSTOP-NAMED"

    override val klass: KClass<NonstopDebateVariable> = NonstopDebateVariable::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            OneOrMore(LineCodeMatcher),
                            Action<Any> {
                                val name = match()
                                (hopesPeakKillingGame ?: UnknownHopesPeakGame).nonstopDebateOpCodeNames.values.any { opCodeName -> opCodeName == name }
                            },
                            pushDrillHead(cmd, this@NonstopDebateNamedDrill),
                            pushTmpAction(cmd),
                            '|',
                            RuleWithVariables(OneOrMore(Digit())),
                            pushTmpFromStack(cmd)
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): NonstopDebateVariable {
        val opName = rawParams[0].toString()
        rawParams[0] = (parser.hopesPeakKillingGame ?: UnknownHopesPeakGame)
                .nonstopDebateOpCodeNames.entries.first { (_, name) -> name == opName }
                .key.toString(16)
        return NonstopDebateBasicDrill.formScript(rawParams)
    }
}
