package org.abimon.spiral.mvc.gurren

import com.jakewharton.fliptables.FlipTable
import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.debug
import org.abimon.spiral.core.objects.CustomWAD
import org.abimon.spiral.core.objects.WAD
import org.abimon.spiral.modding.ModManager
import org.abimon.spiral.mvc.SpiralModel
import org.abimon.spiral.mvc.SpiralModel.Command
import org.abimon.visi.collections.joinToPrefixedString
import org.abimon.visi.io.*
import org.abimon.visi.lang.*
import org.abimon.visi.security.sha512Hash
import java.io.File
import java.io.FileOutputStream

@Suppress("unused")
object GurrenOperation {
    val helpTable: String = FlipTable.of(
            arrayOf("Command", "Arguments", "Description", "Example Command"),
            arrayOf(
                    arrayOf("help", "", "Display this message", ""),
                    arrayOf("extract", "[extraction location] {regex}", "Extracts the contents of this WAD file to [extract location], for all files matching {regex} if provided (all files otherwise)", "extract \"dr1${File.separator}bustups\" \".*bustup.*tga\""),
                    arrayOf("exit", "", "Exits the operate scope", "")
            )
    )

    val operatingWad: WAD
        get() = WAD(FileDataSource(SpiralModel.operating ?: throw IllegalStateException("Attempt to get the WAD file while operating is null, this is a bug!")))
    val operatingName: String
        get() = SpiralModel.operating?.nameWithoutExtension ?: ""

    val help = Command("help", "operate") { println(helpTable) }

    val extract = Command("extract", "operate") { (params) ->
        if(params.size == 1)
            return@Command errPrintln("[$operatingName] Error: No directory to extract to provided")

        val directory = File(params[1])
        if(directory.exists()) {
            if (directory.isFile)
                return@Command errPrintln("[$operatingName] Error: $directory is a file")
            else if (!directory.isDirectory)
                return@Command errPrintln("[$operatingName] Error: $directory is not a directory")
        } else {
            errPrintln("[$operatingName] Warn: $directory does not exist, creating...")
            if(!directory.mkdirs())
                return@Command errPrintln("[$operatingName] Error: $directory could not be created, returning...")
        }

        val regex = (if(params.size > 2) params[2] else ".*").toRegex()
        val wad = operatingWad

        val matching = wad.files.filter { (name) -> name.matches(regex) || name.child.matches(regex) }

        println("[$operatingName] Attempting to extract files matching the regex ${regex.pattern}, which is the following list of files: ")
        println("")
        println(matching.joinToPrefixedString("\n", "[$operatingName]\t") { name })
        println("")
        if(question("[$operatingName] Proceed with extraction (Y/n)? ", "Y")) {
            val rows = ArrayList<Array<String>>()
            matching.forEach { entry ->
                val parents = File(directory, entry.name.parents)
                if(!parents.exists() && !parents.mkdirs())
                    return@forEach errPrintln("[$operatingName] Warn: $parents could not be created; skipping ${entry.name}")

                val output = File(directory, entry.name)
                FileOutputStream(output).use { outputStream -> entry.use { inputStream -> inputStream.writeTo(outputStream) } }
                debug("[$operatingName] Wrote ${entry.name} to $output")
                rows.add(arrayOf(entry.name, output relativePathTo directory))
            }
            println(FlipTable.of(arrayOf("File", "Output"), rows.toTypedArray()))
        }
    }
    val extractNicely = Command("extract_nicely", "operate") { (params) ->
        if(params.size == 1)
            return@Command errPrintln("[$operatingName] Error: No directory to extract to provided")

        val directory = File(params[1])
        if(directory.exists()) {
            if (directory.isFile)
                return@Command errPrintln("[$operatingName] Error: $directory is a file")
            else if (!directory.isDirectory)
                return@Command errPrintln("[$operatingName] Error: $directory is not a directory")
        } else {
            errPrintln("[$operatingName] Warn: $directory does not exist, creating...")
            if(!directory.mkdirs())
                return@Command errPrintln("[$operatingName] Error: $directory could not be created, returning...")
        }

        val regex = (if(params.size > 2) params[2] else ".*").toRegex()
        val wad = operatingWad

        val matching = wad.files.filter { (name) -> name.matches(regex) || name.child.matches(regex) }

        println("[$operatingName] Attempting to extract files matching the regex ${regex.pattern}, which is the following list of files: ")
        println("")
        println(matching.joinToPrefixedString("\n", "[$operatingName]\t") { name })
        println("")
        if(question("[$operatingName] Proceed with extraction (Y/n)? ", "Y")) {
            val formatParams = mapOf("pak:convert" to true, "lin:dr1" to operatingName.startsWith("dr1"))

            val rows = ArrayList<Array<String>>()
            matching.forEach { entry ->
                val parents = File(directory, entry.name.parents)
                if(!parents.exists() && !parents.mkdirs())
                    return@forEach errPrintln("[$operatingName] Warn: $parents could not be created; skipping ${entry.name}")
                val format = SpiralFormats.formatForExtension(entry.name.extension, SpiralFormats.drWadFormats) ?: SpiralFormats.formatForData(entry, SpiralFormats.drWadFormats)

                val convertingTo = format?.conversions?.firstOrNull()

                if(format == null) {
                    val output = File(directory, entry.name)
                    FileOutputStream(output).use { outputStream -> entry.use { inputStream -> inputStream.writeTo(outputStream) } }
                    rows.add(arrayOf(entry.name, "Unknown", "None", output relativePathTo directory))
                } else if(convertingTo == null) {
                    val output = File(directory, entry.name)
                    FileOutputStream(output).use { outputStream -> entry.use { inputStream -> inputStream.writeTo(outputStream) } }
                    rows.add(arrayOf(entry.name, format.name, "None", output relativePathTo directory))
                } else {
                    val output = File(directory, entry.name.replace(".${format.extension}", "") + ".${convertingTo.extension ?: "unk"}")
                    FileOutputStream(output).use { outputStream -> format.convert(convertingTo, entry, outputStream, formatParams) }
                    rows.add(arrayOf(entry.name, format.name, convertingTo.name, output relativePathTo directory))
                }
            }
            println(FlipTable.of(arrayOf("File", "File Format", "Converted Format", "Output"), rows.toTypedArray()))
        }
    }

