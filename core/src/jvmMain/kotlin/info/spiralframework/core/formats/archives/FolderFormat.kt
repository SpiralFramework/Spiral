package info.spiralframework.core.formats.archives

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.success
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.jvm.files.AsyncFileDataSource
import dev.brella.kornea.io.jvm.files.relativePathFrom
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.concurrent.suspendForEach
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.base.jvm.io.files.Folder
import info.spiralframework.core.common.formats.WritableSpiralFormat
import info.spiralframework.core.common.formats.WritableSpiralFormatBridge
import info.spiralframework.core.common.formats.archives.CpkArchiveFormat
import info.spiralframework.core.common.formats.archives.PakArchiveFormat
import info.spiralframework.core.common.formats.archives.SpcArchiveFormat
import info.spiralframework.core.common.formats.archives.WadArchiveFormat
import info.spiralframework.core.common.formats.spiralWrongFormat
import info.spiralframework.formats.common.archives.*
import java.io.File
import java.util.*

public object FolderFormat : WritableSpiralFormatBridge<Unit> {
    override fun supportsWritingAs(
        context: SpiralContext,
        writeContext: SpiralProperties?,
        format: WritableSpiralFormat<*>,
        data: Any
    ): Boolean =
        data is Folder && (format === CpkArchiveFormat || format === PakArchiveFormat || format === SpcArchiveFormat || format === WadArchiveFormat)

    override suspend fun writeAs(
        context: SpiralContext,
        writeContext: SpiralProperties?,
        format: WritableSpiralFormat<*>,
        data: Any,
        flow: OutputFlow
    ): KorneaResult<Unit> =
        when (data) {
            is Folder -> {
                when (format) {
                    CpkArchiveFormat -> {
                        val customCpk = CustomCpkArchive()
                        val files: MutableList<DataSource<*>> = ArrayList()

                        data.base.walk()
                            .filter(File::isFile)
                            .forEach { file ->
                                customCpk[file relativePathFrom data.base] = AsyncFileDataSource(file).also(files::add)
                            }

                        customCpk.compile(context, flow)
                        files.suspendForEach(DataSource<*>::close)

                        KorneaResult.success()
                    }
                    PakArchiveFormat -> {
                        val customPak = CustomPakArchive()
                        val files: MutableList<DataSource<*>> = ArrayList()

                        data.base.walk()
                            .filter(File::isFile)
                            .associateBy { file ->
                                file.name.substringBeforeLast('.').toIntOrNull() ?: customPak.nextFreeIndex()
                            }
                            .forEach { (index, file) ->
                                customPak[index] = AsyncFileDataSource(file).also(files::add)
                            }

                        customPak.compile(flow)
                        files.suspendForEach(DataSource<*>::close)

                        KorneaResult.success()
                    }
                    SpcArchiveFormat -> {
                        val customSpc = CustomSpcArchive()
                        val files: MutableList<DataSource<*>> = ArrayList()

                        data.base.walk()
                            .filter(File::isFile)
                            .forEach { file ->
                                customSpc[file relativePathFrom data.base] = AsyncFileDataSource(file).also(files::add)
                            }

                        customSpc.compile(flow)
                        files.suspendForEach(DataSource<*>::close)

                        KorneaResult.success()
                    }
                    WadArchiveFormat -> {
                        val customWad = CustomWadArchive()
                        val files: MutableList<DataSource<*>> = ArrayList()

                        data.base.walk()
                            .filter(File::isFile)
                            .forEach { file ->
                                customWad[file relativePathFrom data.base] = AsyncFileDataSource(file).also(files::add)
                            }

                        customWad.compile(flow)
                        files.suspendForEach(DataSource<*>::close)

                        KorneaResult.success()
                    }
                    else -> KorneaResult.spiralWrongFormat()
                }
            }
            else -> KorneaResult.spiralWrongFormat()
        }
}