package info.spiralframework.core.formats.archives

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.*
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.OutputFlow
import java.io.File
import java.util.*

object FolderFormat : ReadableSpiralFormat<File>, WritableSpiralFormat {
    override val name: String = "folder"
    override val extension: String? = null

    override suspend fun identify(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<Optional<File>> {
        TODO("Not Implemented; Data Sources only provide Input Streams")
    }

    override suspend fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<File> {
        TODO("Not Implemented; Data Sources only provide Input Streams")
    }

    override fun supportsWriting(context: SpiralContext, writeContext: FormatWriteContext?, data: Any): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun write(context: SpiralContext, writeContext: FormatWriteContext?, data: Any, flow: OutputFlow): FormatWriteResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}