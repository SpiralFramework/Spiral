package org.abimon.osl.drills.circuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.SpiralDrillException
import org.parboiled.Rule

object ErrorDrill: DrillCircuit {
    val cmd = "ERROR"
    
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    FirstOf("error", "throw", "throw error", "throw exception"),
                    Whitespace(),
                    Parameter(cmd),
                    operateOnTmpActions(cmd) { (error) ->
                        if (silence)
                            return@operateOnTmpActions

                        throw SpiralDrillException(error.toString())
                    }
            )
}