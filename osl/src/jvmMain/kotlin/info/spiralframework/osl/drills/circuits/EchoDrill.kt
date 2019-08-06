package info.spiralframework.osl.drills.circuits

import info.spiralframework.osl.OpenSpiralLanguageParser
import org.parboiled.Rule

object EchoDrill : DrillCircuit {
    val cmd = "ECHO"

    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    Sequence(
                            "echo",
                            InlineWhitespace(),
                            pushDrillHead(cmd, this@EchoDrill),
                            Parameter(cmd)
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>) {
        if (parser.silence)
            return

        parser.stdout.println(rawParams[0])
    }
}
