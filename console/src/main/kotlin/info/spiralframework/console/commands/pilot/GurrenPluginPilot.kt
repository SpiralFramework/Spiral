package info.spiralframework.console.commands.pilot

import info.spiralframework.console.Cockpit
import info.spiralframework.console.CommandBuilders
import info.spiralframework.console.eventbus.CommandClass
import info.spiralframework.core.plugins.PluginRegistry
import info.spiralframework.osl.parserAction

class GurrenPluginPilot(override val cockpit: Cockpit<*>) : CommandClass {
    val builders = CommandBuilders(cockpit)

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

    val enablePlugin = ParboiledCommand(enablePluginRule) { stack ->
        val pluginName = stack[0] as String

        val plugins = PluginRegistry.discover()
        val plugin = plugins
                .firstOrNull { entry -> entry.pojo.name.equals(pluginName, true) || entry.pojo.uid.equals(pluginName, true) }
                ?: return@ParboiledCommand info.spiralframework.console.eventbus.ParboiledCommand.fail("commands.pilot.enable_plugin.err_no_plugin_by_name", pluginName)

        if (PluginRegistry.queryEnablePlugin(plugin)) {
            val loadResponse = PluginRegistry.loadPlugin(plugin)
            println("Response: $loadResponse")
        }

        return@ParboiledCommand info.spiralframework.console.eventbus.ParboiledCommand.SUCCESS
    }
}