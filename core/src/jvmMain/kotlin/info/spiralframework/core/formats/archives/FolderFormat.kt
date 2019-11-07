package info.spiralframework.core.formats.archives

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.*
import info.spiralframework.formats.utils.DataSource
import java.io.File
import java.io.OutputStream
import java.util.*

object FolderFormat : ReadableSpiralFormat<File>, WritableSpiralFormat {
    override val name: String = "folder"
    override val extension: String? = null

    override fun identify(context: SpiralContext, readContext: FormatReadContext?, source: DataSource): FormatResult<Optional<File>> {
        TODO("Not Implemented; Data Sources only provide Input Streams")
    }

    override fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource): FormatResult<File> {
        TODO("Not Implemented; Data Sources only provide Input Streams")
    }

    override fun supportsWriting(context: SpiralContext, data: Any): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun write(context: SpiralContext, writeContext: FormatWriteContext?, data: Any, stream: OutputStream): FormatWriteResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}