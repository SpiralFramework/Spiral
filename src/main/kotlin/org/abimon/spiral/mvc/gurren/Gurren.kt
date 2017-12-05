package org.abimon.spiral.mvc.gurren

import com.github.kittinunf.fuel.Fuel
import com.jakewharton.fliptables.FlipTable
import org.abimon.imperator.impl.InstanceOrder
import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.core.archives.IArchive
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.archives.CPKFormat
import org.abimon.spiral.core.formats.archives.PAKFormat
import org.abimon.spiral.core.formats.archives.WADFormat
import org.abimon.spiral.core.formats.images.*
import org.abimon.spiral.core.formats.scripting.LINFormat
import org.abimon.spiral.core.formats.scripting.NonstopFormat
import org.abimon.spiral.core.formats.video.MP4Format
import org.abimon.spiral.core.userAgent
import org.abimon.spiral.mvc.SpiralModel
import org.abimon.spiral.mvc.SpiralModel.Command
import org.abimon.spiral.util.MediaWrapper
import org.abimon.spiral.util.debug
import org.abimon.spiral.util.responseStream
import org.abimon.visi.collections.copyFrom
import org.abimon.visi.collections.group
import org.abimon.visi.collections.joinToPrefixedString
import org.abimon.visi.io.*
import org.abimon.visi.lang.EnumOS
import org.abimon.visi.lang.replaceLast
import org.abimon.visi.security.md5Hash
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileFilter
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.*
import kotlin.collections.ArrayList
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

    val separator: String = File.separator
    var keepLooping: Boolean = true
    val version: String
        get() = Gurren::class.java.protectionDomain.codeSource.location.openStream().md5Hash()

    val helpTable: String = FlipTable.of(
            arrayOf("Command", "Arguments", "Description", "Example Command"),
            arrayOf(
                    arrayOf("help", "", "Display this message", ""),
                    arrayOf("find", "", "Try to find the archive files for DR, if they're in their normal locations", ""),
                    arrayOf("locate", "[directory]", "Search [directory] for all archive. Note: Will take a fair amount of time, and requires confirmation for each archive.", "find \"Steam${separator}steamapps${separator}common${separator}Danganronpa: Trigger Happy Havoc\""),
                    arrayOf("register", "[archive]", "Register an individual archive", "register \"dr1_data_us.wad\""),
                    arrayOf("registered", "", "Display the registered archives", ""),
                    arrayOf("formats", "", "Display the formats table", ""),
                    arrayOf("identify", "[file|directory]", "Identify the format of either the provided [file], or the files in the provided [directory]", "identify \"images\""),
                    arrayOf("identify_and_convert", "[file|directory] [format] {params}", "Identify the format of either the provided [file], or the files in the provided [directory], and try to convert them to [format] with the provided {params} (or no params if not provided)", "identify_and_convert \"images\" \"png\""),
                    arrayOf("convert", "[file|directory] [from] [to] {params}", "Convert the provided [file], or the files in the provided [directory], from [from] to [to] with the provided {params} (or no params if not provided)", "convert \"scripts\" \"lin\" \"txt\" \"lin:dr1=true\""),
                    arrayOf("exit", "", "Exits the program", "")
            )
    )

    val formatTable: String = FlipTable.of(
            arrayOf("Format", "Can Convert To"),
            arrayOf(
                    arrayOf("WAD", WADFormat.conversions.joinToString { it.name }),
                    arrayOf("CPK", CPKFormat.conversions.joinToString { it.name }),
                    arrayOf("PAK", PAKFormat.conversions.joinToString { it.name }),
                    arrayOf("TGA", TGAFormat.conversions.joinToString { it.name }),
                    arrayOf("SHTX", SHTXFormat.conversions.joinToString { it.name }),
                    arrayOf("PNG", PNGFormat.conversions.joinToString { it.name }),
                    arrayOf("JPG", JPEGFormat.conversions.joinToString { it.name }),
                    arrayOf("LIN", LINFormat.conversions.joinToString { it.name }),
                    arrayOf("Nonstop DAT", NonstopFormat.conversions.joinToString { it.name })
            )
    )

    val help = Command("help", "default") { println(helpTable) }
    val find = Command("find") {
        when (os) {
            EnumOS.WINDOWS -> {
                for (root in File.listRoots()) {
                    for (programFolder in arrayOf(File(root, "Program Files (x86)"), File(root, "Program Files"))) {
                        val steamFolder = File(programFolder, "Steam")
                        if (steamFolder.exists()) {
                            val common = File(steamFolder, "steamapps${File.separator}common")
                            for (game in common.listFiles { file -> file.isDirectory && file.name.contains("Danganronpa") })
                                SpiralModel.archives.addAll(game.iterate().filter { file -> file.isFile && file.extension in IArchive.EXTENSIONS && !file.name.contains(".backup") })
                        }
                    }
                }
            }
            EnumOS.MACOSX -> {
                val steamFolder = os.getStorageLocation("Steam")
                if (steamFolder.exists()) {
                    val common = File(steamFolder, "steamapps${File.separator}common")
                    for (game in common.listFiles { file -> file.isDirectory && file.name.contains("Danganronpa") })
                        SpiralModel.archives.addAll(game.iterate().filter { file -> file.isFile && file.extension in IArchive.EXTENSIONS && !file.name.contains(".backup") })
                }
            }
            else -> println("No behaviour defined for $os!")
        }

        if (SpiralModel.archives.isEmpty())
            errPrintln("Error: No archive files detected! You can manually add them via the register command, or by running the locate command!")
        else
            println("Archives: ${SpiralModel.archives.joinToPrefixedString("", "\n\t")}")

        SpiralModel.save()
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
                        FileFilter { file -> file.isDirectory || (file.isFile && file.extension in IArchive.EXTENSIONS && !file.name.contains(".backup")) }
                )).forEach { archive ->
                    if (question("Archive Found ($archive). Would you like to add this to the internal registry (Y/N)? ", "Y"))
                        SpiralModel.archives.add(archive)
                }
            }
            println("Took $time ms.")
            if (SpiralModel.archives.isEmpty())
                errPrintln("Error: No archive files detected! You can manually add them via the register command, or by running the locate command!")
            else
                println("archives: ${SpiralModel.archives.joinToPrefixedString("", "\n\t")}")

            SpiralModel.save()
        }
    }
    val register = Command("register") { (operation) ->
        if (operation.size == 1) {
            errPrintln("Error: No file provided!")
            return@Command
        }

        val archive = File(operation.copyFrom(1).joinToString(" "))
        if (!archive.exists()) {
            errPrintln("Error: $archive does not exist!")
            return@Command
        }

        if (archive.extension !in IArchive.EXTENSIONS && !archive.isDirectory) {
            errPrintln("Error: $archive is not an archive file!")
            return@Command
        }

        SpiralModel.archives.add(archive)
        println("Registered $archive!")
        SpiralModel.save()
    }
    val deregister = Command("deregister") {}
    val registered = Command("registered") { println("Registered archives: ${SpiralModel.archives.joinToPrefixedString("", "\n\t")}") }
    val formats = Command("formats") { println(formatTable) }

    val identify = Command("identify") { (params) ->
        if (params.size == 1)
            errPrintln("Error: No file or directory provided")

        val files = params.map { File(it) }

        files.forEach { file ->
            if (file.isFile) {
                val rows = ArrayList<Array<String>>()
                val format = SpiralFormats.formatForExtension(file.extension) ?: SpiralFormats.formatForData(FileDataSource(file))
                if (format == null)
                    rows.add(arrayOf(file.name, "No Identifiable Format"))
                else
                    rows.add(arrayOf(file.name, format.name))

                println(FlipTable.of(arrayOf("File", "Format"), rows.toTypedArray()))
            } else if (file.isDirectory) {
                val rows = ArrayList<Array<String>>()
                file.iterate(filters = ignoreFilters).forEach dirIteration@ { subfile ->
                    val format = SpiralFormats.formatForExtension(subfile.extension) ?: SpiralFormats.formatForData(FileDataSource(subfile))
                    if (format == null)
                        rows.add(arrayOf(file.name + subfile.absolutePath.replace(file.absolutePath, ""), "No Identifiable Format"))
                    else
                        rows.add(arrayOf(file.name + subfile.absolutePath.replace(file.absolutePath, ""), format.name))
                }

                println(FlipTable.of(arrayOf("File", "Format"), rows.toTypedArray()))
            }
        }
    }
    val identifyAndConvert = Command("identify_and_convert") { (params) ->
        if (params.size == 1)
            return@Command errPrintln("Error: No file or directory provided")

        val file = File(params[1])
        val convertTo: SpiralFormat? = if (params.size < 3) null else SpiralFormats.formatForName(params[2]) ?: SpiralFormats.formatForExtension(params[2])
        val formatParams: Map<String, String> = if (params.size < 4) emptyMap() else params.copyFrom(3).map { it.split('=', limit = 2).takeIf { it.size == 2 }?.run { this[0] to this[1] } }.filterNotNull().toMap()

        val rows = ArrayList<Array<String>>()
        if (file.isFile) {
            val format = SpiralFormats.formatForExtension(file.extension) ?: SpiralFormats.formatForData(FileDataSource(file))
            if (format == null)
                rows.add(arrayOf(file.path, "N/a", "No Identifiable Format", "N/a"))
            else {
                if (convertTo == null) {
                    if (format.conversions.isEmpty())
                        rows.add(arrayOf(file.path, "N/a", format.name, "No Convertable Formats"))
                    else {
                        val tmpConvertTo = format.conversions.first()
                        val output = File(file.absolutePath.replace(".${format.extension ?: file.extension}", "") + ".${tmpConvertTo.extension ?: "unk"}").ensureUnique()

                        try {
                            FileOutputStream(output).use { out -> format.convert(tmpConvertTo, FileDataSource(file), out, formatParams) }
                            rows.add(arrayOf(file.path, output.path, format.name, tmpConvertTo.name))
                        } catch (iea: IllegalArgumentException) {
                            rows.add(arrayOf(file.path, "N/a", format.name, "Could not convert to ${tmpConvertTo.name}: ${iea.localizedMessage}"))
                        } finally {
                            if (output.length() == 0L)
                                output.delete()
                        }
                    }
                } else {
                    if (format.canConvert(convertTo)) {
                        val output = File(file.absolutePath.replace(".${format.extension ?: file.extension}", "") + ".${convertTo.extension ?: "unk"}").ensureUnique()

                        try {
                            FileOutputStream(output).use { out -> format.convert(convertTo, FileDataSource(file), out, formatParams) }
                            rows.add(arrayOf(file.path, output.path, format.name, convertTo.name))
                        } catch (iea: IllegalArgumentException) {
                            rows.add(arrayOf(file.path, "N/a", format.name, "Could not convert to ${convertTo.name}: ${iea.localizedMessage}"))
                        } finally {
                            if (output.length() == 0L)
                                output.delete()
                        }
                    } else
                        rows.add(arrayOf(file.path, "N/a", "${format.name} cannot be converted to ${convertTo.name}", "N/a"))
                }
            }
        } else if (file.isDirectory) {
            file.iterate(filters = ignoreFilters).forEach dirIteration@ { subfile ->
                val format = SpiralFormats.formatForExtension(subfile.extension) ?: SpiralFormats.formatForData(FileDataSource(subfile))
                if (format == null)
                    rows.add(arrayOf(file.name + subfile.absolutePath.replace(file.absolutePath, ""), "N/a", "No Identifiable Format", "N/a"))
                else {
                    if (convertTo == null) {
                        if (format.conversions.isEmpty())
                            rows.add(arrayOf(file.name + subfile.absolutePath.replace(file.absolutePath, ""), "N/a", format.name, "No Convertable Formats"))
                        else {
                            val tmpConvertTo = format.conversions.first()
                            val output = File(subfile.absolutePath.replace(".${format.extension ?: subfile.extension}", "") + ".${tmpConvertTo.extension ?: "unk"}").run {
                                if (exists())
                                    return@run File(this.absolutePath.replaceLast(".${tmpConvertTo.extension ?: "unk"}", "-${UUID.randomUUID()}.${tmpConvertTo.extension ?: "unk"}"))
                                return@run this
                            }
                            try {
                                FileOutputStream(output).use { out -> format.convert(tmpConvertTo, FileDataSource(subfile), out, formatParams) }
                                rows.add(arrayOf(file.name + subfile.absolutePath.replace(file.absolutePath, ""), file.name + output.absolutePath.replace(file.absolutePath, ""), format.name, tmpConvertTo.name))
                            } catch (iea: IllegalArgumentException) {
                                rows.add(arrayOf(file.name + subfile.absolutePath.replace(file.absolutePath, ""), "N/a", format.name, "Could not convert to ${tmpConvertTo.name}: ${iea.localizedMessage}"))
                            } finally {
                                if (output.length() == 0L)
                                    output.delete()
                            }
                        }
                    } else {
                        if (format.canConvert(convertTo)) {
                            val output = File(subfile.absolutePath.replace(".${format.extension ?: subfile.extension}", "") + ".${convertTo.extension ?: "unk"}").run {
                                if (exists())
                                    return@run File(this.absolutePath.replaceLast(".${convertTo.extension ?: "unk"}", "-${UUID.randomUUID()}.${convertTo.extension ?: "unk"}"))
                                return@run this
                            }

                            try {
                                FileOutputStream(output).use { out -> format.convert(convertTo, FileDataSource(subfile), out, formatParams) }
                                rows.add(arrayOf(file.name + subfile.absolutePath.replace(file.absolutePath, ""), file.name + output.absolutePath.replace(file.absolutePath, ""), format.name, convertTo.name))
                            } catch (iea: IllegalArgumentException) {
                                rows.add(arrayOf(file.name + subfile.absolutePath.replace(file.absolutePath, ""), "N/a", format.name, "Could not convert to ${convertTo.name}: ${iea.localizedMessage}"))
                            } finally {
                                if (output.length() == 0L)
                                    output.delete()
                            }
                        } else
                            rows.add(arrayOf(file.name + subfile.absolutePath.replace(file.absolutePath, ""), "N/a", "${format.name} cannot be converted to ${convertTo.name}", "N/a"))
                    }
                }
            }
        }
        println(FlipTable.of(arrayOf("File", "Output File", "Old Format", "New Format"), rows.toTypedArray()))
    }
    val convert = Command("convert") { (params, str) ->
        if (params.size == 1)
            return@Command errPrintln("Error: No file or directory provided")

        if (params.size == 3)
            return@Command identifyAndConvert.command(InstanceOrder("Redirected", null, str))

        val file = File(params[1])
        val convertFrom: SpiralFormat = if (params.size == 2) return@Command errPrintln("Error: No format to convert from provided") else SpiralFormats.formatForName(params[2]) ?: SpiralFormats.formatForExtension(params[2]) ?: return@Command errPrintln("Error: No format known by name or extension ${params[2]}")
        val convertTo: SpiralFormat = SpiralFormats.formatForName(params[3]) ?: SpiralFormats.formatForExtension(params[3]) ?: return@Command errPrintln("Error: No format known by name or extension ${params[3]}")
        val formatParams: Map<String, String> = if (params.size == 4) emptyMap() else params.copyFrom(4).map { it.split('=', limit = 2).takeIf { it.size == 2 }?.run { this[0] to this[1] } }.filterNotNull().toMap()

        val rows = ArrayList<Array<String>>()
        if (file.isFile) {
            val data = FileDataSource(file)
            if (!convertFrom.isFormat(data))
                rows.add(arrayOf(file.path, "N/a", "File is not of type ${convertFrom.name}", "N/a"))
            else {
                if (convertFrom.canConvert(convertTo)) {
                    val output = File(file.absolutePath.replace(".${convertFrom.extension ?: file.extension}", "") + ".${convertTo.extension ?: "unk"}").ensureUnique()

                    try {
                        FileOutputStream(output).use { out -> convertFrom.convert(convertTo, data, out, formatParams) }
                        rows.add(arrayOf(file.path, output.path, convertFrom.name, convertTo.name))
                    } catch (iea: IllegalArgumentException) {
                        rows.add(arrayOf(file.path, "N/a", convertFrom.name, "Could not convert to ${convertTo.name}: ${iea.localizedMessage}"))
                    } finally {
                        if (output.length() == 0L)
                            output.delete()
                    }
                } else
                    rows.add(arrayOf(file.path, "N/a", "${convertFrom.name} cannot be converted to ${convertTo.name}", "N/a"))
            }
        } else if (file.isDirectory) {
            file.iterate(filters = ignoreFilters).forEach dirIteration@ { subfile ->
                val data = FileDataSource(subfile)
                if (!convertFrom.isFormat(data))
                    rows.add(arrayOf(file.path, "N/a", "File is not of type ${convertFrom.name}", "N/a"))
                else {
                    if (convertFrom.canConvert(convertTo)) {
                        val output = File(subfile.absolutePath.replace(".${convertFrom.extension ?: subfile.extension}", "") + ".${convertTo.extension ?: "unk"}").ensureUnique()

                        try {
                            FileOutputStream(output).use { out -> convertFrom.convert(convertTo, data, out, formatParams) }
                            rows.add(arrayOf(file.name + subfile.absolutePath.replace(file.absolutePath, ""), file.name + output.absolutePath.replace(file.absolutePath, ""), convertFrom.name, convertTo.name))
                        } catch (iea: IllegalArgumentException) {
                            rows.add(arrayOf(file.name + subfile.absolutePath.replace(file.absolutePath, ""), "N/a", convertFrom.name, "Could not convert to ${convertTo.name}: ${iea.localizedMessage}"))
                        } finally {
                            if (output.length() == 0L)
                                output.delete()
                        }
                    } else
                        rows.add(arrayOf(file.name + subfile.absolutePath.replace(file.absolutePath, ""), "N/a", "${convertFrom.name} cannot be converted to ${convertTo.name}", "N/a"))
                }
            }
        }
        println(FlipTable.of(arrayOf("File", "Output File", "Old Format", "New Format"), rows.toTypedArray()))
    }

    val squish = Command("squish") { (params) ->
        if (params.size == 1)
            return@Command errPrintln("Error: No file or directory provided")

        val file = File(params[1])
        val croppedCanvas = if (params.size < 4) true else params[3].toBoolean()

        if (file.isFile) {
            val ds = FileDataSource(file)
            val format: SpiralImageFormat = (if (params.size < 3) SpiralFormats.formatForData(ds, SpiralFormats.imageFormats) else SpiralFormats.formatForName(params[2], SpiralFormats.imageFormats) ?: SpiralFormats.formatForExtension(params[2], SpiralFormats.imageFormats) ?: SpiralFormats.formatForData(ds, SpiralFormats.imageFormats))
                    as? SpiralImageFormat ?: return@Command errPrintln("Error: No image format could be found for the provided parameter or for the data.")

            val full = format.toBufferedImage(ds)

            val topHalf = full.getSubimage(0, 0, full.width, full.height / 2)
            val half = full.getSubimage(0, full.height / 2, full.width, full.height / 2)
            val squish = BufferedImage(full.width, half.height / 2, BufferedImage.TYPE_INT_ARGB)
            var g = squish.createGraphics()
            g.drawImage(half, AffineTransform.getScaleInstance(1.0, 0.5), null)
            g.dispose()

            val squishied: BufferedImage

            if(croppedCanvas) {
                squishied = BufferedImage(full.width, topHalf.height + squish.height, BufferedImage.TYPE_INT_ARGB)
                g = squishied.createGraphics()
                g.drawImage(topHalf, 0, 0, null)
                g.drawImage(squish, 0, topHalf.height, null)
                g.dispose()
            } else {
                squishied = BufferedImage(full.width, full.height, BufferedImage.TYPE_INT_ARGB)
                g = squishied.createGraphics()
                g.drawImage(topHalf, 0, squish.height, null)
                g.drawImage(squish, 0, squish.height + topHalf.height, null)
                g.dispose()
            }

            //ImageIO.write(squishied, "PNG", File("$name-squish.png"))
            val output = File(file.absolutePath.substringBeforeLast('.') + "-squished.${file.extension}")
            FileOutputStream(output).use { stream -> PNGFormat.convert(format, squishied, stream, emptyMap()) }

            println("Squished $file into $output")
        } else if (file.isDirectory) {
            val rows: MutableList<Array<String>> = ArrayList()

            file.iterate().filter { it.isFile }.forEach { subfile ->
                val ds = FileDataSource(subfile)
                val format: SpiralImageFormat = (if (params.size < 3) SpiralFormats.formatForData(ds, SpiralFormats.imageFormats) else SpiralFormats.formatForName(params[2], SpiralFormats.imageFormats) ?: SpiralFormats.formatForExtension(params[2], SpiralFormats.imageFormats) ?: SpiralFormats.formatForData(ds, SpiralFormats.imageFormats))
                        as? SpiralImageFormat ?: run {
                    rows.add(arrayOf(subfile relativePathFrom file, "ERR: No format"))
                    return@forEach
                }

                val full = format.toBufferedImage(ds)

                val topHalf = full.getSubimage(0, 0, full.width, full.height / 2)
                val half = full.getSubimage(0, full.height / 2, full.width, full.height / 2)
                val squish = BufferedImage(full.width, half.height / 2, BufferedImage.TYPE_INT_ARGB)
                var g = squish.createGraphics()
                g.drawImage(half, AffineTransform.getScaleInstance(1.0, 0.5), null)
                g.dispose()



                val squishied: BufferedImage

                if(croppedCanvas) {
                    squishied = BufferedImage(full.width, topHalf.height + squish.height, BufferedImage.TYPE_INT_ARGB)
                    g = squishied.createGraphics()
                    g.drawImage(topHalf, 0, 0, null)
                    g.drawImage(squish, 0, topHalf.height, null)
                    g.dispose()
                } else {
                    squishied = BufferedImage(full.width, full.height, BufferedImage.TYPE_INT_ARGB)
                    g = squishied.createGraphics()
                    g.drawImage(topHalf, 0, squish.height, null)
                    g.drawImage(squish, 0, squish.height + topHalf.height, null)
                    g.dispose()
                }

                //ImageIO.write(squishied, "PNG", File("$name-squish.png"))
                val output = File(subfile.absolutePath.substringBeforeLast('.') + "-squished.${subfile.extension}")
                FileOutputStream(output).use { stream -> PNGFormat.convert(format, squishied, stream, emptyMap()) }

                rows.add(arrayOf(subfile relativePathFrom file, output relativePathFrom file))
            }

            println(FlipTable.of(arrayOf("Original", "Squished"), rows.toTypedArray()))
        }
    }

    val squishOverride = Command("squish_override") { (params) ->
        if (params.size == 1)
            return@Command errPrintln("Error: No file or directory provided")

        val file = File(params[1])
        val croppedCanvas = if (params.size < 4) true else params[3].toBoolean()

        if (file.isFile) {
            val ds = FileDataSource(file)
            val format: SpiralImageFormat = (if (params.size < 3) SpiralFormats.formatForData(ds, SpiralFormats.imageFormats) else SpiralFormats.formatForName(params[2], SpiralFormats.imageFormats) ?: SpiralFormats.formatForExtension(params[2], SpiralFormats.imageFormats) ?: SpiralFormats.formatForData(ds, SpiralFormats.imageFormats))
                    as? SpiralImageFormat ?: return@Command errPrintln("Error: No image format could be found for the provided parameter or for the data.")

            val full = format.toBufferedImage(ds)

            val topHalf = full.getSubimage(0, 0, full.width, full.height / 2)
            val half = full.getSubimage(0, full.height / 2, full.width, full.height / 2)
            val squish = BufferedImage(full.width, half.height / 2, BufferedImage.TYPE_INT_ARGB)
            var g = squish.createGraphics()
            g.drawImage(half, AffineTransform.getScaleInstance(1.0, 0.5), null)
            g.dispose()

            val squishied: BufferedImage

            if(croppedCanvas) {
                squishied = BufferedImage(full.width, topHalf.height + squish.height, BufferedImage.TYPE_INT_ARGB)
                g = squishied.createGraphics()
                g.drawImage(topHalf, 0, 0, null)
                g.drawImage(squish, 0, topHalf.height, null)
                g.dispose()
            } else {
                squishied = BufferedImage(full.width, full.height, BufferedImage.TYPE_INT_ARGB)
                g = squishied.createGraphics()
                g.drawImage(topHalf, 0, squish.height, null)
                g.drawImage(squish, 0, squish.height + topHalf.height, null)
                g.dispose()
            }

            //ImageIO.write(squishied, "PNG", File("$name-squish.png"))
            FileOutputStream(file).use { stream -> PNGFormat.convert(format, squishied, stream, emptyMap()) }

            println("Squished $file")
        } else if (file.isDirectory) {
            val rows: MutableList<Array<String>> = ArrayList()

            file.iterate().filter { it.isFile }.forEach { subfile ->
                val ds = FileDataSource(subfile)
                val format: SpiralImageFormat = (if (params.size < 3) SpiralFormats.formatForData(ds, SpiralFormats.imageFormats) else SpiralFormats.formatForName(params[2], SpiralFormats.imageFormats) ?: SpiralFormats.formatForExtension(params[2], SpiralFormats.imageFormats) ?: SpiralFormats.formatForData(ds, SpiralFormats.imageFormats))
                        as? SpiralImageFormat ?: run {
                    rows.add(arrayOf(subfile relativePathFrom file, "ERR: No format"))
                    return@forEach
                }

                val full = format.toBufferedImage(ds)

                val topHalf = full.getSubimage(0, 0, full.width, full.height / 2)
                val half = full.getSubimage(0, full.height / 2, full.width, full.height / 2)
                val squish = BufferedImage(full.width, half.height / 2, BufferedImage.TYPE_INT_ARGB)
                var g = squish.createGraphics()
                g.drawImage(half, AffineTransform.getScaleInstance(1.0, 0.5), null)
                g.dispose()

                val squishied: BufferedImage

                if(croppedCanvas) {
                    squishied = BufferedImage(full.width, topHalf.height + squish.height, BufferedImage.TYPE_INT_ARGB)
                    g = squishied.createGraphics()
                    g.drawImage(topHalf, 0, 0, null)
                    g.drawImage(squish, 0, topHalf.height, null)
                    g.dispose()
                } else {
                    squishied = BufferedImage(full.width, full.height, BufferedImage.TYPE_INT_ARGB)
                    g = squishied.createGraphics()
                    g.drawImage(topHalf, 0, squish.height, null)
                    g.drawImage(squish, 0, squish.height + topHalf.height, null)
                    g.dispose()
                }

                //ImageIO.write(squishied, "PNG", File("$name-squish.png"))
                FileOutputStream(subfile).use { stream -> PNGFormat.convert(format, squishied, stream, emptyMap()) }

                rows.add(arrayOf(subfile relativePathFrom file, subfile relativePathFrom file))
            }

            println(FlipTable.of(arrayOf("Original", "Squished"), rows.toTypedArray()))
        }
    }

    val join = Command("join") { (params) ->
        if (!MediaWrapper.ffmpeg.isInstalled)
            return@Command errPrintln("Error: ffmpeg is not installed")

        if (params.size == 1)
            return@Command errPrintln("Error: No file or directory provided")

        if (params.size == 2) {
            val directory = File(params[1])

            if (!directory.exists())
                return@Command errPrintln("Error: Directory does not exist")
            else if (!directory.isDirectory)
                return@Command errPrintln("Error: Provided directory was not, in fact, a directory")

            val entries = ArrayList<Array<String>>()

            val files = directory.listFiles().filter { file -> ignoreFilters.all { filter -> filter.accept(file) } }
            files.map { it.nameWithoutExtension }.group().values.sortedBy { it.firstOrNull() ?: "" }.forEach { names ->
                if (names.size < 2) {
                    entries.add(arrayOf(names.firstOrNull() ?: "None", "", "", "", " < 2 files for provided name"))
                    return@forEach
                }
                val name = names.first()
                val audio = files.filter { it.nameWithoutExtension == name }.firstOrNull { file ->
                    (SpiralFormats.formatForExtension(file.extension, SpiralFormats.audioFormats) ?: SpiralFormats.formatForData(FileDataSource(file), SpiralFormats.audioFormats)) != null
                } ?: run {
                    entries.add(arrayOf(name, "", "", "", "No audio file for provided name"))
                    return@forEach
                }

                val video = files.filter { it.nameWithoutExtension == name }.firstOrNull { file ->
                    (SpiralFormats.formatForExtension(file.extension, SpiralFormats.videoFormats) ?: SpiralFormats.formatForData(FileDataSource(file), SpiralFormats.videoFormats)) != null
                } ?: run {
                    entries.add(arrayOf(name, audio.name, "", "", "No video file for provided name"))
                    return@forEach
                }

                debug("Joining ${audio.name} and ${video.name}")

                val output = File(directory, "$name.mp4")

                try {
                    MediaWrapper.ffmpeg.join(audio, video, output)
                } finally {
                    if (output.exists()) {
                        if (output.length() > 16) {
                            if (MP4Format.isFormat(FileDataSource(output)))
                                entries.add(arrayOf(name, audio.name, video.name, output.name, ""))
                            else
                                entries.add(arrayOf(name, audio.name, video.name, output.name, "Output is not an MP4 file"))
                        } else {
                            output.delete()
                            entries.add(arrayOf(name, audio.name, video.name, "", "Output was empty"))
                        }
                    } else
                        entries.add(arrayOf(name, audio.name, video.name, "", "Output does not exist!"))
                }
            }

            println(FlipTable.of(arrayOf("Name", "Audio File", "Video File", "Output File", "Error"), entries.toTypedArray()))
        }
    }

    val reloadConfig = Command("reload_config") {
        SpiralModel.load()
        println("Reloaded config.")
    }

    val versionCommand = Command("version") { println("SPIRAL version $version") }
    val whereAmI = Command("whereami") { println("You are here: ${Gurren::class.java.protectionDomain.codeSource.location}\nAnd you are: ${Gurren::class.java.protectionDomain.codeSource.location.openStream().use { it.toString() }}") }
    val jenkinsBuild = Command("build") {
        val (_, response, r) = Fuel.get("https://jenkins-ci.abimon.org/fingerprint/$version/api/json").userAgent().responseString()

        if (response.httpStatusCode != 200)
            println("Error retrieving the jenkins build; status code ${response.httpStatusCode}")
        else
            println("SPIRAL version $version; Jenkins build ${(SpiralData.MAPPER.readValue(r.component1(), Map::class.java)["original"] as? Map<*, *> ?: emptyMap<String, String>())["number"] as? Int ?: -1}")
    }

    val checkForUpdates = Command("check_for_update") {
        val currentBuild = this.currentBuild

        if (currentBuild == -1) {
            println("Error retrieving current build")
            return@Command
        }

        val latestBuild = this.latestBuild

        if (latestBuild == -1)
            println("Error retrieving latest build")
        else {
            when {
                currentBuild == latestBuild -> println("SPIRAL version $version; build $currentBuild - You are on the latest build")
                currentBuild < latestBuild -> println("SPIRAL version $version; build $currentBuild - Latest build is $latestBuild\nIf you would like to update, run \"download_latest\"")
                else -> println("SPIRAL version $version; build $currentBuild - Apparent latest build is $latestBuild, however that build precedes this one.")
            }
        }
    }

    val downloadLatest = Command("download_latest") {
        val (_, headResponse, _) = Fuel.head("https://jenkins-ci.abimon.org/job/KSPIRAL/lastSuccessfulBuild/artifact/build/libs/KSPIRAL-all.jar").response()

        if(headResponse.httpStatusCode != 200)
            return@Command errPrintln("Error retrieving latest update: ${headResponse.httpStatusCode}")

        val latestBuild = this.latestBuild

        println("SPIRAL build $currentBuild -> build $latestBuild")
        println("Update Size: ${headResponse.httpContentLength} B / ${GurrenPlugins.TWO_DECIMAL_PLACES.format(headResponse.httpContentLength / 1000.0 / 1000.0)} MB")

        if (question("Do you wish to continue downloading this plugin (Y/n)? ", "Y")) {
            val destination = File("SPIRAL-$latestBuild.jar")

            val (_, response, _) = Fuel.download("https://jenkins-ci.abimon.org/job/KSPIRAL/lastSuccessfulBuild/artifact/build/libs/KSPIRAL-all.jar").progress { readBytes, totalBytes ->
                println("Downloaded ${GurrenPlugins.TWO_DECIMAL_PLACES.format(readBytes * 100.0 / totalBytes.toDouble())}%")
            }.destination { response, url -> destination }.responseStream()

            if(response.httpStatusCode == 200)
                println("Successfully downloaded update to $destination")
            else
                errPrintln("Error: Was unable to download SPIRAL build $latestBuild")
        }
    }

    //val toggleDebug = Command("toggle_debug") { SpiralModel.isDebug = !SpiralModel.isDebug; println("Debug status is now ${SpiralModel.isDebug}"); SpiralModel.save() }
    val exit = Command("exit", "default") { println("Bye!"); keepLooping = false }

    val currentBuild: Int
        get() {
            val (_, response, r) = Fuel.get("https://jenkins-ci.abimon.org/fingerprint/$version/api/json").userAgent().responseString()

            if (response.httpStatusCode != 200)
                return -1
            else
                return (SpiralData.MAPPER.readValue(r.component1(), Map::class.java)["original"] as? Map<*, *> ?: emptyMap<String, String>())["number"] as? Int ?: -1
        }

    val latestBuild: Int
        get() {
            val (_, response, r) = Fuel.get("https://jenkins-ci.abimon.org/job/KSPIRAL/api/json").userAgent().responseString()

            if (response.httpStatusCode != 200)
                return -1
            else
                return (SpiralData.MAPPER.readValue(r.component1(), Map::class.java)["lastSuccessfulBuild"] as? Map<*, *> ?: emptyMap<String, String>())["number"] as? Int ?: -1
        }
}