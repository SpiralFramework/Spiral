package info.spiralframework.base.jvm.io.files

import org.abimon.kornea.io.common.DataPool
import org.abimon.kornea.io.jvm.files.FileDataPool
import org.abimon.kornea.io.jvm.files.FileInputFlow
import org.abimon.kornea.io.jvm.files.FileOutputFlow
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