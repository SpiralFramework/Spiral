package org.abimon.spiral.mvc

import com.jakewharton.fliptables.FlipTable
import org.abimon.imperator.impl.InstanceOrder
import org.abimon.imperator.impl.InstanceSoldier
import org.abimon.imperator.impl.InstanceWatchtower
import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.core.formats.*
import org.abimon.visi.collections.copyFrom
import org.abimon.visi.collections.joinToPrefixedString
import org.abimon.visi.io.FileDataSource
import org.abimon.visi.io.errPrintln
import org.abimon.visi.io.iterate
import org.abimon.visi.io.question
import org.abimon.visi.lang.EnumOS
import org.abimon.visi.lang.splitOutsideGroup
import java.io.File
import java.io.FileFilter
import java.nio.file.Files
import kotlin.system.measureTimeMillis

@Suppress("unused")
object Gurren {
    val os = EnumOS.determineOS()
    val ignoreFilters: Array<FileFilter> = arrayOf(
            FileFilter { file -> !file.name.startsWith(".") },
            FileFilter { file -> !file.name.startsWith("__") },
            FileFilter { file -> !Files.isHidden(file.toPath()) },
            FileFilter { file -> !Files.isSymbolicLink(file.toPath()) },
            FileFilter { file -> Files.isReadable(file.toPath()) }
    )
    val identifyFormats: Array<SpiralFormat> = SpiralFormats.formats.filter { it !is TXTFormat }.toTypedArray()
    val separator: String = File.separator
    var keepLooping: Boolean = true

    val helpTable: String = FlipTable.of(
            arrayOf("Command", "Arguments", "Description", "Example Command"),
            arrayOf(
                    arrayOf("help", "", "Display this message", ""),
                    arrayOf("find", "", "Try to find the WAD files for DR1/2, if they're in their normal locations", ""),
                    arrayOf("locate", "[directory]", "Search [directory] for all WAD files. Note: Will take a fair amount of time, and requires confirmation for each WAD file.", "find \"Steam${separator}steamapps${separator}common${separator}Danganronpa: Trigger Happy Havoc\""),
                    arrayOf("register", "[wad]", "Register an individual WAD file", "register \"dr1_data_us.wad\""),
                    arrayOf("registered", "", "Display the registered WAD files", ""),
                    arrayOf("formats", "", "Display the formats table", ""),
                    arrayOf("identify", "[file|directory]", "Identify the format of either the provided [file], or the files in the provided [directory]", "identify \"images\""),
                    arrayOf("exit", "", "Exits the program", "")
            )
    )

    val formatTable: String = FlipTable.of(
            arrayOf("Format", "Can Convert To"),
            arrayOf(
                    arrayOf("WAD", WADFormat.conversions.joinToString { it.name }),
                    arrayOf("PAK", PAKFormat.conversions.joinToString { it.name }),
                    arrayOf("TGA", TGAFormat.conversions.joinToString { it.name }),
                    arrayOf("SHTX", SHTXFormat.conversions.joinToString { it.name }),
                    arrayOf("PNG", PNGFormat.conversions.joinToString { it.name }),
                    arrayOf("JPG", JPEGFormat.conversions.joinToString { it.name }),
                    arrayOf("LIN", LINFormat.conversions.joinToString { it.name }),
                    arrayOf("Nonstop DAT", NonstopFormat.conversions.joinToString { it.name })
            )
    )

