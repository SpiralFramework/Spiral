package info.spiralframework.osl.drills.lin

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.DrillHead
import info.spiralframework.formats.game.hpa.DR1
import info.spiralframework.formats.game.hpa.DR2
import info.spiralframework.formats.scripting.lin.AnimationEntry
import info.spiralframework.formats.scripting.lin.LinScript
import org.parboiled.Action
import org.parboiled.Rule
import org.parboiled.support.Var
import kotlin.reflect.KClass

object LinDisplayCutinDrill : DrillHead<LinScript> {
    override val klass: KClass<LinScript> = LinScript::class
    val cmd = "LIN-TRIAL-CAMERA"

    override fun OpenSpiralLanguageParser.syntax(): Rule {
        val hide = Var<Boolean>(false)

        return Sequence(
                clearTmpStack(cmd),

                Sequence(
                        FirstOf(
                                Sequence(
                                        "Display",
                                        Action<Any> { hide.set(false) }
                                ),
                                Sequence(
                                        "Hide",
                                        Action<Any> { hide.set(true) }
                                )
                        ),
                        InlineWhitespace(),
                        "cut",
                        OptionalWhitespace(),
                        "in",
                        pushDrillHead(cmd, this@LinDisplayCutinDrill),
                        InlineWhitespace(),
                        Optional("with", InlineWhitespace()),
                        Optional("ID", InlineWhitespace()),
                        CutinID(),
                        pushTmpFromStack(cmd),
                        Action<Any> { pushTmp(cmd, hide.get()); true }
                ),

                pushStackWithHead(cmd)
        )
    }

        override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinScript {
            val id = 3000 + (rawParams[0].toString().toIntOrNull() ?: 0)
            val hide = rawParams[1].toString().toBoolean()

            return when (parser.hopesPeakGame) {
                DR1 -> AnimationEntry(id, 0, 0, 0, 0, 0, if (hide) 2 else 1)
                DR2 -> AnimationEntry(id, 0, 0, 0, 0, 0, if (hide) 2 else 1)
                else -> TODO("Display cutin's are not documented in ${parser.hopesPeakGame}")
            }
        }
    }
