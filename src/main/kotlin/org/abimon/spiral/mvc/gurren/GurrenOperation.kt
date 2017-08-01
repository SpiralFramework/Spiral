package org.abimon.spiral.mvc.gurren

import com.jakewharton.fliptables.FlipTable
import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.core.objects.WAD
import org.abimon.spiral.mvc.SpiralModel
import org.abimon.spiral.mvc.SpiralModel.Command
import org.abimon.visi.collections.joinToPrefixedString
import org.abimon.visi.io.FileDataSource
import org.abimon.visi.io.errPrintln
import org.abimon.visi.io.question
import org.abimon.visi.io.writeTo
import org.abimon.visi.lang.child
import org.abimon.visi.lang.extension
import org.abimon.visi.lang.parents
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
        println()
        println(matching.joinToPrefixedString("\n", "[$operatingName]\t") { name })
        println()
        if(question("[$operatingName] Proceed with extraction (Y/n)? ", "Y")) {
            matching.forEach { entry ->
                val parents = File(directory, entry.name.parents)
                if(!parents.exists() && !parents.mkdirs())
                    return@forEach errPrintln("[$operatingName] Warn: $parents could not be created; skipping ${entry.name}")

                val output = File(directory, entry.name)
                FileOutputStream(output).use { outputStream -> entry.use { inputStream -> inputStream.writeTo(outputStream) } }
                println("[$operatingName] Wrote ${entry.name} to $output")
            }
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
        println()
        println(matching.joinToPrefixedString("\n", "[$operatingName]\t") { name })
        println()
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
                    rows.add(arrayOf(entry.name, "Unknown", "None", directory.name + output.absolutePath.replace(directory.absolutePath, "")))
                } else if(convertingTo == null) {
                    val output = File(directory, entry.name)
                    FileOutputStream(output).use { outputStream -> entry.use { inputStream -> inputStream.writeTo(outputStream) } }
                    rows.add(arrayOf(entry.name, format.name, "None", directory.name + output.absolutePath.replace(directory.absolutePath, "")))
                } else {
                    val output = File(directory, entry.name.replace(".${format.extension}", "") + ".${convertingTo.extension ?: "unk"}")
                    FileOutputStream(output).use { outputStream -> format.convert(convertingTo, entry, outputStream, formatParams) }
                    rows.add(arrayOf(entry.name, format.name, convertingTo.name, directory.name + output.absolutePath.replace(directory.absolutePath, "")))
                }
            }
            println(FlipTable.of(arrayOf("File", "File Format", "Converted Format", "Output"), rows.toTypedArray()))
        }
    }
    val info = Command("info", "operate") { (params) ->
        val regex = (if(params.size > 1) params[1] else ".*").toRegex()
        val wad = operatingWad

        val matching = wad.files.filter { (name) -> name.matches(regex) || name.child.matches(regex) }.map { (name, fileSize, offset) -> arrayOf(name, "$fileSize B", "$offset B from the beginning") }.toTypedArray()
        println(FlipTable.of(arrayOf("Entry Name", "Entry Size", "Entry Offset"), matching))
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