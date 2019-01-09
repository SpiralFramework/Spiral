package info.spiralframework.core.formats.archives

import info.spiralframework.core.formats.EnumFormatWriteResponse
import info.spiralframework.core.formats.FormatResult
import info.spiralframework.core.formats.ReadableSpiralFormat
import info.spiralframework.core.formats.WritableSpiralFormat
import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.utils.DataContext
import info.spiralframework.formats.utils.DataSource
import java.io.File
import java.io.OutputStream

object FolderFormat: WritableSpiralFormat {
    override fun supportsWriting(data: Any): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun write(name: String?, game: DRGame?, context: DataContext, data: Any, stream: OutputStream): EnumFormatWriteResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}