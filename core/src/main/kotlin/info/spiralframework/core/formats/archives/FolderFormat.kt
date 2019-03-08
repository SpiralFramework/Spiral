package info.spiralframework.core.formats.archives

import info.spiralframework.core.formats.FormatWriteResponse
import info.spiralframework.core.formats.WritableSpiralFormat
import info.spiralframework.formats.game.DRGame
import info.spiralframework.formats.utils.DataContext
import java.io.OutputStream

object FolderFormat: WritableSpiralFormat {
    override val name: String = "folder"

    override fun supportsWriting(data: Any): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun write(name: String?, game: DRGame?, context: DataContext, data: Any, stream: OutputStream): FormatWriteResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}