package info.spiralframework.core.formats.archives

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.Optional
import info.spiralframework.base.common.SpiralContext
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.common.formats.FormatWriteResponse
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.WritableSpiralFormat
import java.io.File

object FolderFormat : ReadableSpiralFormat<File>, WritableSpiralFormat {
    override val name: String = "folder"
    override val extension: String? = null

    override suspend fun identify(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): KorneaResult<Optional<File>> {
        TODO("Not Implemented; Data Sources only provide Input Streams")
    }

    override suspend fun read(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): KorneaResult<File> {
        TODO("Not Implemented; Data Sources only provide Input Streams")
    }

    override fun supportsWriting(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun write(context: SpiralContext, writeContext: SpiralProperties?, data: Any, flow: OutputFlow): FormatWriteResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}