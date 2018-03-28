package org.abimon.osl.drills

import org.abimon.osl.LineCodeMatcher
import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

object NamedSpiralDrill : DrillHead<LinScript> {
    val cmd = "NAMED"

    override val klass: KClass<LinScript> = LinScript::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    OneOrMore(LineCodeMatcher),
                    Action<Any> {
                        val name = match()
                        game.opCodes.values.any { (names) -> name in names }
                    },
                    pushTmpAction(cmd, this@NamedSpiralDrill),
                    pushTmpAction(cmd),
                    Optional(
                            '|'
                    ),
                    Optional(
                            ParamList(
                                    cmd,
                                    Sequence(
                                            OneOrMore(Digit()),
                                            pushToStack(this)
                                    ),
                                    Sequence(
                                            ',',
                                            Optional(Whitespace())
                                    )
                            )
                    ),
                    pushTmpStack(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinScript {
        val opName = rawParams[0].toString()
        rawParams[0] = parser.game.opCodes.entries.first { (_, triple) -> opName in triple.first }.key.toString(16)
        return BasicSpiralDrill.formScript(rawParams, parser.game)
    }
}