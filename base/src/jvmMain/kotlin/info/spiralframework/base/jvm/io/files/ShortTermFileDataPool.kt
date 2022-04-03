package info.spiralframework.base.jvm.io.files

import dev.brella.kornea.io.common.DataPool
import dev.brella.kornea.io.jvm.files.AsyncFileDataPool
import dev.brella.kornea.io.jvm.files.AsyncFileInputFlow
import dev.brella.kornea.io.jvm.files.AsyncFileOutputFlow
import java.io.File

public class ShortTermFileDataPool(
    public val file: File,
    override val location: String? = file.absolutePath,
    private val backing: AsyncFileDataPool = AsyncFileDataPool(file)
) : DataPool<AsyncFileInputFlow, AsyncFileOutputFlow> by backing {
    override suspend fun close() {
        backing.close()

        file.delete()
    }

    init {
        file.deleteOnExit()
    }
}