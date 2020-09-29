package info.spiralframework.formats.jvm.archives

import dev.brella.kornea.io.jvm.files.AsyncFileDataSource
import dev.brella.kornea.io.jvm.files.relativePathFrom
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.formats.common.archives.SpiralArchive
import info.spiralframework.formats.common.archives.SpiralArchiveSubfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import java.io.File

class FolderArchive(val file: File): SpiralArchive {
    override val fileCount: Int by lazy { file.walkTopDown().count(File::isFile) }

    override suspend fun SpiralContext.getSubfiles(): Flow<SpiralArchiveSubfile<*>> =
        file.walkTopDown().asFlow().map { subFile -> SpiralArchiveSubfile(subFile relativePathFrom file, AsyncFileDataSource(subFile)) }
}