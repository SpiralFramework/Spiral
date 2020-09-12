package info.spiralframework.core.formats.archives

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.Optional
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.*
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.OutputFlow
import java.io.File
import java.util.*
import java.util.Optional as JvmOptional

object FolderFormat : ReadableSpiralFormat<File>, WritableSpiralFormat {
    override val name: String = "folder"
    override val extension: String? = null

    override suspend fun identify(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): KorneaResult<Optional<File>> {
        TODO("Not Implemented; Data Sources only provide Input Streams")
    }

    override suspend fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): KorneaResult<File> {
        TODO("Not Implemented; Data Sources only provide Input Streams")
    }

    override fun supportsWriting(context: SpiralContext, writeContext: FormatWriteContext?, data: Any): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun write(context: SpiralContext, writeContext: FormatWriteContext?, data: Any, flow: OutputFlow): FormatWriteResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}