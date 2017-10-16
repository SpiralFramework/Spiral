package org.abimon.spiral.mvc.gurren

import com.jakewharton.fliptables.FlipTable
import org.abimon.spiral.core.data.EnumSignedStatus
import org.abimon.spiral.modding.PluginManager
import org.abimon.spiral.mvc.SpiralModel
import org.abimon.spiral.mvc.SpiralModel.Command
import org.abimon.visi.io.errPrintln
import org.abimon.visi.io.question
import java.text.DecimalFormat

@Suppress("unused")
object GurrenPlugins {
    val TWO_DECIMAL_PLACES = DecimalFormat("0.0#")

    val scannedPlugins = Command("scanned_plugins") {
        println("Loaded Plugins: ${PluginManager.pluginsInFolder.values.joinToString { (_, config, signed) ->
            "\n\t* ${config.name} v${config.version} $signed"
        } }")
    }

    val rescanPlugins = Command("rescan_plugins") {
        PluginManager.scanForPlugins()
    }

    val enablePlugin = Command("enable_plugin") { (params) ->
        if(params.size == 1)
            return@Command errPrintln("Error: No plugin to enable")

        val uid = PluginManager.uidForName(params[1]) ?: params[1]

        if(PluginManager.pluginsInFolder.containsKey(uid)) {
            val (file, config, signed) = PluginManager.pluginsInFolder[uid]!!

            when(signed) {
                EnumSignedStatus.UNSIGNED -> {
                    println()
                    println("**WARNING**")
                    println("${config.name} v${config.version} (${file.name}) is an **unsigned** plugin.")
                    println("This means that it hasn't been officially verified, and may therefore be malicious")
                    println("Only continue to enable this plugin if you trust the plugin developer and the download source.")
                    println()

                    if(!question("Enable ${config.name} (Y/n)? ", "Y"))
                        return@Command
                }
                EnumSignedStatus.INVALID_SIGNATURE -> {
                    println()
                    println("**ERROR**")
                    println("${config.name} v${config.version} (${file.name}) has an **invalid** signature.")
                    println("This means that, while the plugin and version are officially verified, the file you have downloaded does not match the provided signature")
                    println("This should only happen if the plugin author has misconfigured their plugin, or if the file you have downloaded is not the plugin it claims to be")
                    println("This plugin therefore cannot be enabled. Please contact the plugin author to report this error.")
                    println()

                    return@Command
                }
                EnumSignedStatus.NO_PUBLIC_KEY -> {
                    println()
                    println("**ERROR**")
                    println("SPIRAL could not find it's public key from the mod repository.")
                    println("Verifying signatures is therefore impossible, and absolute caution should be taken.")
                    println("Please report this to a SPIRAL developer as soon as possible, and only proceed to enable this plugin if you trust the plugin developer and the download source")
                    println()

                    if(!question("Enable ${config.name} (Y/n)? ", "Y"))
                        return@Command
                }
                EnumSignedStatus.SIGNED -> {}
            }

            if(PluginManager.loadPlugin(uid))
                println("Loaded ${params[1]}")
            else
                errPrintln("Error: Could not enable ${params[1]}")
        } else
            return@Command errPrintln("Error: No plugin with UID $uid / name ${params[1]}")
    }

    val disablePlugin = Command("disable_plugin") { (params) ->
        if(params.size == 1)
            return@Command errPrintln("Error: No plugin to disable")

        val uid = PluginManager.uidForName(params[1]) ?: params[1]

        if(PluginManager.loadedPlugins.containsKey(uid)) {
            PluginManager.loadedPlugins[uid]!!.third.disable(SpiralModel.imperator)
            println("Disabled $uid")
        } else
            return@Command errPrintln("Error: No loaded plugin with UID $uid")
    }

    val searchPlugins = Command("search_plugins") { (params) ->
        val query = if(params.size == 1) "" else params[1]
        val searchResults = PluginManager.apiSearch(query)

        println(FlipTable.of(arrayOf("Name", "Latest Version", "Author", "Short Desc"), searchResults.map { (_, _, version, name, short_desc) -> arrayOf(name, version, "", short_desc ?: "") }.toTypedArray()))
    }

    val downloadPlugin = Command("download_plugin") { (params) ->
        if(params.size == 1)
            return@Command errPrintln("Error: No plugin name provided")

        val name = params[1]
        val (uid, _, latestVersion, modName, shortDesc) = PluginManager.apiSearch(name).firstOrNull() ?: return@Command errPrintln("Error: No plugin found for name $name")

        val version = if(params.size > 2) params[2] else latestVersion

        val size = PluginManager.pluginSize(uid, version) ?: return@Command errPrintln("Error: No plugin found for name $name and version $version")
        println("$modName v$version ($size B / ${TWO_DECIMAL_PLACES.format(size / 1000.0 / 1000.0)} MB)")
        println(shortDesc ?: "No desc provided")
        println()

        if(question("Do you wish to continue downloading this plugin (Y/n)? ", "Y")) {
            val success = PluginManager.downloadPlugin(uid, version) { readBytes, totalBytes ->
                println("Downloaded ${TWO_DECIMAL_PLACES.format(readBytes * 100.0 / totalBytes.toDouble())}%")
            }

            if(success)
                println("Successfully downloaded $modName v$version")
            else
                errPrintln("Error: Was unable to download $modName v$version")
        }
    }
}