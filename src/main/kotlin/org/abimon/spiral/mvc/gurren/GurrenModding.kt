package org.abimon.spiral.mvc.gurren

import com.jakewharton.fliptables.FlipTable
import org.abimon.spiral.core.archives.IArchive
import org.abimon.spiral.core.data.EnumSignedStatus
import org.abimon.spiral.modding.ModManager
import org.abimon.spiral.modding.PluginManager
import org.abimon.spiral.modding.data.ModConfig
import org.abimon.spiral.mvc.SpiralModel
import org.abimon.spiral.mvc.SpiralModel.Command
import org.abimon.visi.collections.joinToPrefixedString
import org.abimon.visi.io.errPrintln
import org.abimon.visi.io.question

@Suppress("unused")
object GurrenModding {
    val operatingArchive: IArchive
        get() = IArchive(SpiralModel.operating ?: throw IllegalStateException("Attempt to get the archive while operating is null, this is a bug!")) ?: throw IllegalStateException("Attempts to create an archive return null, this is a bug!")
    val operatingName: String
        get() = SpiralModel.operating?.nameWithoutExtension ?: ""

    val IArchive.enabledMods: Set<ModConfig>
        get() = HashSet<ModConfig>().apply {
            addAll(installedMods.mods.values)
            addAll(ModManager.newEnabledMods.mapNotNull { uid -> ModManager.modsInFolder[uid]?.second })
        }

    val prepareV3 = SpiralModel.Command("prepare_v3", "default") { (params) ->

    }

