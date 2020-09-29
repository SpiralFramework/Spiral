package info.spiralframework.formats.common.archives

import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.InputFlow
import info.spiralframework.base.common.SpiralContext
import kotlinx.coroutines.flow.Flow

interface SpiralArchive {
    val fileCount: Int

    suspend fun SpiralContext.getSubfiles(): Flow<SpiralArchiveSubfile<*>>
}

interface SpiralArchiveSubfile<out I: InputFlow> {
    data class Base<out I: InputFlow>(override val path: String, override val dataSource: DataSource<I>): SpiralArchiveSubfile<I>

    companion object {
        inline operator fun <I: InputFlow> invoke(path: String, dataSource: DataSource<I>): SpiralArchiveSubfile<I> =
            Base(path, dataSource)
    }

    val path: String
    val dataSource: DataSource<I>

    operator fun component1(): String = path
    operator fun component2(): DataSource<I> = dataSource
}

suspend fun SpiralArchive.getSubfiles(context: SpiralContext) = context.getSubfiles()