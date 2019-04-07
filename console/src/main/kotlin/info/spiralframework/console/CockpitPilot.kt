package info.spiralframework.console

import info.spiralframework.base.SpiralLocale
import info.spiralframework.console.commands.pilot.GurrenPilot
import info.spiralframework.console.data.GurrenArgs
import info.spiralframework.console.eventbus.CommandRequest
import info.spiralframework.core.SpiralCoreData
import info.spiralframework.core.plugins.PluginRegistry
import info.spiralframework.core.postback
import kotlinx.coroutines.*

class CockpitPilot internal constructor(args: GurrenArgs): Cockpit<CockpitPilot>(args) {
    override fun startAsync(scope: CoroutineScope): Job {
        return scope.launch {
            while (isActive && GurrenPilot.keepLooping.get()) {
                delay(50)
                print(operationScope.scopePrint)
                val matchingCommands = bus.postback(CommandRequest(readLine() ?: break)).foundCommands

                if (matchingCommands.isEmpty())
                    println(SpiralLocale.localise("commands.unknown"))
            }
        }
    }

    init {
        println(SpiralLocale.localise("gurren.pilot.init", SpiralCoreData.version ?: SpiralLocale.localise("gurren.default_version")))

        registerCommandClass(GurrenPilot(this))

        val enabledPlugins = SpiralCoreData.enabledPlugins
        val plugins = PluginRegistry.discover()
                .filter { entry -> enabledPlugins[entry.pojo.uid] == entry.pojo.semanticVersion }

        //TODO: Check signatures
        plugins.map(PluginRegistry::loadPlugin)
    }
}