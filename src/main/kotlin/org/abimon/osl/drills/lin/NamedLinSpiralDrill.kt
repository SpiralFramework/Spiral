package org.abimon.osl.drills.lin

import org.abimon.osl.LineCodeMatcher
import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.spiral.core.objects.game.hpa.UnknownHopesPeakGame
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

object NamedLinSpiralDrill : DrillHead<LinScript> {
    val cmd = "NAMED-LIN"

    override val klass: KClass<LinScript> = LinScript::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            OneOrMore(LineCodeMatcher),
                            Action<Any> {
                                val name = match()
                                (game as? HopesPeakDRGame
                                        ?: UnknownHopesPeakGame).opCodes.values.any { (names) -> name in names }
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

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinScript {
        val opName = rawParams[0].toString()
        rawParams[0] = (parser.game as? HopesPeakDRGame
                ?: UnknownHopesPeakGame).opCodes.entries.first { (_, triple) -> opName in triple.first }.key.toString(16)
        return BasicLinSpiralDrill.formScript(rawParams, parser.game as? HopesPeakDRGame ?: UnknownHopesPeakGame)
    }
}