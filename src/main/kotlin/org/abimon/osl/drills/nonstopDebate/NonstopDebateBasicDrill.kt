package org.abimon.osl.drills.nonstopDebate

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.data.nonstopDebate.NonstopDebateVariable
import org.abimon.osl.drills.DrillHead
import org.parboiled.Rule
import kotlin.reflect.KClass

object NonstopDebateBasicDrill : DrillHead<NonstopDebateVariable> {
    val cmd = "NONSTOP-BASIC"

    override val klass: KClass<NonstopDebateVariable> = NonstopDebateVariable::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),
                    Sequence(
                            "0x",
                            OneOrMore(Digit(16)),
                            pushDrillHead(cmd, this@NonstopDebateBasicDrill),
                            pushTmpAction(cmd),
                            '|',
                            RuleWithVariables(OneOrMore(Digit())),
                            pushTmpFromStack(cmd)
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): NonstopDebateVariable = formScript(rawParams)

    fun formScript(rawParams: Array<Any>): NonstopDebateVariable {
        val opCode = rawParams[0].toString().toInt(16)
        val value = rawParams[1].toString().toInt()
    
        return NonstopDebateVariable(opCode, value)
    }
}