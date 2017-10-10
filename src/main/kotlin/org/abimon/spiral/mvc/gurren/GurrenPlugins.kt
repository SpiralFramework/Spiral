package org.abimon.spiral.mvc.gurren

import org.abimon.spiral.modding.PluginManager
import org.abimon.spiral.mvc.SpiralModel
import org.abimon.spiral.mvc.SpiralModel.Command
import org.abimon.visi.io.errPrintln

@Suppress("unused")
object GurrenPlugins {
    val scannedPlugins = Command("scanned_plugins") {
        println("Loaded Plugins: ${PluginManager.pluginsInFolder.values.joinToString { (_, config, signed) ->
            "\n\t* ${config.name} v${config.version} ${if(signed) "[SIGNED]" else "[UNSIGNED]"}"
        } }")
    }

    val rescanPlugins = Command("rescan_plugins") {
        PluginManager.scanForPlugins()
    }

    val enablePlugin = Command("enable_plugin") { (params) ->
        if(params.size == 1)
            return@Command errPrintln("Error: No plugin to enable")

        val uid = params[1]

        if(PluginManager.pluginsInFolder.containsKey(uid)) {
            if(PluginManager.loadPlugin(uid))
                println("Loaded $uid")
            else
                errPrintln("Error: Could not enable $uid")
        } else
            return@Command errPrintln("Error: No plugin with UID $uid")
    }

    val disablePlugin = Command("disable_plugin") { (params) ->
        if(params.size == 1)
            return@Command errPrintln("Error: No plugin to disable")

        val uid = params[1]

        if(PluginManager.loadedPlugins.containsKey(uid)) {
            PluginManager.loadedPlugins[uid]!!.third.disable(SpiralModel.imperator)
            println("Disabled $uid")
        } else
            return@Command errPrintln("Error: No loaded plugin with UID $uid")
    }
}