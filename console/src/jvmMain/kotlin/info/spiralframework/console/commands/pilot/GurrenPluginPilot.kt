package info.spiralframework.console.commands.pilot

import info.spiralframework.base.binding.readConfirmation
import info.spiralframework.base.util.printLocale
import info.spiralframework.base.util.printlnLocale
import info.spiralframework.console.CommandBuilders
import info.spiralframework.console.data.ParameterParser
import info.spiralframework.console.eventbus.CommandClass
import info.spiralframework.console.eventbus.ParboiledCommand
import info.spiralframework.console.eventbus.ParboiledCommand.Companion.SUCCESS
import info.spiralframework.console.eventbus.ParboiledCommand.Companion.fail
import info.spiralframework.core.core
import info.spiralframework.osl.parserAction

class GurrenPluginPilot(override val parameterParser: ParameterParser) : CommandClass {
    val builders = CommandBuilders(parameterParser)

    val enablePluginRule = makeRule {
        Sequence(
                Localised("commands.pilot.enable_plugin.enable_plugin"),
                parserAction { pushMarkerSuccessBase() },
                Optional(
                        InlineWhitespace(),
                        MechanicParameter(),
                        parserAction { pushMarkerSuccessCommand() }
                )
        )
    }

    val disablePluginRule = makeRule {
        Sequence(
                Localised("commands.pilot.disable_plugin.disable_plugin"),
                parserAction { pushMarkerSuccessBase() },
                Optional(
                        InlineWhitespace(),
                        MechanicParameter(),
                        parserAction { pushMarkerSuccessCommand() }
                )
        )
    }

    val enablePlugin = ParboiledCommand(enablePluginRule) { stack ->
        val core = core() ?: return@ParboiledCommand fail("spiral.context.required_core_context", this)
        with(core) {
            val pluginName = stack[0] as String

            val plugins = discover()
            val plugin = plugins
                    .firstOrNull { entry -> entry.pojo.name.equals(pluginName, true) || entry.pojo.uid.equals(pluginName, true) }
                    ?: return@ParboiledCommand fail("commands.pilot.enable_plugin.err_no_plugin_by_name", pluginName)

            val pojo = plugin.pojo

            printlnLocale("commands.pilot.enable_plugin.details", pojo.name, pojo.uid, pojo.version
                    ?: pojo.semanticVersion, pojo.description ?: "(empty)", pojo.authors?.joinToString()
                    ?: "It came from Space!", pojo.supportedModules?.joinToString()
                    ?: "N/a", pojo.requiredModules?.joinToString() ?: "N/a", pojo.contentWarnings ?: "N/a")
            printLocale("commands.pilot.enable_plugin.prompt", pojo.name)

            if (readConfirmation()) {
                if (queryEnablePlugin(plugin)) {
                    val loadResponse = loadPlugin(plugin)
                    if (loadResponse.success) {
                        printlnLocale("commands.pilot.enable_plugin.successful", pluginName)
                    } else {
                        printlnLocale("commands.pilot.enable_plugin.unsuccessful", pluginName, loadResponse)
                    }
                } else {
                    printlnLocale("commands.pilot.enable_plugin.query_failed", pluginName)
                }
            }

            return@ParboiledCommand SUCCESS
        }
    }

    val disablePlugin = ParboiledCommand(disablePluginRule) { stack ->
        val core = core() ?: return@ParboiledCommand fail("spiral.context.required_core_context", this)
        with(core) {
            val pluginName = stack[0] as String

            val plugin = loadedPlugins()
                    .firstOrNull { plugin -> plugin.name.equals(pluginName, true) || plugin.uid.equals(pluginName, true) }
                    ?: return@ParboiledCommand fail("commands.pilot.disable_plugin.err_no_plugin_by_name", pluginName)

            printlnLocale("commands.pilot.disable_plugin.details", plugin.name, plugin.uid, plugin.version)
            printLocale("commands.pilot.disable_plugin.prompt", plugin.name)

            if (readConfirmation()) {
                unloadPlugin(plugin)
                printlnLocale("commands.pilot.disable_plugin.successful", pluginName)
            }

            return@ParboiledCommand SUCCESS
        }
    }
}