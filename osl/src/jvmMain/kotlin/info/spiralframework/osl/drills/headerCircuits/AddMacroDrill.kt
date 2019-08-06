package info.spiralframework.osl.drills.headerCircuits

import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.drills.circuits.DrillCircuit
import org.parboiled.Rule

object AddMacroDrill : DrillCircuit {
    val cmd = "ADD_MACRO"
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    Sequence(
                            FirstOf(
                                    "Add Macro",
                                    "Define Macro"
                            ),
                            InlineWhitespace(),
                            pushDrillHead(cmd, this@AddMacroDrill),
                            Parameter(cmd),
                            InlineWhitespace(),
                            FirstOf("as", "to"),
                            InlineWhitespace(),
                            Parameter(cmd),
                            ZeroOrMore(
                                    InlineWhitespace(),
                                    FirstOf("or", "and"),
                                    InlineWhitespace(),
                                    Parameter(cmd)
                            ),
                            operateOnTmpActions(cmd) { params ->
                                val macro = params[1].toString()

                                for (i in 2 until params.size)
                                    macros[params[i].toString().toLowerCase()] = macro
                            }
                    ),
                    clearTmpStack(cmd)
            )
}
