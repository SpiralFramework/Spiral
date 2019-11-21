package info.spiralframework.osl.drills.lin

import info.spiralframework.osl.LineCodeMatcher
import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.DrillHead
import info.spiralframework.formats.game.hpa.UnknownHopesPeakGame
import info.spiralframework.formats.common.scripting.lin.LinEntry
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

object NamedLinSpiralDrill : DrillHead<LinEntry> {
    val cmd = "NAMED-LIN"

    override val klass: KClass<LinEntry> = LinEntry::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            OneOrMore(LineCodeMatcher),
                            Action<Any> {
                                val name = match()
                                (hopesPeakGame ?: UnknownHopesPeakGame).opCodes.values.any { (names) -> name in names }
                            },
                            pushDrillHead(cmd, this@NamedLinSpiralDrill),
                            pushTmpAction(cmd),
                            Optional(
                                    '|'
                            ),
                            Optional(
                                    ParamList(
                                            cmd,
                                            RuleWithVariables(OneOrMore(Digit())),
                                            Sequence(
                                                    ',',
                                                    OptionalInlineWhitespace()
                                            )
                                    )
                            )
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinEntry {
        val opName = rawParams[0].toString()
        rawParams[0] = (parser.hopesPeakGame
                ?: UnknownHopesPeakGame).opCodes.entries.first { (_, triple) -> opName in triple.first }.key.toString(16)
        return BasicLinSpiralDrill.formScript(rawParams, parser.hopesPeakGame ?: UnknownHopesPeakGame)
    }
}
