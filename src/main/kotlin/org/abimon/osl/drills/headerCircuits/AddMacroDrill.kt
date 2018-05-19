package org.abimon.osl.drills.headerCircuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.drills.circuits.DrillCircuit
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
                            Whitespace(),
                            pushDrillHead(cmd, this@AddMacroDrill),
                            Parameter(cmd),
                            Whitespace(),
                            FirstOf("as", "to"),
                            Whitespace(),
                            Parameter(cmd),
                            ZeroOrMore(
                                    Whitespace(),
                                    FirstOf("or", "and"),
                                    Whitespace(),
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