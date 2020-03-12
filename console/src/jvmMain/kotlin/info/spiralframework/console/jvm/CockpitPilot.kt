package info.spiralframework.console.jvm

import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.base.jvm.crypto.verify
import info.spiralframework.console.jvm.commands.pilot.GurrenPilot
import info.spiralframework.console.jvm.data.SpiralCockpitContext
import info.spiralframework.console.jvm.pipeline.PipelineContext
import info.spiralframework.console.jvm.pipeline.parsePipeline
import info.spiralframework.console.jvm.pipeline.run
import info.spiralframework.core.common.SPIRAL_ENV_BUILD_KEY
import kotlinx.coroutines.delay

@ExperimentalUnsignedTypes
class CockpitPilot internal constructor(startingContext: SpiralCockpitContext) : Cockpit(startingContext) {
    override suspend fun start() {
        with(context) {
            println(localise("gurren.pilot.init", retrieveStaticValue(SPIRAL_ENV_BUILD_KEY)
                    ?: localise("gurren.default_version")))

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

            val globalContext = PipelineContext(null)

            GurrenPilot.register(context, globalContext)

            while (GurrenPilot.keepLooping.get()) {
                delay(50)
                val localScope = with { operationScope }
                print(localScope.scopePrint)

                val input = readLine() ?: break
                val pipeline = runCatching { parsePipeline(input) }
                if (pipeline.isFailure) {
                    printlnLocale("commands.unknown")
                } else {
                    val scope = pipeline.getOrThrow()
                    scope.run(context, globalContext)
                }

//                val matchingCommands = post(CommandRequest(readLine() ?: break, localScope)).foundCommands
//
//                if (matchingCommands.isEmpty())
//                    printlnLocale("commands.unknown")
            }
        }
    }
}