    val scannedMods = Command("scanned_mods") {
        println("Loaded Mods: ${ModManager.modsInFolder.values.joinToString { (_, config, signed) ->
            "\n\t* ${config.name} v${config.version} $signed"
        } }")
    }

    val rescanMods = Command("rescan_mods") {
        ModManager.scanForMods()
    }

    val enabledMods = Command("enabled_mods", "mod") {
        println("Enabled Mods: ${operatingArchive.enabledMods.joinToString { config -> "\n\t* ${config.name} v${config.version}" }}")
    }

    val installedMods = Command("installed_mods", "mod") {
        val modlist = operatingArchive.installedMods

        println("Installed Mods: ${modlist.mods.values.joinToString { config -> "\n\t* ${config.name} v${config.version}" }}")
    }

    val enableMod = Command("enable_mod", "mod") { (params) ->
        if(params.size == 1)
            return@Command errPrintln("Error: No mod to enable")

        val uid = ModManager.uidForName(params[1]) ?: params[1]

        if(ModManager.modsInFolder.containsKey(uid)) {
            val (file, config, signed) = ModManager.modsInFolder[uid]!!

            when(signed) {
                EnumSignedStatus.UNSIGNED -> {
                    println()
                    println("**WARNING**")
                    println("${config.name} v${config.version} (${file.name}) is an **unsigned** mod.")
                    println("This means that it hasn't been officially verified, and may therefore contain content that is different from what it claims.")
                    println("While mods are not normally capable of performing malicious actions, the content contains within may not be desired.")
                    println()

                    if(!question("Enable ${config.name} (Y/n)? ", "Y"))
                        return@Command
                }
                EnumSignedStatus.INVALID_SIGNATURE -> {
                    println()
                    println("**ERROR**")
                    println("${config.name} v${config.version} (${file.name}) has an **invalid** signature.")
                    println("This means that, while the mod and version are officially verified, the file you have downloaded does not match the provided signature")
                    println("This should only happen if the mod maker has misconfigured their mod, or if the file you have downloaded is not the mod it claims to be")
                    println("Please contact the mod maker to report this error.")
                    println("In the mean time, you may choose to enable the mod, being aware the contents of the mod may not be as they say they are.")
                    println()

                    if(!question("Enable ${config.name} (Y/n)? ", "Y"))
                        return@Command

                    return@Command
                }
                EnumSignedStatus.NO_PUBLIC_KEY -> {
                    println()
                    println("**ERROR**")
                    println("SPIRAL could not find it's public key from the mod repository.")
                    println("Verifying signatures is therefore impossible, and absolute caution should be taken.")
                    println("Please report this to a SPIRAL developer as soon as possible, and only proceed with enabling this mod if you accept that this mod may not contain what it says it does.")
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
            return@Command errPrintln("Error: No mod with UID $uid / name ${params[1]}")
    }

    val disablePlugin = Command("disable_mod") { (params) ->
        if(params.size == 1)
            return@Command errPrintln("Error: No mod to disable")

        val uid = ModManager.uidForName(params[1]) ?: params[1]
        val mod = operatingArchive.enabledMods.firstOrNull { modConfig -> modConfig.uid == uid }

        if(mod != null) {
            //ModManager.loadedPlugins[uid]!!.third.disable(SpiralModel.imperator)
            //Do something here
            println("Disabled $uid")
        } else
            return@Command errPrintln("Error: No loaded plugin with UID $uid")
    }
    val searchMods = Command("search_mods") { (params) ->
        val query = if(params.size == 1) "" else params[1]
        val searchResults = ModManager.apiSearch(query)

        println(FlipTable.of(arrayOf("Name", "Latest Version", "Author", "Short Desc"), searchResults.map { (_, _, version, name, short_desc) -> arrayOf(name, version, "", short_desc ?: "") }.toTypedArray()))
    }

    val downloadMod = Command("download_mod") { (params) ->
        if(params.size == 1)
            return@Command errPrintln("Error: No mod name provided")

        val name = params[1]
        val (uid, _, latestVersion, modName, shortDesc) = ModManager.apiSearch(name).firstOrNull() ?: return@Command errPrintln("Error: No mod found for name $name")

        val version = if(params.size > 2) params[2] else latestVersion

        val size = ModManager.modSize(uid, version) ?: return@Command errPrintln("Error: $modName has no version $version")
        println("$modName v$version ($size B / ${GurrenPlugins.TWO_DECIMAL_PLACES.format(size / 1000.0 / 1000.0)} MB)")
        println(shortDesc ?: "No desc provided")
        println()

        if(question("Do you wish to continue downloading this mod (Y/n)? ", "Y")) {
            val success = ModManager.downloadMod(uid, version) { readBytes, totalBytes ->
                println("Downloaded ${GurrenPlugins.TWO_DECIMAL_PLACES.format(readBytes * 100.0 / totalBytes.toDouble())}%")
            }

            if(success)
                println("Successfully downloaded $modName v$version")
            else
                errPrintln("Error: Was unable to download $modName v$version")
        }
    }

    val installMods = Command("install_mods") {

    }

    val modArchive = Command("mod", "default") { (params) ->
        if (SpiralModel.archives.isEmpty())
            return@Command errPrintln("Error: No archives registered")
        if (params.size > 1) {
            for (i in 1 until params.size) {
                val archiveName = params[i]
                val archive = SpiralModel.archives.firstOrNull { file -> file.nameWithoutExtension == archiveName || file.absolutePath == archiveName }
                if (archive == null)
                    println("Invalid archive $archiveName")
                else {
                    SpiralModel.operating = archive
                    SpiralModel.scope = "[Modding ${archive.nameWithoutExtension}]|> " to "mod"
                    println("Now modding ${archive.nameWithoutExtension}")

                    return@Command
                }
            }
        }

        println("Select an archive to mod")
        println(SpiralModel.archives.joinToPrefixedString("\n", "\t") { "$nameWithoutExtension ($absolutePath)" })
        while (true) {
            print("[mod] > ")
            val archiveName = readLine() ?: break
            if (archiveName == "exit")
                break

            val archive = SpiralModel.archives.firstOrNull { file -> file.nameWithoutExtension == archiveName || file.absolutePath == archiveName }
            if (archive == null)
                println("Invalid archive $archiveName")
            else {
                SpiralModel.operating = archive
                SpiralModel.scope = "[Modding ${archive.nameWithoutExtension}]|> " to "mod"
                println("Now modding ${archive.nameWithoutExtension}")

                break
            }
        }
    }

    val exit = Command("exit", "mod") { SpiralModel.scope = "> " to "default" }
}