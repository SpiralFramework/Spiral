package org.abimon.osl.drills.circuits

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.data.nonstopDebate.OSLVariable
import org.abimon.osl.drills.DrillHead
import org.parboiled.Rule
import kotlin.reflect.KClass

object CompileAsDrill: DrillHead<OSLVariable<*>> {
    val cmd = "COMPILE-AS"

    override val klass: KClass<OSLVariable<*>> = OSLVariable::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            FirstOf("Compile As", "File Name", "Set File Name", "File Name Is "),
                            pushDrillHead(cmd, this@CompileAsDrill),
                            Separator(),
                            ParameterToStack(),
                            pushTmpFromStack(cmd)
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): OSLVariable<String> {
        return OSLVariable(OSLVariable.KEYS.COMPILE_AS, rawParams[0].toString())
    }
}