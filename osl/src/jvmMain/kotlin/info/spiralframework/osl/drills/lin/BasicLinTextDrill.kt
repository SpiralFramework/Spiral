package info.spiralframework.osl.drills.lin

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.DrillHead
import info.spiralframework.formats.game.hpa.DR1
import info.spiralframework.formats.game.hpa.DR2
import info.spiralframework.formats.game.hpa.UDG
import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.scripting.lin.TextEntry
import info.spiralframework.formats.scripting.lin.udg.UDGTextEntry
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

object BasicLinTextDrill : DrillHead<LinEntry> {
    val cmd = "BASIC-LIN-TEXT"
    var BLANK_LINE = buildString {
        for (i in 0 until 1024)
            append(' ')
    }

    override val klass: KClass<LinEntry> = LinEntry::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            FirstOf(
                                    Sequence(
                                            Action<Any> { hopesPeakGame.let { game -> game == DR1 || game == DR2 } },
                                            "0x",
                                            Optional("0"),
                                            "2"
                                    ),
                                    Sequence(
                                            Action<Any> { hopesPeakGame == UDG },
                                            "0x",
                                            Optional("0"),
                                            "1"
                                    ),
                                    IgnoreCase("Text")
                            ),
                            '|',
                            pushDrillHead(cmd, this@BasicLinTextDrill),
                            FirstOf(
                                    Sequence(
                                            "[Blank Line]",
                                            pushTmpAction(cmd, BLANK_LINE)
                                    ),
                                    Sequence(
                                            "[Empty]",
                                            pushTmpAction(cmd, "\u0000")
                                    ),
                                    LinText(cmd)
                            ),

                            pushTmpAction(cmd)
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): LinEntry {
        return when (parser.hopesPeakGame) {
            DR1 -> TextEntry("${rawParams[0]}", -1)
            DR2 -> TextEntry("${rawParams[0]}", -1)
            UDG -> UDGTextEntry("${rawParams[0]}", -1)
            else -> TODO("Text is not documented for ${parser.hopesPeakGame}")
        }
    }
}
