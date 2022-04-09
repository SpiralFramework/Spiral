package info.spiralframework.formats.common.archives

import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.InputFlow
import info.spiralframework.base.common.SpiralContext
import kotlinx.coroutines.flow.Flow

public interface SpiralArchive {
    public val fileCount: Int

    public suspend fun SpiralContext.getSubfiles(): Flow<SpiralArchiveSubfile<*>>
}

public interface SpiralArchiveSubfile<out I: InputFlow> {
    public data class Base<out I: InputFlow>(override val path: String, override val dataSource: DataSource<I>): SpiralArchiveSubfile<I>

    public companion object {
        @Suppress("NOTHING_TO_INLINE")
        public inline operator fun <I: InputFlow> invoke(path: String, dataSource: DataSource<I>): SpiralArchiveSubfile<I> =
            Base(path, dataSource)
    }

    public val path: String
    public val dataSource: DataSource<I>

    public operator fun component1(): String = path
    public operator fun component2(): DataSource<I> = dataSource
}

public suspend fun SpiralArchive.getSubfiles(context: SpiralContext): Flow<SpiralArchiveSubfile<*>> = context.getSubfiles()