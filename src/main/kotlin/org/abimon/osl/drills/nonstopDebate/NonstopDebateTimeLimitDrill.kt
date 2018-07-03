package org.abimon.osl.drills.nonstopDebate

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.data.nonstopDebate.OSLVariable
import org.abimon.osl.drills.DrillHead
import org.parboiled.Rule
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

object NonstopDebateTimeLimitDrill : DrillHead<OSLVariable<*>> {
    val cmd = "NONSTOP-TIME-LIMIT"

    override val klass: KClass<OSLVariable<*>> = OSLVariable::class
    override fun OpenSpiralLanguageParser.syntax(): Rule =
            Sequence(
                    clearTmpStack(cmd),

                    Sequence(
                            "Time Limit",
                            pushDrillHead(cmd, this@NonstopDebateTimeLimitDrill),
                            Separator(),
                            Duration(TimeUnit.SECONDS),
                            pushTmpFromStack(cmd)
                    ),

                    pushStackWithHead(cmd)
            )

    override fun operate(parser: OpenSpiralLanguageParser, rawParams: Array<Any>): OSLVariable<Long> {
        return OSLVariable(OSLVariable.KEYS.NONSTOP_TIMELIMIT, rawParams[0].toString().toLongOrNull() ?: 300)
    }
}