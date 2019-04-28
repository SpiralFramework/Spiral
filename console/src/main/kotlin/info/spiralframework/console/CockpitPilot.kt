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
                val localScope = with { operationScope }
                print(localScope.scopePrint)
                val matchingCommands = bus.postback(CommandRequest(readLine() ?: break, localScope)).foundCommands

                if (matchingCommands.isEmpty())
                    println(SpiralLocale.localise("commands.unknown"))
            }
        }
    }

    init {
        println(SpiralLocale.localise("gurren.pilot.init", SpiralCoreData.version
                ?: SpiralLocale.localise("gurren.default_version")))

        registerCommandClass(GurrenPilot(parameterParser))
        registerCommandClass(GurrenPluginPilot(parameterParser))

        if (SpiralSignatures.PUBLIC_KEY == null && SpiralSignatures.GITHUB_PUBLIC_KEY == null) {
            LOGGER.warn("gurren.pilot.plugin_load.missing_public")
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
                            LOGGER.debug("gurren.pilot.plugin_load.missing_signature", entry.pojo.name, entry.pojo.version ?: entry.pojo.semanticVersion)
                            return@filter false
                        }

                        return@filter entry.source?.openStream()?.verify(signature, SpiralSignatures.PUBLIC_KEY
                                ?: SpiralSignatures.GITHUB_PUBLIC_KEY ?: return@filter false) == true
                    }

            plugins.forEach { plugin ->
                LOGGER.info("gurren.pilot.plugin_load.loading", plugin.pojo.name, plugin.pojo.version ?: plugin.pojo.semanticVersion)
            }
        }
    }
}