package org.abimon.spiral.mvc.gurren

import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.core.data.PatchOperation
import org.abimon.spiral.core.formats.archives.SPCFormat
import org.abimon.spiral.core.formats.archives.ZIPFormat
import org.abimon.spiral.core.formats.images.PNGFormat
import org.abimon.spiral.core.objects.archives.CustomSPC
import org.abimon.spiral.core.objects.archives.SPC
import org.abimon.spiral.core.objects.images.CustomSRD
import org.abimon.spiral.mvc.SpiralModel
import org.abimon.spiral.mvc.SpiralModel.Command
import org.abimon.spiral.util.InputStreamFuncDataSource
import org.abimon.visi.collections.joinToPrefixedString
import org.abimon.visi.io.errPrintln
import org.abimon.visi.lang.child
import org.abimon.visi.lang.make
import org.abimon.visi.util.zip.forEach
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.imageio.ImageIO

@Suppress("unused")
object GurrenPatching {
    val patchingName: String
        get() = SpiralModel.patchFile?.nameWithoutExtension ?: ""

    val patchOperation: PatchOperation
        get() = SpiralModel.patchOperation ?: throw noPatchError

    val patchFile: File
        get() = SpiralModel.patchFile ?: throw noPatchError

    val noPatchError: IllegalStateException
        get() = IllegalStateException("Attempt to perform a patch command while the operation or patching file is null, this is a bug!")

