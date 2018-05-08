package org.abimon.spiral.mvc.gurren

import com.jakewharton.fliptables.FlipTable
import kotlinx.coroutines.experimental.runBlocking
import org.abimon.imperator.impl.InstanceOrder
import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.core.archives.CPKArchive
import org.abimon.spiral.core.archives.IArchive
import org.abimon.spiral.core.archives.WADArchive
import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.core.data.FileContext
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.game.v3.V3
import org.abimon.spiral.modding.HookManager
import org.abimon.spiral.modding.ModManager
import org.abimon.spiral.mvc.SpiralModel
import org.abimon.spiral.mvc.SpiralModel.Command
import org.abimon.spiral.mvc.SpiralModel.operating
import org.abimon.spiral.util.*
import org.abimon.visi.collections.copyFrom
import org.abimon.visi.collections.joinToPrefixedString
import org.abimon.visi.io.*
import org.abimon.visi.lang.*
import java.awt.Desktop
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URI
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.zip.ZipFile
import kotlin.system.measureTimeMillis

@Suppress("unused", "MemberVisibilityCanBePrivate")
object GurrenOperation {
    val helpTable: String = FlipTable.of(
            arrayOf("Command", "Arguments", "Description", "Example Command"),
            arrayOf(
                    arrayOf("help", "", "Display this message", ""),
                    arrayOf("extract", "[extraction location] {regex}", "Extracts the contents of this WAD file to [extract location], for all files matching {regex} if provided (all files otherwise)", "extract \"dr1${File.separator}bustups\" \".*bustup.*tga\""),
                    arrayOf("exit", "", "Exits the operate scope", "")
            )
    )

    private var backingOperatingArchive: IArchive? = null

    val operatingArchive: IArchive
        get() = backingOperatingArchive
                ?: throw IllegalStateException("Attempt to get the archive while operating is null, this is a bug!")

    val operatingName: String
        get() = SpiralModel.operating?.nameWithoutExtension ?: ""
    val operatingGame: DRGame?
        get() {
            when (SpiralModel.operating?.name?.substringBefore('_') ?: return null) {
                "dr1" -> return DR1
                "dr2" -> return DR2
                "partition" -> return V3
                else -> return null
            }
        }

    val help = Command("help", "operate") { println(helpTable) }