    val compile = Command("compile", "operate") { (params) ->
        if(params.size == 1)
            return@Command errPrintln("[$operatingName] Error: No directory to compile from provided")

        val directory = File(params[1])
        if(directory.exists()) {
            if (directory.isFile)
                return@Command errPrintln("[$operatingName] Error: $directory is a file")
            else if (!directory.isDirectory)
                return@Command errPrintln("[$operatingName] Error: $directory is not a directory")
        } else
            return@Command errPrintln("[$operatingName] Error: $directory does not exist")

        val regex = (if(params.size > 2) params[2] else ".*").toRegex()
        val wad = operatingWad

        val matching = directory.iterate(filters = Gurren.ignoreFilters).filter { (it relativePathFrom directory).matches(regex) || it.name.matches(regex) }

        println("[$operatingName] Attempting to compile files matching the regex ${regex.pattern}, which is the following list of files: ")
        println("")
        println(matching.joinToPrefixedString("\n", "[$operatingName]\t") { this relativePathFrom directory })
        println("")
        if(question("[$operatingName] Proceed with compilation (Y/n)? ", "Y")) {
            val customWad = make<CustomWAD> {
                wad(wad)

                matching.forEach { entry -> file(entry, entry relativePathFrom directory) }
            }

            val tmpFile = File(SpiralModel.operating!!.absolutePath + ".tmp")
            val backupFile = File(SpiralModel.operating!!.absolutePath + ".backup")
            try {
                FileOutputStream(tmpFile).use(customWad::compile)

                if(backupFile.exists()) backupFile.delete()
                SpiralModel.operating!!.renameTo(backupFile)
                tmpFile.renameTo(SpiralModel.operating!!)

                println("[$operatingName] Successfully compiled ${matching.size} files into $operatingName.wad")
            } finally {
                tmpFile.delete()
            }
        }
    }

