package org.abimon.osl.drills.lin

import org.abimon.osl.AllButMatcher
import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.DrillHead
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.game.hpa.UnknownHopesPeakGame
import org.abimon.spiral.core.objects.scripting.lin.*
import org.parboiled.Action
import org.parboiled.Rule
import kotlin.reflect.KClass

object LinDialogueDrill : DrillHead<Array<LinScript>> {
    val NAME = AllButMatcher(charArrayOf(':', '\n'))
    val cmd = "LIN-DIALOGUE"

    override val klass: KClass<Array<LinScript>> = Array<LinScript>::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            OneOrMore(NAME),
                            Action<Any> {
                                match() in customIdentifiers || match() in (hopesPeakGame
                                        ?: UnknownHopesPeakGame).characterIdentifiers
                            },
                            pushDrillHead(cmd, this@LinDialogueDrill),
                            pushTmpAction(cmd),
                            ':',
                            OptionalInlineWhitespace(),
                            LinText(cmd)
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): Array<LinScript> {
        val game = parser.hopesPeakGame ?: UnknownHopesPeakGame
        return when(game) {
            DR1 -> arrayOf(
                    SpeakerEntry(
                            parser.customIdentifiers["${rawParams[0]}"]
                                    ?: game.characterIdentifiers["${rawParams[0]}"]
                                    ?: parser.customIdentifiers["???"]
                                    ?: game.characterIdentifiers["???"]
                                    ?: 0
                    ), //Replace with valid naming or numbers or whatever
                    TextEntry("${rawParams[1]}", -1),
                    WaitFrameEntry.DR1,
                    WaitForInputEntry.DR1
            )
            DR2 -> arrayOf(
                    SpeakerEntry(
                            parser.customIdentifiers["${rawParams[0]}"]
                                    ?: game.characterIdentifiers["${rawParams[0]}"]
                                    ?: parser.customIdentifiers["???"]
                                    ?: game.characterIdentifiers["???"]
                                    ?: 0
                    ), //Replace with valid naming or numbers or whatever
                    TextEntry("${rawParams[1]}", -1),
                    WaitFrameEntry.DR2,
                    WaitForInputEntry.DR2
            )
            else -> TODO("Dialogue is not documented for $game")
        }
    }
}