    val extract = Command("extract", "operate") { (params) ->
        if (params.size == 1)
            return@Command errPrintln("[$operatingName] Error: No directory to extract to provided")

        val opArchive = operatingArchive

        val directory = File(params[1])
        if (directory.exists()) {
            if (directory.isFile)
                return@Command errPrintln("[$operatingName] Error: $directory is a file")
            else if (!directory.isDirectory)
                return@Command errPrintln("[$operatingName] Error: $directory is not a directory")
        } else {
            errPrintln("[$operatingName] Warn: $directory does not exist, creating...")
            if (!directory.mkdirs())
                return@Command errPrintln("[$operatingName] Error: $directory could not be created, returning...")
        }

        val regex = (if (params.size > 2) params[2] else ".*").toRegex()

        val matching = opArchive.fileEntries.filter { (name) -> name.matches(regex) || name.child.matches(regex) }

        if (!HookManager.shouldExtract(opArchive, directory, matching))
            return@Command errPrintln("[$operatingName] Extraction cancelled by plugin")

        val proceed = SpiralModel.confirm {
            println("[$operatingName] Attempting to extract files matching the regex ${regex.pattern}, which is the following list of files: ")
            println("")
            println(matching.joinToPrefixedString("\n", "[$operatingName]\t") { first })
            println("")

            return@confirm question("[$operatingName] Proceed with extraction (Y/n)? ", "Y")
        }

        if (proceed) {
            HookManager.extracting(opArchive, directory, matching)

            val rows: MutableCollection<Array<String>> = ArrayList<Array<String>>()
            val duration = measureTimeMillis {
                val onExtract: (String, () -> InputStream) -> Unit = if (SpiralModel.noFluffIO) { _, _ -> } else { entryName, entry -> HookManager.extractingFile(opArchive, directory, matching, entryName to entry) }
                matching.forEach { (entryName, entry) ->
                    onExtract(entryName, entry)

                    val parents = File(directory, entryName.parents)
                    if (!parents.exists() && !parents.mkdirs()) //Second check due to concurrency
                        return@forEach errPrintln("[$operatingName] Warn: $parents could not be created; skipping $entryName")

                    val output = File(directory, entryName)
                    FileOutputStream(output).use { outputStream -> decompress(entry)().use { inputStream -> inputStream.copyTo(outputStream) } }
                    debug("[$operatingName] Wrote $entryName to $output")
                    rows.add(arrayOf(entryName, output relativePathTo directory))
                }
            }

            HookManager.finishedExtraction(opArchive, directory, matching)
            println(FlipTable.of(arrayOf("File", "Output"), rows.toTypedArray()))
            debug("Took $duration ms")
        }
    }
    val extractNicely = Command("extract_nicely", "operate") { (params) ->
        if (params.size == 1)
            return@Command errPrintln("[$operatingName] Error: No directory to extract to provided")

        val directory = File(params[1])
        if (directory.exists()) {
            if (directory.isFile)
                return@Command errPrintln("[$operatingName] Error: $directory is a file")
            else if (!directory.isDirectory)
                return@Command errPrintln("[$operatingName] Error: $directory is not a directory")
        } else {
            errPrintln("[$operatingName] Warn: $directory does not exist, creating...")
            if (!directory.mkdirs())
                return@Command errPrintln("[$operatingName] Error: $directory could not be created, returning...")
        }

        val regex = (if (params.size > 2) params[2] else ".*").toRegex()

        val archive = operatingArchive
        val matching = archive.fileEntries.filter { (name) -> name.matches(regex) || name.child.matches(regex) }.sortedBy { (name) -> name }

        val proceed = SpiralModel.confirm {
            println("[$operatingName] Attempting to extract files matching the regex ${regex.pattern}, which is the following list of files: ")
            println("")
            println(matching.joinToPrefixedString("\n", "[$operatingName]\t") { first })
            println("")

            return@confirm question("[$operatingName] Proceed with extraction (Y/n)? ", "Y")
        }

        if (proceed) {
            HookManager.extracting(archive, directory, matching)
            val formatParams: MutableMap<String, Any> = hashMapOf("pak:convert" to true)

            if (params.size > 3) params.copyFrom(3).mapNotNull { param -> param.split('=', limit = 2).takeIf { paramList -> paramList.size == 2 }?.run { this[0] to this[1] } }.forEach { (key, value) -> formatParams[key] = value }

            val rows: MutableCollection<Array<String>> = ConcurrentLinkedQueue()
            val onExtract: (String, () -> InputStream) -> Unit = if (SpiralModel.noFluffIO) { _, _ -> } else { entryName, entry -> HookManager.extractingFile(archive, directory, matching, entryName to entry) }

            val duration = measureTimeMillis {
                runBlocking {
                    matching.forEach { (entryName) ->
                        val parents = File(directory, entryName.parents)
                        if (!parents.exists())
                            parents.mkdirs()
                    }

                    //debug("Next ${SpiralModel.concurrentOperations.coerceAtLeast(1)}: ${sublist.joinToString { (entryName) -> entryName }}")
                    SpiralModel.distribute(matching) launch@{ (entryName, entry) ->
                        onExtract(entryName, entry)
                        val parents = File(directory, entryName.parents)
                        if (!parents.exists() && !parents.mkdirs() && !parents.exists())
                            return@launch errPrintln("[$operatingName] Warn: $parents could not be created; skipping $entryName")
                        val data = decompress(entry)
                        val format = SpiralFormats.formatForExtension(entryName.extension, SpiralFormats.drArchiveFormats)
                                ?: SpiralFormats.formatForData(operatingGame, data, entryName, SpiralFormats.drArchiveFormats)

                        val convertingTo = format?.conversions?.firstOrNull()

                        when {
                            format == null -> {
                                val output = File(directory, entryName)
                                FileOutputStream(output).use { outputStream -> data().use { inputStream -> inputStream.copyTo(outputStream) } }
                                rows.add(arrayOf(entryName, "Unknown", "None", output relativePathTo directory))
                            }
                            convertingTo == null -> {
                                val output = File(directory, entryName)
                                FileOutputStream(output).use { outputStream -> data().use { inputStream -> inputStream.copyTo(outputStream) } }
                                rows.add(arrayOf(entryName, format.name, "None", output relativePathTo directory))
                            }
                            else -> {
                                try {
                                    val output = File(directory, entryName.replace(".${format.extension
                                            ?: "unk"}", "", true) + ".${convertingTo.extension ?: "unk"}")
                                    FileOutputStream(output).use { outputStream -> format.convert(operatingGame, convertingTo, entryName, archive::fileSourceForname, data, outputStream, formatParams) }
                                    rows.add(arrayOf(entryName, format.name, convertingTo.name, output relativePathTo directory))
                                } catch (iea: IllegalArgumentException) {
                                    val output = File(directory, entryName)
                                    FileOutputStream(output).use { outputStream -> data().use { inputStream -> inputStream.copyTo(outputStream) } }
                                    rows.add(arrayOf(entryName, format.name, "ERR", output relativePathTo directory))

                                    if (LoggerLevel.ERROR.enabled)
                                        LoggerLevel.ERROR(iea.exportStackTrace())
                                }
                            }
                        }
                    }
                }
            }

            HookManager.finishedExtraction(archive, directory, matching)

            println(FlipTable.of(arrayOf("File", "File Format", "Converted Format", "Output"), rows.toTypedArray()))
//            debug("Took $duration ms; opened a total of ${archive.openedStreams.get()}, and we have ${archive.openStreams.get()} left over")
        }
    }

    val compile = Command("compile", "operate") { (params) ->
        if (params.size == 1)
            return@Command errPrintln("[$operatingName] Error: No directory to compile from provided")

        val directory = File(params[1])
        if (directory.exists()) {
            if (directory.isFile)
                return@Command errPrintln("[$operatingName] Error: $directory is a file")
            else if (!directory.isDirectory)
                return@Command errPrintln("[$operatingName] Error: $directory is not a directory")
        } else
            return@Command errPrintln("[$operatingName] Error: $directory does not exist")

        val regex = (if (params.size > 2) params[2] else ".*").toRegex()

        val matching = directory.iterate(filters = Gurren.ignoreFilters).filter { (it relativePathFrom directory).matches(regex) || it.name.matches(regex) }

        val proceed = SpiralModel.confirm {
            println("[$operatingName] Attempting to compile files matching the regex ${regex.pattern}, which is the following list of files: ")
            println("")
            println(matching.joinToPrefixedString("\n", "[$operatingName]\t") { this relativePathFrom directory })
            println("")

            return@confirm question("[$operatingName] Proceed with compilation (Y/n)? ", "Y")
        }

        if (proceed) {
            operatingArchive.compile(matching.map { file -> (file relativePathFrom directory) to { FileInputStream(file) } })
            println("[$operatingName] Successfully compiled ${matching.size} files into ${operating!!.name}")
        }
    }

    val compileAndRun = Command("compile_and_run", "operate") { (params) ->
        compile.command(InstanceOrder<String>("COMPILE_AND_RUN", scout = null, data = params.apply { this[0] = "compile" }.joinToString(" ") { cmd -> "\"$cmd\"" }))

        if(Desktop.isDesktopSupported()) {
            operatingGame?.steamID?.let { steamID -> Desktop.getDesktop().browse(URI("steam://run/$steamID")) }
        } else {
            errPrintln("No desktop environment detected; running in headless most likely!")
        }
    }

    val compileNicely = Command("compile_nicely", "operate") { (params) ->
        if (params.size == 1)
            return@Command errPrintln("[$operatingName] Error: No directory to compile from provided")

        val directory = File(params[1])
        if (directory.exists()) {
            if (directory.isFile)
                return@Command errPrintln("[$operatingName] Error: $directory is a file")
            else if (!directory.isDirectory)
                return@Command errPrintln("[$operatingName] Error: $directory is not a directory")
        } else
            return@Command errPrintln("[$operatingName] Error: $directory does not exist")

        val regex = (if (params.size > 2) params[2] else ".*").toRegex()

        val matching = directory.iterate(filters = Gurren.ignoreFilters)
                .filter { (it relativePathFrom directory).matches(regex) || it.name.matches(regex) }
                .map { file ->
                    file to (SpiralFormats.formatForExtension(file.extension)
                            ?: SpiralFormats.formatForData(operatingGame, file::inputStream, file relativePathFrom directory))
                }
                .map { (file, format) -> if (format in SpiralFormats.drArchiveFormats) file to null else file to format }
                .toMap()

        val proceed = SpiralModel.confirm {
            println("[$operatingName] Attempting to convert and compile files matching the regex ${regex.pattern}, which is the following list of files: ")
            println("")
            println(matching.entries.joinToPrefixedString("\n", "[$operatingName]\t") {
                when {
                    this.value == null -> "${this.key relativePathFrom directory} (No known format)"
                    this.value!!.conversions.isEmpty() -> "${this.key relativePathFrom directory} (Cannot convert from ${this.value!!.name})"
                    else -> "${this.key relativePathFrom directory} (${this.value!!.name} -> ${this.value!!.conversions.first().name})"
                }
            })
            println("")

            return@confirm question("[$operatingName] Proceed with conversion and compilation (Y/n)? ", "Y")
        }

        if (proceed) {
            val formatParams = mapOf("pak:convert" to true)
            val fileContext = FileContext(directory)
//            val customWad = make<CustomWAD> {
//                wad(wad)
//
//                matching.filter { (_, format) -> format == null }.forEach { (entry) -> file(entry, entry relativePathFrom directory) }
//                matching.filter { (_, format) -> format != null }.forEach { (entry, from) ->
//                    val name = (entry relativePathFrom directory).replaceLast(".${from!!.extension ?: "unk"}", ".${from.conversions.first().extension ?: "unk"}")
//                    data(name, ByteArrayDataSource(from.convertToBytes(from.conversions.first(), FileDataSource(entry), formatParams)))
//                }
//            }

            val newEntries: MutableList<Pair<String, () -> InputStream>> = ArrayList()

            newEntries.addAll(matching.filter { (_, format) -> format == null || format.conversions.isEmpty() }.map { (file) -> (file relativePathFrom directory) to { FileInputStream(file) } })

            newEntries.addAll(matching.filter { (_, format) -> format != null && format.conversions.isNotEmpty() }.map { (entry, from) ->
                val name = (entry relativePathFrom directory).replaceLast(".${from!!.extension
                        ?: "unk"}", ".${from.conversions.first().extension ?: "unk"}")
                val (formatOut, formatIn) = CacheHandler.cacheStream()
                from.convert(operatingGame, operatingArchive.niceCompileFormats[from]
                        ?: from.conversions.first(), entry relativePathFrom directory, fileContext::provide, entry::inputStream, formatOut, formatParams)
                return@map name to formatIn
            })

            operatingArchive.compile(newEntries)
            println("[$operatingName] Successfully compiled ${matching.size} files into ${operating!!.name}")
        }
    }

    val compileNicelyAndRun = Command("compile_nicely_and_run", "operate") { (params) ->
        compileNicely.command(InstanceOrder<String>("COMPILE_NICELY_AND_RUN", scout = null, data = params.apply { this[0] = "compile_nicely" }.joinToString(" ") { cmd -> "\"$cmd\"" }))

        if(Desktop.isDesktopSupported()) {
            operatingGame?.steamID?.let { steamID -> Desktop.getDesktop().browse(URI("steam://run/$steamID")) }
        } else {
            errPrintln("No desktop environment detected; running in headless most likely!")
        }
    }

    val restore = Command("restore", "operate") { (params) ->
        val backupFile = File((SpiralModel.operating
                ?: return@Command errPrintln("Error: SpiralModel#operating is null, this is a bug!")).absolutePath.replaceAfterLast('.', "zip"))
        val backupZip = ZipFile(backupFile)
        val regex = (if (params.size < 2) ".*" else params[1]).toRegex()

        val archive = GurrenModding.operatingArchive
        val archiveFiles = archive.fileEntries.map { (name) -> name }
        val archiveBackupFiles = backupZip.entries().toList().filter { zipEntry -> zipEntry.name in archiveFiles && zipEntry.name.matches(regex) }
        val proceed = SpiralModel.confirm {
            println("[$operatingName] Attempting to restore files matching the regex ${regex.pattern}, which is the following list of files: ")
            println("")
            println(archiveBackupFiles.joinToPrefixedString("\n", "[$operatingName]\t") { name })
            println("")

            return@confirm question("[${GurrenModding.operatingName}] Proceed with restoration (Y/n)? ", "Y")
        }

        if (proceed) {
            val backupEntries = archiveBackupFiles.map { zipEntry ->
                val (out, ds) = CacheHandler.cacheStream()

                backupZip.getInputStream(zipEntry).use { stream -> out.use { outStream -> stream.copyTo(outStream) } }

                return@map zipEntry.name to ds
            }

            archive.compile(backupEntries)

            println("Restored ${backupEntries.size} from backup")
        }
    }

    val info = Command("info", "operate") { (params) ->
        val regex = (if (params.size > 1) params[1] else ".*").toRegex()
        when (operatingArchive) {
            is WADArchive -> {
                val wad = (operatingArchive as WADArchive).wad

                val matching = wad.files.filter { (name) -> name.matches(regex) || name.child.matches(regex) }.map { file ->
                    arrayOf(file.name, "${file.size} B", "${file.offset} B from the beginning", ModManager.getModForFingerprint(InputStreamFuncDataSource(file::inputStream))?.mod_uid
                            ?: "Unknown")
                }.toTypedArray()
                println(FlipTable.of(arrayOf("Entry Name", "Entry Size", "Entry Offset", "Mod Origin"), matching))
            }
            is CPKArchive -> {
                val cpk = (operatingArchive as CPKArchive).cpk

                val matching = cpk.files.filter { (fileName, dirName) -> "$fileName/$dirName".matches(regex) || fileName.matches(regex) }.map { file ->
                    arrayOf("${file.directoryName}/${file.fileName}", "${file.fileSize} B", "${file.offset} B from the beginning", ModManager.getModForFingerprint(InputStreamFuncDataSource(file::inputStream))?.mod_uid
                            ?: "Unknown")
                }.toTypedArray()
                println(FlipTable.of(arrayOf("Entry Name", "Entry Size", "Entry Offset", "Mod Origin"), matching))
            }
        }
    }

    val exit = Command("exit", "operate") {
        SpiralModel.scope = "> " to "default"
        SpiralModel.operating = null
    }

    val operateOn = Command("operate", "default") { (params) ->
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
                    SpiralModel.scope = "[Operation ${archive.nameWithoutExtension}]|> " to "operate"
                    println("Now operating on ${archive.nameWithoutExtension}")

                    return@Command
                }
            }
        }

        println("Select an archive to operate on")
        println(SpiralModel.archives.joinToPrefixedString("\n", "\t") { "$nameWithoutExtension ($absolutePath)" })
        while (true) {
            print("[operate] > ")
            val archiveName = readLine() ?: break
            if (archiveName == "exit")
                break

            val archive = SpiralModel.archives.firstOrNull { file -> file.nameWithoutExtension == archiveName || file.absolutePath == archiveName }
            if (archive == null)
                println("Invalid archive $archiveName")
            else {
                SpiralModel.operating = archive
                SpiralModel.scope = "[Operation ${archive.nameWithoutExtension}]|> " to "operate"
                println("Now operating on ${archive.nameWithoutExtension}")

                break
            }
        }
    }

    fun onArchiveChange(old: File?, new: File?) {
        backingOperatingArchive = new?.let(IArchive.Companion::invoke)
    }

    init {
        HookManager.ON_OPERATING_CHANGE.add(SpiralData.BASE_PLUGIN to this::onArchiveChange)
        onArchiveChange(null, SpiralModel.operating)
    }
}