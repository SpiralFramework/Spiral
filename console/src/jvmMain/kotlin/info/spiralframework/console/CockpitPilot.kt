package info.spiralframework.console

import info.spiralframework.base.util.printlnLocale
import info.spiralframework.base.util.verify
import info.spiralframework.console.commands.pilot.GurrenPilot
import info.spiralframework.console.commands.pilot.GurrenPluginPilot
import info.spiralframework.console.data.SpiralCockpitContext
import info.spiralframework.console.eventbus.CommandRequest
import info.spiralframework.core.common.SPIRAL_ENV_BUILD_KEY
import kotlinx.coroutines.delay

class CockpitPilot internal constructor(startingContext: SpiralCockpitContext) : Cockpit(startingContext) {
    override suspend fun start() {
        with(context) {
            println(localise("gurren.pilot.init", retrieveStaticValue(SPIRAL_ENV_BUILD_KEY)
                    ?: localise("gurren.default_version")))

            registerCommandClass(GurrenPilot(parameterParser))
            registerCommandClass(GurrenPluginPilot(parameterParser))

            if (publicKey == null) {
                warn("gurren.pilot.plugin_load.missing_public")
            } else {
                val enabledPlugins = enabledPlugins
                val plugins = discover()
                        .filter { entry -> enabledPlugins[entry.pojo.uid] == entry.pojo.semanticVersion }
                        .filter { entry ->
                            if (entry.source == null)
                                return@filter true

                            val signature = signatureForPlugin(entry.pojo.uid, entry.pojo.semanticVersion.toString(), entry.pojo.pluginFileName
                                    ?: entry.source!!.path.substringAfterLast('/'))
                            if (signature == null) {
                                debug("gurren.pilot.plugin_load.missing_signature", entry.pojo.name, entry.pojo.version
                                        ?: entry.pojo.semanticVersion)
                                return@filter false
                            }

                            return@filter entry.source
                                    ?.openStream()
                                    ?.verify(signature, publicKey ?: return@filter false) == true
                        }

                plugins.forEach { plugin ->
                    info("gurren.pilot.plugin_load.loading", plugin.pojo.name, plugin.pojo.version
                            ?: plugin.pojo.semanticVersion)
                    loadPlugin(plugin)
                }
            }

            while (GurrenPilot.keepLooping.get()) {
                delay(50)
                val localScope = with { operationScope }
                print(localScope.scopePrint)
                val matchingCommands = post(CommandRequest(readLine() ?: break, localScope)).foundCommands

                if (matchingCommands.isEmpty())
                    printlnLocale("commands.unknown")
            }
        }
    }
}