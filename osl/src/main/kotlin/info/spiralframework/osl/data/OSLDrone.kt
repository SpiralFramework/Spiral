package info.spiralframework.osl.data

import info.spiralframework.osl.GameContext
import info.spiralframework.osl.OpenSpiralLanguageParser
import info.spiralframework.osl.SpiralDrillBit
import info.spiralframework.osl.data.nonstopDebate.OSLVariable
import info.spiralframework.osl.results.*
import org.parboiled.support.ValueStack
import java.util.*
import kotlin.reflect.full.cast

class OSLDrone(parser: OpenSpiralLanguageParser, valueStack: ValueStack<Any>, name: String? = null) {
    val compiled: Map<String, OSLCompilation<*>>

    init {
        var compiling: OSLCompilation<*>? = null

        val compiled: MutableMap<String, OSLCompilation<*>> = HashMap()

        for (value in valueStack.reversed()) {
            if (value is List<*>) {
                val drillBit = (value[0] as? SpiralDrillBit) ?: continue
                val head = drillBit.head
                try {
                    val params = value.subList(1, value.size).filterNotNull().toTypedArray()

                    val products = head.operate(parser, params)

                    compiling?.handle(drillBit, head.klass.cast(products), head.klass)

                    when (head.klass) {
                        GameContext::class -> {
                            val context = products as info.spiralframework.osl.GameContext
                            when (context) {
                                is GameContext.NonstopDebateDataContext -> compiling = CustomNonstopDataOSL(context.game)
                                is GameContext.NonstopDebateMinigameContext -> compiling = CustomNonstopMinigameOSL(context.game)
                                is GameContext.HopesPeakGameContext -> compiling = CustomLinOSL()
                                is GameContext.V3GameContext -> compiling = CustomWordScriptOSL()
                                is GameContext.STXGameContext -> compiling = CustomSTXOSL()
                            }
                        }
                        OSLVariable::class -> {
                            val (key, keyVal) = products as OSLVariable<*>

                            when (key) {
                                OSLVariable.KEYS.COMPILE_AS -> {
                                    if (compiling != null)
                                        compiled[keyVal.toString()] = compiling

                                    compiling = null
                                }
                            }
                        }
                    }
                } catch (th: Throwable) {
                    throw IllegalArgumentException("Script line [${drillBit.script}] threw an error", th)
                }
            }
        }

        if (compiling != null) {
            compiled[name?.substringBeforeLast('.') ?: UUID.randomUUID().toString()] = compiling
        }

        this.compiled = compiled
    }
}