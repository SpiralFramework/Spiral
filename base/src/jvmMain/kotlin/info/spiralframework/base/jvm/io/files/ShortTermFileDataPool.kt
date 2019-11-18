package info.spiralframework.base.jvm.io.files

import info.spiralframework.base.common.io.DataPool
import java.io.File

@ExperimentalUnsignedTypes
class ShortTermFileDataPool(val file: File, val backing: FileDataPool = FileDataPool(file)) : DataPool<FileInputFlow, FileOutputFlow> by backing {
    override suspend fun close() {
        super.close()

//        backing.close()
        file.delete()
    }

    init {
        file.deleteOnExit()
    }
}