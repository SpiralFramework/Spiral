package info.spiralframework.console

import info.spiralframework.base.SpiralLocale
import info.spiralframework.console.commands.Gurren
import info.spiralframework.console.data.GurrenArgs
import info.spiralframework.core.SpiralCoreData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.abimon.imperator.impl.InstanceOrder

class CockpitPilot internal constructor(args: GurrenArgs): Cockpit<CockpitPilot>(args) {
    override fun startAsync(scope: CoroutineScope): Job {
        return scope.launch {
            while (true) {
                delay(50)
                print(operationScope.scopePrint)
                val matchingSoldiers = imperator.dispatch(InstanceOrder("STDIN", scout = null, data = readLine() ?: break))

                if (matchingSoldiers.isEmpty())
                    println(SpiralLocale.localise("commands.unknown"))
            }
        }
    }

    init {
        println(SpiralLocale.localise("gurren.pilot.init", SpiralCoreData.version ?: SpiralLocale.localise("gurren.default_version")))

        imperator.hireSoldiers(Gurren(this))
    }
}