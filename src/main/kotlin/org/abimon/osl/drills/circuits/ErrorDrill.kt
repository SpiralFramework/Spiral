package org.abimon.osl.drills.circuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.SpiralDrillException
import org.parboiled.Rule

object ErrorDrill : DrillCircuit {
    val cmd = "ERROR"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    Sequence(
                            FirstOf("error", "throw", "throw error", "throw exception"),
                            Whitespace(),
                            Parameter(cmd)
                    ),

                    operateOnTmpActionsWithContext(cmd) { context, (error) ->
                        if (silence)
                            return@operateOnTmpActionsWithContext

                        throw SpiralDrillException(error.toString())
                    }
            )
}