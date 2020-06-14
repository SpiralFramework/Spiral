package info.spiralframework.base.jvm.io.files

import org.abimon.kornea.annotations.ExperimentalKorneaIO
import org.abimon.kornea.io.common.DataPool
import org.abimon.kornea.io.jvm.files.AsyncFileDataPool
import org.abimon.kornea.io.jvm.files.AsyncFileInputFlow
import org.abimon.kornea.io.jvm.files.AsyncFileOutputFlow
import java.io.File

@ExperimentalKorneaIO
@ExperimentalUnsignedTypes
class ShortTermFileDataPool(val file: File, override val location: String? = file.absolutePath, val backing: AsyncFileDataPool = AsyncFileDataPool(file)) : DataPool<AsyncFileInputFlow, AsyncFileOutputFlow> by backing {
    override suspend fun close() {
        backing.close()

        file.delete()
    }

    init {
        file.deleteOnExit()
    }
}