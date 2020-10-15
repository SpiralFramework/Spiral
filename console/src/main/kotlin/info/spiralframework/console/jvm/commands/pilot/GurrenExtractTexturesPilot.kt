package info.spiralframework.console.jvm.commands.pilot

import dev.brella.knolus.context.KnolusContext
import dev.brella.knolus.modules.functionregistry.registerFunctionWithContextWithoutReturn
import dev.brella.knolus.objectTypeParameter
import dev.brella.knolus.stringTypeParameter
import dev.brella.knolus.types.KnolusArray
import dev.brella.knolus.types.KnolusTypedValue
import dev.brella.knolus.types.asString
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.Optional
import dev.brella.kornea.errors.common.doOnSuccess
import dev.brella.kornea.errors.common.filterNotNull
import dev.brella.kornea.errors.common.flatMap
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.errors.common.getOrElseRun
import dev.brella.kornea.errors.common.switchIfEmpty
import dev.brella.kornea.img.DXT1PixelData
import dev.brella.kornea.img.bc7.BC7PixelData
import dev.brella.kornea.img.createPngImage
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.BinaryInputFlow
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.WindowedInputFlow
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.flow.readBytes
import dev.brella.kornea.io.common.useInputFlow
import dev.brella.kornea.io.jvm.files.AsyncFileDataSource
import dev.brella.kornea.toolkit.common.use
import dev.brella.kornea.toolkit.coroutines.ascii.arbitraryProgressBar
import info.spiralframework.base.binding.prompt
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.cache
import info.spiralframework.base.common.locale.constNull
import info.spiralframework.base.common.locale.printLocale
import info.spiralframework.base.common.locale.printlnLocale
import info.spiralframework.base.common.logging.error
import info.spiralframework.base.common.logging.trace
import info.spiralframework.console.jvm.commands.CommandRegistrar
import info.spiralframework.console.jvm.commands.shared.GurrenShared
import info.spiralframework.console.jvm.pipeline.DataSourceType
import info.spiralframework.console.jvm.pipeline.spiralContext
import info.spiralframework.core.decompress
import info.spiralframework.core.formats.DefaultFormatReadContext
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.SpiralFormat
import info.spiralframework.formats.common.archives.SpiralArchive
import info.spiralframework.formats.common.archives.getSubfiles
import info.spiralframework.formats.common.archives.srd.SrdArchive
import info.spiralframework.formats.common.archives.srd.TextureSrdEntry
import info.spiralframework.formats.jvm.archives.FolderArchive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object GurrenExtractTexturesPilot: CommandRegistrar {
    suspend fun SpiralContext.extractTexturesStub(knolusContext: KnolusContext, source: KnolusTypedValue, destDir: String) {
        val destination = File(destDir)

        if (destination.exists() && !destination.isDirectory) {
            printlnLocale("error.file.not_dir", destination)
            return
        }

        extractTextures(knolusContext, source, destination)
    }

    suspend fun SpiralContext.extractTextures(knolusContext: KnolusContext, source: KnolusTypedValue, destination: File) {
        if (source is KnolusArray<*>) {
            source.array.forEach { entry ->
                extractTextures(knolusContext, entry, destination)
            }
        } else if (source is DataSourceType) {
            source.inner.use { ds -> extractTextures(ds, destination) }
        } else {
            source.asString(knolusContext).doOnSuccess { sourcePath ->
                val file = File(sourcePath)

                if (file.isDirectory) {
                    return extractTextures(FolderArchive(file), destination)
                } else if (file.isFile) {
                    return AsyncFileDataSource(file).use { ds -> extractTextures(ds, destination) }
                } else {
                    printlnLocale("commands.pilot.extract_textures.err_path_not_file_or_directory", sourcePath)
                    return
                }
            }
        }
    }

    suspend fun SpiralContext.extractTextures(archiveDataSource: DataSource<*>, destination: File) {
        val (decompressedDataSource, archiveCompressionFormats) =
            if (archiveDataSource.reproducibility.isUnreliable() || archiveDataSource.reproducibility.isUnstable()) {
                archiveDataSource.cache(this).use { ds -> decompress(ds) }
            } else {
                decompress(archiveDataSource)
            }

        val readContext = DefaultFormatReadContext(decompressedDataSource.location?.replace("$(.+?)(?:\\+[0-9a-fA-F]+h|\\[[0-9a-fA-F]+h,\\s*[0-9a-fA-F]+h\\])".toRegex()) { result -> result.groupValues[1] }, GurrenPilot.game)
        val result = arbitraryProgressBar(loadingText = localise("commands.pilot.extract_textures.analysing_archive"), loadedText = null) {
            GurrenShared.EXTRACTABLE_ARCHIVES.map { archive -> archive.identify(this, readContext, decompressedDataSource) }
                .also { results ->
                    trace {
                        trace("\rResults for \"{0}\":", archiveDataSource.location ?: constNull())
                        results.forEachIndexed { index, result ->
                            trace("\t{0}] == {1} ==", GurrenShared.EXTRACTABLE_ARCHIVES[index].name, result)
                        }
                    }
                }
                .filterIsInstance<FormatResult<Optional<SpiralArchive>, SpiralArchive>>()
                .sortedBy(FormatResult<*, *>::confidence)
                .asReversed()
                .firstOrNull()
        }

        println()

        if (result == null) {
            printlnLocale("commands.pilot.extract_textures.err_no_format_for", archiveDataSource.location ?: constNull())
            return
        }

        val archive: SpiralArchive = result.get()
            .filterNotNull()
            .getOrElseRun {
                result.format().read(this, readContext, decompressedDataSource)
                    .filterNotNull()
                    .getOrBreak {
                        printlnLocale("commands.pilot.extract_textures.err_no_format_for", archiveDataSource.location ?: constNull())
                        return
                    }
            }

        if (archiveCompressionFormats?.isNotEmpty() != true) {
            printLocale("commands.pilot.extract_textures.archive_type", result.format().name)
        } else {
            printLocale("commands.pilot.extract_textures.compressed_archive_type", archiveCompressionFormats.joinToString(" > ", transform = SpiralFormat::name), result.format().name)
        }

        extractTextures(archive, destination)
    }

    suspend fun SpiralContext.extractTextures(archive: SpiralArchive, destination: File) {
        println("Identifying texture sources...")

        val textureSourceNames = archive.getSubfiles(this)
            .toList()
            .groupBy { entry -> entry.path.substringBeforeLast('.') }
            .filterValues { list -> list.any { subfile -> subfile.path.endsWith(".srd") } && list.any { subfile -> subfile.path.endsWith(".srdv") } }
            .mapValues { (_, list) -> Pair(list.first { subfile -> subfile.path.endsWith(".srd") }, list.first { subfile -> subfile.path.endsWith(".srdv") }) }

        println("Found: ${textureSourceNames.keys.joinToString()}")

        textureSourceNames.values.forEach { (srdEntry, srdvEntry) ->
            val srdFile = SrdArchive(srdEntry.dataSource)
                .getOrBreak { return@forEach }

            val textureEntries = srdFile.entries.filterIsInstance<TextureSrdEntry>()

            val outputDir = File(destination, srdEntry.path.substringBeforeLast('.'))
            outputDir.mkdirs()

            textureEntries.forEach { textureEntry ->
                srdvEntry.dataSource.useInputFlow { srdvFlow ->
                    val textureFlow = WindowedInputFlow(srdvFlow, textureEntry.rsiEntry.resources[0].start.toULong(), textureEntry.rsiEntry.resources[0].length.toULong())

                    val swizzled = textureEntry.swizzle and 1 != 1
                    if (textureEntry.format in arrayOf(0x01, 0x02, 0x05, 0x1A)) {
                        val bytespp: Int

                        when (textureEntry.format) {
                            0x01 -> bytespp = 4
                            0x02 -> bytespp = 2
                            0x05 -> bytespp = 2
                            0x1A -> bytespp = 4
                            else -> bytespp = 2
                        }

                        val width: Int = textureEntry.displayWidth //(scanline / bytespp).toInt()
                        val height: Int = textureEntry.displayHeight

                        val processing: InputFlow

                        if (swizzled) {
                            val processingData = textureFlow.readBytes()
                            println("ERR: DATA SWIZZLED")
//                                processingData.deswizzle(width / 4, height / 4, bytespp)
                            processing = BinaryInputFlow(processingData)
                        } else
                            processing = textureFlow

                        when (textureEntry.format) {
                            0x01 -> {
                                val resultingImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                                val rgbData = IntArray(height * width) { requireNotNull(processing.readInt32LE()) }
                                resultingImage.setRGB(0, 0, width, height, rgbData, 0, width)

//                                    for (y in 0 until height) {
//                                        for (x in 0 until width) {
//                                            //                                                    val b = processing.read()
//                                            //                                                    val g = processing.read()
//                                            //                                                    val r = processing.read()
//                                            //                                                    val a = processing.read()
//                                            val bgra = processing.readInt32LE()
//
//
//                                            resultingImage.setRGB(x, y, bgra ?: break)
//                                        }
//                                    }

                                return@useInputFlow resultingImage
                            }
                            else -> return@useInputFlow null
                        }
                    } else if (textureEntry.format in arrayOf(0x0F, 0x11, 0x14, 0x16, 0x1C)) {
                        val bytespp: Int

                        when (textureEntry.format) {
                            0x0F -> bytespp = 8
                            0x1C -> bytespp = 16
                            else -> bytespp = 8
                        }

                        var width: Int = textureEntry.displayWidth
                        var height: Int = textureEntry.displayHeight

                        if (width % 4 != 0)
                            width += 4 - (width % 4)

                        if (height % 4 != 0)
                            height += 4 - (height % 4)

                        val processingFlow: InputFlow

                        if (swizzled && width >= 4 && height >= 4) {
                            val processingData = textureFlow.readBytes()
//                                processingData.deswizzle(width / 4, height / 4, bytespp)
                            println("ERR: DATA SWIZZLED")
                            processingFlow = BinaryInputFlow(processingData)
                        } else
                            processingFlow = BinaryInputFlow(textureFlow.readBytes())

                        when (textureEntry.format) {
                            0x0F -> return@useInputFlow DXT1PixelData.read(width, height, processingFlow).createPngImage()
                            0x16 -> return@useInputFlow null
                            //                                        0x16 -> return BC4PixelData.read(width, height, processingFlow)
                            0x1C -> return@useInputFlow BC7PixelData.read(width, height, processingFlow).createPngImage()
                            else -> return@useInputFlow null
                        }
                    } else {
                        return@useInputFlow null
                    }
                }.filterNotNull().doOnSuccess { texture ->
                    withContext(Dispatchers.IO) {
                        ImageIO.write(texture, "PNG", File(outputDir, textureEntry.rsiEntry.name))
                    }
                }
            }
        }
    }

    override suspend fun register(spiralContext: SpiralContext, knolusContext: KnolusContext) {
        with (knolusContext) {
            registerFunctionWithContextWithoutReturn("extract_textures", objectTypeParameter("file_path").asOptional(), stringTypeParameter("dest_dir").asOptional()) { context, filePath, destDir ->
                val spiralContext = context.spiralContext().getOrBreak { return@registerFunctionWithContextWithoutReturn }

                val filePath = filePath.getOrBreak { failure ->
                    spiralContext.printlnLocale("commands.pilot.extract_textures.err_no_file")
                    if (failure is KorneaResult.WithException<*>)
                        spiralContext.error("commands.pilot.extract_textures.err_no_file", failure.exception)
                    else
                        spiralContext.error("commands.pilot.extract_textures.err_no_file", failure)

                    return@registerFunctionWithContextWithoutReturn
                }

                val destination: String = destDir.switchIfEmpty {
                    filePath.asString(context).flatMap { str ->
                        val file = File(str)
                        if (file.exists()) {
                            if (spiralContext.prompt("commands.pilot.extract_textures.prompt_auto_dest")) {
                                return@flatMap KorneaResult.success(file.absolutePath.substringBeforeLast('.'))
                            }
                        }

                        return@flatMap KorneaResult.empty<String>()
                    }
                }.getOrBreak { failure ->
                    spiralContext.printlnLocale("commands.pilot.extract_textures.err_no_dest_dir")
                    if (failure is KorneaResult.WithException<*>)
                        spiralContext.error("commands.pilot.extract_textures.err_no_dest_dir", failure.exception)
                    else
                        spiralContext.error("commands.pilot.extract_textures.err_no_dest_dir", failure)

                    return@registerFunctionWithContextWithoutReturn
                }

                spiralContext.extractTexturesStub(knolusContext, filePath, destination)
            }
        }

        GurrenPilot.help("extract_textures")
    }
}