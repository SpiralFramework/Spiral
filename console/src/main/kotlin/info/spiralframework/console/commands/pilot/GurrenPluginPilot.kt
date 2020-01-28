package info.spiralframework.console.commands.pilot

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.isSuccessful
import info.spiralframework.base.SpiralLocale
import info.spiralframework.base.config.SpiralConfig
import info.spiralframework.base.util.*
import info.spiralframework.console.CommandBuilders
import info.spiralframework.console.data.ParameterParser
import info.spiralframework.console.eventbus.CommandClass
import info.spiralframework.console.eventbus.ParboiledCommand
import info.spiralframework.console.eventbus.ParboiledCommand.Companion.FAILURE
import info.spiralframework.console.eventbus.ParboiledCommand.Companion.SUCCESS
import info.spiralframework.console.eventbus.ParboiledCommand.Companion.fail
import info.spiralframework.core.SpiralCoreData
import info.spiralframework.core.SpiralSignatures
import info.spiralframework.core.plugins.PluginRegistry
import info.spiralframework.core.plugins.SpiralPluginDefinitionPojo
import info.spiralframework.core.plugins.SpiralPluginDownloadInfo
import info.spiralframework.core.userAgent
import info.spiralframework.osl.parserAction
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

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

    val downloadPluginRule = makeRule {
        Sequence(
                Localised("commands.pilot.download_plugin.base"),
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
                ?: return@ParboiledCommand fail("commands.pilot.enable_plugin.err_no_plugin_by_name", pluginName)

        val pojo = plugin.pojo

        printlnLocale("commands.pilot.enable_plugin.details", pojo.name, pojo.uid, pojo.version
                ?: pojo.semanticVersion, pojo.description ?: "(empty)", pojo.authors?.joinToString()
                ?: "It came from Space!", pojo.supportedModules?.joinToString()
                ?: "N/a", pojo.requiredModules?.joinToString() ?: "N/a", pojo.contentWarnings?.joinToString() ?: "N/a")
        printLocale("commands.pilot.enable_plugin.prompt", pojo.name)

        if (SpiralLocale.readConfirmation()) {
            if (PluginRegistry.queryEnablePlugin(plugin)) {
                val loadResponse = PluginRegistry.loadPlugin(plugin)
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

    val disablePlugin = ParboiledCommand(disablePluginRule) { stack ->
        val pluginName = stack[0] as String

        val plugin = PluginRegistry.loadedPlugins.firstOrNull { plugin -> plugin.name.equals(pluginName, true) || plugin.uid.equals(pluginName, true) }
                ?: return@ParboiledCommand fail("commands.pilot.disable_plugin.err_no_plugin_by_name", pluginName)

        printlnLocale("commands.pilot.disable_plugin.details", plugin.name, plugin.uid, plugin.version)
        printLocale("commands.pilot.disable_plugin.prompt", plugin.name)

        if (SpiralLocale.readConfirmation()) {
            PluginRegistry.unloadPlugin(plugin)
            printlnLocale("commands.pilot.disable_plugin.successful", pluginName)
        }

        return@ParboiledCommand SUCCESS
    }

    val downloadPlugin = ParboiledCommand(downloadPluginRule) { stack ->
        val queryString = stack[0] as String
        val target = SystemTarget.determineFromSystem().format()
        val searchResults = PluginRegistry.searchDatabase("%$queryString%")
                .mapNotNull { pojo -> PluginRegistry.getDownloadInfo(uid = pojo.uid, version = pojo.semanticVersion.toString(), target = target)?.let(pojo::to) }
        val downloading: Pair<SpiralPluginDefinitionPojo, SpiralPluginDownloadInfo>

        when (searchResults.size) {
            0 -> {
                printlnLocale("commands.pilot.download_plugin.no_results")
                return@ParboiledCommand SUCCESS
            }
            1 -> downloading = searchResults[0]
            else -> {
                printlnLocale("commands.pilot.download_plugin.multiple_results")
                println(searchResults.mapIndexed { index, (pojo) -> "${index + 1}) ${pojo.name} v${pojo.semanticVersion}" }.joinToString("\n"))
                printLocale("commands.pilot.download_plugin.multiple_results_selector")

                while (true) {
                    val input = readLine() ?: return@ParboiledCommand FAILURE

                    if (input.equals(SpiralLocale.PROMPT_EXIT, true))
                        return@ParboiledCommand SUCCESS

                    val matchingName = searchResults.firstOrNull { (pojo) -> "${pojo.name} v${pojo.semanticVersion}".equals(input, true) }
                            ?: searchResults.firstOrNull { (pojo) -> pojo.name.equals(input, true) }

                    if (matchingName != null) {
                        downloading = matchingName
                        break
                    }

                    val index = input.toIntOrNull()?.minus(1)
                    if (index != null) {
                        val result = searchResults.getOrNull(index)
                        if (result == null) {
                            printlnLocale("commands.pilot.download_plugin.bad_index", index)
                            continue
                        }

                        downloading = result
                        break
                    }
                }
            }
        }

        val (pluginPojo, downloadPojo) = downloading

        printlnLocale("commands.pilot.download_plugin.details", pluginPojo.name, pluginPojo.uid, pluginPojo.version
                ?: pluginPojo.semanticVersion, pluginPojo.description ?: "(empty)", pluginPojo.authors?.joinToString()
                ?: "It came from Space!", pluginPojo.supportedModules?.joinToString()
                ?: "N/a", pluginPojo.requiredModules?.joinToString()
                ?: "N/a", pluginPojo.contentWarnings?.joinToString() ?: "N/a")
        printLocale("commands.pilot.download_plugin.prompt", pluginPojo.name)

        if (SpiralLocale.readConfirmation(true)) {
            val signatureData = SpiralSignatures.signatureForPlugin(pluginPojo.uid, pluginPojo.semanticVersion.toString(), downloadPojo.filename)

            var shouldDownloadUnsigned = false
            if (signatureData == null) {
                printlnLocale("commands.pilot.download_plugin.unsigned.warning", SpiralCoreData.sha256Hash)
                printLocale("commands.pilot.download_plugin.unsigned.warning_confirmation")

                shouldDownloadUnsigned = SpiralLocale.readConfirmation(defaultToAffirmative = false)

                if (shouldDownloadUnsigned) {
                    printlnLocale("commands.pilot.download_plugin.unsigned.approved_download")
                } else {
                    printlnLocale("commands.pilot.download_plugin.unsigned.denied_download")
                }
            }

            if (signatureData != null || shouldDownloadUnsigned) {
                val pluginFile = SpiralConfig.getPluginFile(downloadPojo.filename)
                val downloadFile = SpiralConfig.getPluginFile(downloadPojo.filename + ".tmp")

                val (_, response) = ProgressTracker(downloadingText = "commands.pilot.download_plugin.downloading", downloadedText = "") {
                    Fuel.download(PluginRegistry.getDownloadUrl(uid = downloadPojo.uid, version = downloadPojo.version, target = downloadPojo.target))
                            .fileDestination { _, _ -> downloadFile }
                            .progress(this::trackDownload)
                            .userAgent()
                            .response()
                            .also(SpiralCoreData::printResponse)
                }

                if (response.isSuccessful) {
                    printlnLocale("commands.pilot.download_plugin.downloaded")

                    val isSigned: Boolean

                    if (signatureData != null) {
                        if (SpiralSignatures.PUBLIC_KEY == null) {
                            if (SpiralSignatures.spiralFrameworkOnline) {
                                //Online and key is down. Suspicious, but give the user a choice
                                //Since we were able to download the plugin though, if the user says no we rename and shift the plugin file

                                printlnLocale("commands.pilot.download_plugin.no_key.spiral_online.warning")
                                printLocale("commands.pilot.download_plugin.no_key.spiral_online.warning_confirmation")

                                if (SpiralLocale.readConfirmation(defaultToAffirmative = false)) {
                                    printlnLocale("commands.pilot.download_plugin.no_key.spiral_online.approved_plugin")
                                } else {
                                    printlnLocale("commands.pilot.download_plugin.no_key.spiral_online.denied_plugin")
                                    PluginRegistry.quarantinePlugin(downloadFile)
                                }
                            } else if (SpiralSignatures.githubOnline) {
                                //Github's online, and our public key is null. Suspicious, but give the user a choice
                                //Since we were able to download the plugin though, if the user says no we rename and shift the plugin file

                                printlnLocale("commands.pilot.download_plugin.no_key.github_online.warning")
                                printLocale("commands.pilot.download_plugin.no_key.github_online.warning_confirmation")

                                if (SpiralLocale.readConfirmation(defaultToAffirmative = false)) {
                                    printlnLocale("commands.pilot.download_plugin.no_key.github_online.approved_plugin")
                                } else {
                                    printlnLocale("commands.pilot.download_plugin.no_key.github_online.denied_plugin")
                                    PluginRegistry.quarantinePlugin(downloadFile)
                                }
                            } else {
                                //Both Github and I are down; unlikely, but possible.
                                //Give the user a choice, but tell them how to verify
                                //Since we were able to download the plugin though, if the user says no we rename and shift the plugin file

                                printlnLocale("commands.pilot.download_plugin.no_key.offline.warning")
                                printLocale("commands.pilot.download_plugin.no_key.offline.warning_confirmation")

                                if (SpiralLocale.readConfirmation(defaultToAffirmative = false)) {
                                    printlnLocale("commands.pilot.download_plugin.no_key.offline.approved_plugin")
                                } else {
                                    printlnLocale("commands.pilot.download_plugin.no_key.offline.denied_plugin")
                                    PluginRegistry.quarantinePlugin(downloadFile)
                                }
                            }

                            isSigned = false
                        } else {
                            isSigned = if (!FileInputStream(downloadFile).use { stream -> stream.verify(signatureData, SpiralSignatures.PUBLIC_KEY!!) }) {
                                printlnLocale("commands.pilot.download_plugin.invalid_signature.error")
                                downloadFile.delete()
                                false
                            } else {
                                true
                            }
                        }
                    } else {
                        isSigned = false
                    }

                    if (downloadFile.exists()) {
                        val unloaded = PluginRegistry.unloadAll(pluginPojo.uid)

                        try {
                            Files.move(downloadFile.toPath(), pluginFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

                            if (unloaded.isNotEmpty() && isSigned) {
                                val entry = PluginRegistry.discover().firstOrNull { entry -> entry.pojo.uid == pluginPojo.uid && entry.pojo.version == pluginPojo.version }
                                if (entry != null) {
                                    PluginRegistry.loadPlugin(entry)
                                    printlnLocale("commands.pilot.download_plugin.hotswapped")
                                }
                            }
                        } catch (io: IOException) {
                            //Uho, file is still being used. That's bad!
                            //We can't hotswap this, so we'll rename it to something helpful and handle it on boot

                            val dest = File(pluginFile.absolutePath + ".hotswap")
                            pluginFile.renameTo(dest)
                            printlnLocale("commands.pilot.download_plugin.hotswap_failed")
                        }
                    }
                } else {
                    printlnLocale("commands.pilot.download_plugin.download_failed", response.statusCode, response.responseMessage)
                    downloadFile.delete()
                }
            }
        }

        return@ParboiledCommand SUCCESS
    }
}