    val help = Command("help") { println(helpTable) }
    val find = Command("find") {
        when (os) {
            EnumOS.WINDOWS -> {
                for (root in File.listRoots()) {
                    for (programFolder in arrayOf(File(root, "Program Files (x86)"), File(root, "Program Files"))) {
                        val steamFolder = File(programFolder, "Steam")
                        if (steamFolder.exists()) {
                            val common = File(steamFolder, "steamapps${File.separator}common")
                            for (game in common.listFiles { file -> file.isDirectory && file.name.contains("Danganronpa") })
                                SpiralModel.wads.addAll(game.listFiles { file -> file.isFile && file.extension == "wad" && !file.name.contains(".backup") })
                        }
                    }
                }
            }
            EnumOS.MACOSX -> {
                val steamFolder = os.getStorageLocation("Steam")
                if (steamFolder.exists()) {
                    val common = File(steamFolder, "steamapps${File.separator}common")
                    for (game in common.listFiles { file -> file.isDirectory && file.name.contains("Danganronpa") }) {
                        val appDirs = game.listFiles { file -> file.isDirectory && file.extension == "app" }
                        if (appDirs.isNotEmpty()) {
                            SpiralModel.wads.addAll(
                                    appDirs.flatMap<File, File> { app ->
                                        File(app, "Contents${File.separator}Resources").listFiles { file ->
                                            file.isFile && file.extension == "wad" && !file.name.contains(".backup")
                                        }.toList()
                                    }
                            )
                        }
                    }
                }
            }
            else -> println("No behaviour defined for $os!")
        }

        if (SpiralModel.wads.isEmpty())
            errPrintln("Error: No WAD files detected! You can manually add them via the register command, or by running the locate command!")
        else
            println("WADs: ${SpiralModel.wads.joinToPrefixedString("", "\n\t")}")
    }
    val locate = Command("locate") { (operation) ->
        if (operation.size == 1) {
            errPrintln("Error: No directory provided!")
            return@Command
        }

        val dir = File(operation.copyFrom(1).joinToString(" "))
        if (!dir.exists()) {
            errPrintln("Error: $dir does not exist!")
            return@Command
        }

        if (question("Warning: This operation will take quite some time. Do you wish to proceed to scan $dir (Y/N)? ", "Y")) {
            val time = measureTimeMillis {
                dir.iterate(filters = arrayOf(
                        FileFilter { file -> !file.name.startsWith(".") },
                        FileFilter { file -> file.isDirectory || (file.isFile && file.extension == "wad" && !file.name.contains(".backup")) }
                )).forEach { wad ->
                    if (question("WAD Found ($wad). Would you like to add this to the internal registry (Y/N)? ", "Y"))
                        SpiralModel.wads.add(wad)
                }
            }
            println("Took $time ms.")
            if (SpiralModel.wads.isEmpty())
                errPrintln("Error: No WAD files detected! You can manually add them via the register command, or by running the locate command!")
            else
                println("WADs: ${SpiralModel.wads.joinToPrefixedString("", "\n\t")}")
        }
    }
    val register = Command("register") { (operation) ->
        if (operation.size == 1) {
            errPrintln("Error: No file provided!")
            return@Command
        }

        val wad = File(operation.copyFrom(1).joinToString(" "))
        if (!wad.exists()) {
            errPrintln("Error: $wad does not exist!")
            return@Command
        }

        if (!wad.isFile) {
            errPrintln("Error: $wad is not a file!")
            return@Command
        }

        if (wad.extension != "wad") {
            errPrintln("Error: $wad is not a .wad file!")
            return@Command
        }

        SpiralModel.wads.add(wad)
        println("Registered $wad!")
    }
    val registered = Command("registered") { println("Registered WADs: ${SpiralModel.wads.joinToPrefixedString("", "\n\t")}") }
    val formats = Command("formats") { println(formatTable) }
    val identify = Command("identify") { (params) ->
        if(params.size == 1)
            errPrintln("Error: No file or directory provided")

        val files = params.map { File(it) }

        files.forEach { file ->
            if(file.isFile) {

            } else if(file.isDirectory) {
                val rows = ArrayList<Array<String>>()
                file.iterate(filters = ignoreFilters).forEach dirIteration@{ subfile ->
                    val format = SpiralFormats.formatForExtension(subfile.extension) ?: SpiralFormats.formatForData(FileDataSource(subfile), identifyFormats)
                    if(format == null)
                        rows.add(arrayOf(file.name + subfile.absolutePath.replace(file.absolutePath, ""), "No Identifiable Format"))
                    else
                        rows.add(arrayOf(file.name + subfile.absolutePath.replace(file.absolutePath, ""), format.name))
                }

                println(FlipTable.of(arrayOf("File", "Format"), rows.toTypedArray()))
            }
        }
    }
    val exit = Command("exit") { println("Bye!"); keepLooping = false }

    fun Command(commandName: String, command: (Pair<Array<String>, String>) -> Unit): InstanceSoldier<InstanceOrder<*>> {
        return InstanceSoldier<InstanceOrder<*>>(InstanceOrder::class.java, commandName, arrayListOf(InstanceWatchtower<InstanceOrder<*>> {
            return@InstanceWatchtower it is InstanceOrder<*> && it.data is String && ((it.data as String).splitOutsideGroup().firstOrNull() ?: "") == commandName
        })) { command((it.data as String).splitOutsideGroup() to it.data as String) }
    }
}