    val compileNicely = Command("compile_nicely", "operate") { (params) ->
        if(params.size == 1)
            return@Command errPrintln("[$operatingName] Error: No directory to compile from provided")

        val directory = File(params[1])
        if(directory.exists()) {
            if (directory.isFile)
                return@Command errPrintln("[$operatingName] Error: $directory is a file")
            else if (!directory.isDirectory)
                return@Command errPrintln("[$operatingName] Error: $directory is not a directory")
        } else
            return@Command errPrintln("[$operatingName] Error: $directory does not exist")

        val regex = (if(params.size > 2) params[2] else ".*").toRegex()
        val wad = operatingWad

        val matching = directory.iterate(filters = Gurren.ignoreFilters)
                .filter { (it relativePathFrom directory).matches(regex) || it.name.matches(regex) }
                .map { file -> file to (SpiralFormats.formatForExtension(file.extension) ?: SpiralFormats.formatForData(FileDataSource(file))) }
                .map { (file, format) -> if(format in SpiralFormats.drWadFormats) file to null else file to format }
                .toMap()

        println("[$operatingName] Attempting to convert and compile files matching the regex ${regex.pattern}, which is the following list of files: ")
        println("")
        println(matching.entries.joinToPrefixedString("\n", "[$operatingName]\t") {
            if(this.value == null)
                "${this.key relativePathFrom directory} (No known format)"
            else if(this.value!!.conversions.isEmpty())
                "${this.key relativePathFrom directory} (Cannot convert from ${this.value!!.name})"
            else
                "${this.key relativePathFrom directory} (${this.value!!.name} -> ${this.value!!.conversions.first().name})"
        })
        println("")
        if(question("[$operatingName] Proceed with conversion and compilation (Y/n)? ", "Y")) {
            val formatParams = mapOf("pak:convert" to true, "lin:dr1" to operatingName.startsWith("dr1"))
            val customWad = make<CustomWAD> {
                wad(wad)

                matching.filter { (_, format) -> format == null }.forEach { (entry) -> file(entry, entry relativePathFrom directory) }
                matching.filter { (_, format) -> format != null }.forEach { (entry, from) ->
                    val name = (entry relativePathFrom directory).replaceLast(".${from!!.extension ?: "unk"}", ".${from.conversions.first().extension ?: "unk"}")
                    data(name, ByteArrayDataSource(from.convertToBytes(from.conversions.first(), FileDataSource(entry), formatParams)))
                }
            }

            val tmpFile = File(SpiralModel.operating!!.absolutePath + ".tmp")
            val backupFile = File(SpiralModel.operating!!.absolutePath + ".backup")
            try {
                FileOutputStream(tmpFile).use(customWad::compile)

                if(backupFile.exists()) backupFile.delete()
                SpiralModel.operating!!.renameTo(backupFile)
                tmpFile.renameTo(SpiralModel.operating!!)

                println("[$operatingName] Successfully compiled ${matching.size} files into $operatingName.wad")
            } finally {
                tmpFile.delete()
            }
        }
    }

    val fingerprintWad = Command("fingerprint_wad", "operate") {
        val fileMap: MutableMap<String, Map<String, String>> = HashMap()
        val fingerprints: MutableMap<String, String> = HashMap()
        fileMap["1312478"] = fingerprints

        operatingWad.files.forEach { file -> fingerprints[file.name] = file.use { it.sha512Hash() } }

        SpiralData.MAPPER.writeValue(File("fingerprints.json"), fileMap)
    }

    val info = Command("info", "operate") { (params) ->
        val regex = (if(params.size > 1) params[1] else ".*").toRegex()
        val wad = operatingWad

        val matching = wad.files.filter { (name) -> name.matches(regex) || name.child.matches(regex) }.map { file -> arrayOf(file.name, "${file.fileSize} B", "${file.offset} B from the beginning", ModManager.getModForFingerprint(file)?.run { "${first.name} v$second" } ?: "Unknown") }.toTypedArray()
        println(FlipTable.of(arrayOf("Entry Name", "Entry Size", "Entry Offset", "Mod Origin"), matching))
    }

    val exit = Command("exit", "operate") { SpiralModel.scope = "> " to "default" }

    val operateOn = Command("operate", "default") { (params) ->
        if(SpiralModel.wads.isEmpty())
            return@Command errPrintln("Error: No WAD files registered")
        if(params.size == 1) {
            println("Select a WAD file to operate on")
            println(SpiralModel.wads.joinToPrefixedString("\n", "\t") { "$nameWithoutExtension ($absolutePath)" } )
            while(true) {
                print("[operate] > ")
                val wadName = readLine() ?: break
                val wad = SpiralModel.wads.firstOrNull { file -> file.nameWithoutExtension == wadName || file.absolutePath == wadName }
                if(wad == null)
                    println("Invalid WAD file $wad")
                else {
                    SpiralModel.operating = wad
                    SpiralModel.scope = "[${wad.nameWithoutExtension}]|> " to "operate"
                    println("Now operating on ${wad.nameWithoutExtension}")

                    break
                }
            }
        }
    }

    fun process() {}
}