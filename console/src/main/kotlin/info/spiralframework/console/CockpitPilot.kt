package info.spiralframework.console

import info.spiralframework.base.SpiralLocale
import info.spiralframework.base.util.printlnLocale
import info.spiralframework.console.commands.pilot.GurrenPilot
import info.spiralframework.console.data.GurrenArgs
import info.spiralframework.core.SpiralCoreData
import kotlinx.coroutines.*
import org.abimon.imperator.impl.InstanceOrder

class CockpitPilot internal constructor(args: GurrenArgs): Cockpit<CockpitPilot>(args) {
    override fun startAsync(scope: CoroutineScope): Job {
        return scope.launch {
            while (isActive && GurrenPilot.keepLooping.get()) {
                delay(50)
                print(withAsync { operationScope }.scopePrint)
                val matchingSoldiers = imperator.dispatch(InstanceOrder("STDIN", scout = null, data = readLine() ?: break))

                if (matchingSoldiers.isEmpty())
                    printlnLocale("commands.unknown")
            }
        }
    }

    init {
        printlnLocale("gurren.pilot.init", SpiralCoreData.version ?: SpiralLocale.localise("gurren.default_version"))

        imperator.hireSoldiers(GurrenPilot(this))
    }
}