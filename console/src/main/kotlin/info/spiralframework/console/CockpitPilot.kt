package info.spiralframework.console

import info.spiralframework.base.SpiralLocale
import info.spiralframework.console.commands.Gurren
import info.spiralframework.console.data.GurrenArgs
import info.spiralframework.core.SpiralCoreData
import kotlinx.coroutines.*
import org.abimon.imperator.impl.InstanceOrder

class CockpitPilot internal constructor(args: GurrenArgs): Cockpit(args) {
    override fun startAsync(scope: CoroutineScope): Job {
        runBlocking {
            while (true) {
                delay(50)
                print(operationScope.scopePrint)
                val matchingSoldiers = imperator.dispatch(InstanceOrder("STDIN", scout = null, data = readLine() ?: break))

                if (matchingSoldiers.isEmpty())
                    println(SpiralLocale.localise("commands.unknown"))
            }
        }

        return scope.launch { }
    }

    init {
        println(SpiralLocale.localise("gurren.pilot.init", SpiralCoreData.version ?: "Developer"))

        imperator.hireSoldiers(Gurren(this))
    }
}