    val merge = Command("merge", "patch") { (params) ->
        if (patchOperation != PatchOperation.SRD)
            return@Command errPrintln("[$patchingName] Error: Patch Operation $patchOperation is unable to perform the 'merge' function. This is only supported for SRD operations")

        if (params.size == 1)
            return@Command errPrintln("[$patchingName] Error: No file to merge.")

        val file = File(params[1])

        if (file.exists()) {
            val format = SpiralFormats.formatForExtension(file.extension, SpiralFormats.imageFormats)
                    ?: SpiralFormats.formatForData(Gurren.game, file::inputStream, file.name, SpiralFormats.imageFormats)
                    ?: return@Command errPrintln("Error: No image format recognised for $file")
            val img: BufferedImage

            if (format == PNGFormat)
                img = ImageIO.read(file)
            else {
                val (convOut, convIn) = CacheHandler.cacheStream()
                format.convert(Gurren.game, PNGFormat, file.name, file::inputStream, convOut, emptyMap())
                img = convIn().use { stream -> ImageIO.read(stream) }
            }

            var srd: (() -> InputStream)? = null
            var srdv: (() -> InputStream)? = null

            var srdName: String? = null
            var srdvName: String? = null

            if (ZIPFormat.isFormat(Gurren.game, patchFile.name, patchFile::inputStream)) {
                val zip = ZipFile(patchFile)
                val entries = zip.entries().toList()

                val srdFiles = entries.filter { entry -> entry.name.endsWith(".srd") }
                val srdvFiles = entries.filter { entry -> entry.name.endsWith(".srdv") }

                if (srdFiles.isEmpty())
                    return@Command errPrintln("[$patchingName] $patchFile does not contain any SRD files")

                if (srdvFiles.isEmpty())
                    return@Command errPrintln("[$patchingName] $patchFile does not contain any SRDV files")

                if (srdFiles.size > 1) {
                    println("[$patchingName] $patchFile contains multiple SRD files. Please select one from the following list: ")

                    println(srdFiles.joinToPrefixedString("\n", "\t[$patchingName] ") { name })

                    while (true) {
                        print("[$patchingName]|[Choose SRD]|> ")
                        srdName = readLine() ?: break
                        if (srdName == "exit")
                            return@Command

                        val srdEntry = srdFiles.firstOrNull { entry -> entry.name == srdName }
                        if (srdEntry == null)
                            println("Invalid srd file $srdName")
                        else {
                            val (srdOut, srdIn) = CacheHandler.cacheStream()

                            zip.getInputStream(srdEntry).use { stream -> srdOut.use { out -> stream.copyTo(out) } }

                            srd = srdIn

                            break
                        }
                    }
                } else {
                    val (srdOut, srdIn) = CacheHandler.cacheStream()

                    zip.getInputStream(srdFiles.first()).use { stream -> srdOut.use { out -> stream.copyTo(out) } }

                    srd = srdIn
                    srdName = srdFiles.first().name
                }
                if (srdvFiles.size > 1) {
                    println("[$patchingName] $patchFile contains multiple SRDV files. Please select one from the following list: ")

                    println(srdvFiles.joinToPrefixedString("\n", "\t[$patchingName] ") { name })

                    while (true) {
                        print("[$patchingName]|[Choose SRDV]|> ")
                        srdvName = readLine() ?: break
                        if (srdvName == "exit")
                            return@Command

                        val srdvEntry = srdvFiles.firstOrNull { entry -> entry.name == srdvName }
                        if (srdvEntry == null)
                            println("Invalid srdv file $srdvName")
                        else {
                            val (srdvOut, srdvIn) = CacheHandler.cacheStream()

                            zip.getInputStream(srdvEntry).use { stream -> srdvOut.use { out -> stream.copyTo(out) } }

                            srdv = srdvIn

                            break
                        }
                    }
                } else {
                    val (srdvOut, srdvIn) = CacheHandler.cacheStream()

                    zip.getInputStream(srdvFiles.first()).use { stream -> srdvOut.use { out -> stream.copyTo(out) } }

                    srdv = srdvIn
                    srdvName = srdvFiles.first().name
                }
            } else if(SPCFormat.isFormat(Gurren.game, patchFile.name, patchFile::inputStream)) {
                val spc = SPC(InputStreamFuncDataSource(patchFile::inputStream))

                val srdFiles = spc.files.filter { entry -> entry.name.endsWith(".srd") }
                val srdvFiles = spc.files.filter { entry -> entry.name.endsWith(".srdv") }

                if (srdFiles.isEmpty())
                    return@Command errPrintln("[$patchingName] $patchFile does not contain any SRD files")

                if (srdvFiles.isEmpty())
                    return@Command errPrintln("[$patchingName] $patchFile does not contain any SRDV files")

                if (srdFiles.size > 1) {
                    println("[$patchingName] $patchFile contains multiple SRD files. Please select one from the following list: ")

                    println(srdFiles.joinToPrefixedString("\n", "\t[$patchingName] ") { name })

                    while (true) {
                        print("[$patchingName]|[Choose SRD]|> ")
                        srdName = readLine() ?: break
                        if (srdName == "exit")
                            return@Command

                        val srdEntry = srdFiles.firstOrNull { entry -> entry.name == srdName }
                        if (srdEntry == null)
                            println("Invalid srd file $srdName")
                        else {
                            srd = srdEntry::inputStream
                            break
                        }
                    }
                } else {
                    srd = srdFiles.first()::inputStream
                    srdName = srdFiles.first().name
                }
                if (srdvFiles.size > 1) {
                    println("[$patchingName] $patchFile contains multiple SRDV files. Please select one from the following list: ")

                    println(srdvFiles.joinToPrefixedString("\n", "\t[$patchingName] ") { name })

                    while (true) {
                        print("[$patchingName]|[Choose SRDV]|> ")
                        srdvName = readLine() ?: break
                        if (srdvName == "exit")
                            return@Command

                        val srdvEntry = srdvFiles.firstOrNull { entry -> entry.name == srdvName }
                        if (srdvEntry == null)
                            println("Invalid srdv file $srdvName")
                        else {
                            srdv = srdvEntry::inputStream
                            break
                        }
                    }
                } else {
                    srdv = srdvFiles.first()::inputStream
                    srdvName = srdvFiles.first().name
                }
            } else
                throw IllegalStateException("$patchFile is not of a compatible format to be performing these operations")

            val customSRD = CustomSRD(InputStreamFuncDataSource(srd!!), InputStreamFuncDataSource(srdv!!)) //For these to be null something's gone really wrong

            customSRD.image(if(params.size > 2) params[2] else file.name.child, img)

            val (srdOut, srdIn) = CacheHandler.cacheStream()
            val (srdvOut, srdvIn) = CacheHandler.cacheStream()

            customSRD.patch(srdOut, srdvOut)

            if (ZIPFormat.isFormat(Gurren.game, patchFile.name, patchFile::inputStream)) {
                FileInputStream(patchFile).use { patchDataStream ->
                    val (cacheOut, cacheIn) = CacheHandler.cacheStream()
                    ZipOutputStream(cacheOut).use { zipOut ->
                        ZipInputStream(patchDataStream).use { zipIn ->
                            zipIn.forEach { entry ->
                                zipOut.putNextEntry(entry)
                                when {
                                    entry.name == srdName -> srdIn().use { stream -> stream.copyTo(zipOut) }
                                    entry.name == srdvName -> srdvIn().use { stream -> stream.copyTo(zipOut) }
                                    else -> zipIn.copyTo(zipOut)
                                }
                            }
                        }
                    }

                    patchFile.outputStream().use { out -> cacheIn().use { stream -> stream.copyTo(out) } }
                }
            } else if(SPCFormat.isFormat(Gurren.game, patchFile.name, patchFile::inputStream)) {
                val (cacheOut, cacheIn) = CacheHandler.cacheStream()
                val spc = SPC(InputStreamFuncDataSource(patchFile::inputStream))

                val customSPC = make<CustomSPC> {
                    spc.files.forEach { entry ->
                        when {
                            entry.name == srdName -> file(entry.name, InputStreamFuncDataSource(srdIn))
                            entry.name == srdvName -> file(entry.name, InputStreamFuncDataSource(srdvIn))
                            else -> file(entry.name, entry)
                        }
                    }
                }
                cacheOut.use(customSPC::compile) //Cache it temporarily
                FileOutputStream(patchFile).use { out -> cacheIn().use { stream -> stream.copyTo(out) } }
            }
        } else
            errPrintln("[$patchingName] Error: $file does not exist")
    }

    val exit = Command("exit", "patch") { SpiralModel.scope = "> " to "default" }

    val patch = Command("patch", "default") { (params) ->
        if (params.size == 1)
            return@Command errPrintln("Error: No patch operation. Must be one of: ${PatchOperation.values().joinToString("\n\t* ") { it.name.toLowerCase() }}")

        if (params.size == 2)
            return@Command errPrintln("Error: No file to patch")

        val patchOperation = PatchOperation.valueOf(params[1].toUpperCase())

        val file = File(params[2])
        if (file.exists()) {
            val data = { FileInputStream(file) }
            when (patchOperation) {
                PatchOperation.SRD -> {
                    if (!ZIPFormat.isFormat(null, null, data) && !SPCFormat.isFormat(null, null, data))
                        return@Command errPrintln("Error: File type is incompatible for operation $patchOperation (Wanted .zip or .spc, got ${file.extension})")
                }
            }

            SpiralModel.patchOperation = patchOperation
            SpiralModel.patchFile = file
            SpiralModel.scope = "[Patching ${file.nameWithoutExtension}]|> " to "patch"
            println("Now patching ${file.nameWithoutExtension}")
        } else
            errPrintln("Invalid file $file (File does not exist)")
    }
}