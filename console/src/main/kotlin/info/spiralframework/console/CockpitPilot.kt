package info.spiralframework.console

import info.spiralframework.base.SpiralLocale
import info.spiralframework.base.util.verify
import info.spiralframework.console.commands.pilot.GurrenPilot
import info.spiralframework.console.commands.pilot.GurrenPluginPilot
import info.spiralframework.console.data.GurrenArgs
import info.spiralframework.console.eventbus.CommandRequest
import info.spiralframework.core.SpiralCoreData
import info.spiralframework.core.SpiralSignatures
import info.spiralframework.core.plugins.PluginRegistry
import info.spiralframework.core.postback
import kotlinx.coroutines.*

class CockpitPilot internal constructor(args: GurrenArgs) : Cockpit<CockpitPilot>(args) {
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
        println(SpiralLocale.localise("gurren.pilot.init", SpiralCoreData.version
                ?: SpiralLocale.localise("gurren.default_version")))

        registerCommandClass(GurrenPilot(this))
        registerCommandClass(GurrenPluginPilot(this))

        if (SpiralSignatures.PUBLIC_KEY == null && SpiralSignatures.GITHUB_PUBLIC_KEY == null) {
            LOGGER.warn("No public key could be found; plugins were not automatically loaded")
        } else {

            val enabledPlugins = SpiralCoreData.enabledPlugins
            val plugins = PluginRegistry.discover()
                    .filter { entry -> enabledPlugins[entry.pojo.uid] == entry.pojo.semanticVersion }
                    .filter { entry ->
                        if (entry.source == null)
                            return@filter true

                        val signature = SpiralSignatures.signatureForPlugin(entry.pojo.uid, entry.pojo.semanticVersion.toString(), entry.pojo.pluginFileName
                                ?: entry.source!!.path.substringAfterLast('/'))
                        if (signature == null) {
                            LOGGER.debug("No signature data could be found for {0} {1}, aborting auto-load", entry.pojo.name, entry.pojo.semanticVersion)
                            return@filter false
                        }

                        return@filter entry.source?.openStream()?.verify(signature, SpiralSignatures.PUBLIC_KEY
                                ?: SpiralSignatures.GITHUB_PUBLIC_KEY ?: return@filter false) == true
                    }

            plugins.map(PluginRegistry::loadPlugin)
        }
    }
}