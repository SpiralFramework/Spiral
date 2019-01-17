package info.spiralframework.osl.drills.nonstopDebateData

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.SpiralDrillBit
import info.spiralframework.osl.data.nonstopDebate.NonstopDebateNewObject
import info.spiralframework.osl.drills.StaticDrill
import info.spiralframework.osl.drills.circuits.DrillCircuit
import info.spiralframework.formats.game.hpa.UnknownHopesPeakGame
import org.parboiled.Action
import org.parboiled.Rule

object NonstopDebateNewObjectDrill: DrillCircuit {
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    FirstOf(
                            "New Nonstop Section",
                            "[New Section]",
                            "[New Nonstop Debate Section]"
                    ),

                    Action<Any> {
                        push(listOf(SpiralDrillBit(StaticDrill(NonstopDebateNewObject((hopesPeakKillingGame ?: UnknownHopesPeakGame).nonstopDebateSectionSize / 2)))))
                    }
            )
}
