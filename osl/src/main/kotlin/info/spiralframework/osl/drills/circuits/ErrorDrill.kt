package info.spiralframework.osl.drills.circuits

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.SpiralDrillException
import org.parboiled.Rule

object ErrorDrill : DrillCircuit {
    val cmd = "ERROR"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    Sequence(
                            FirstOf("error", "throw", "throw error", "throw exception"),
                            InlineWhitespace(),
                            Parameter(cmd)
                    ),

                    operateOnTmpActionsWithContext(cmd) { context, (error) ->
                        if (silence)
                            return@operateOnTmpActionsWithContext

                        throw SpiralDrillException(error.toString())
                    }
            )
}
