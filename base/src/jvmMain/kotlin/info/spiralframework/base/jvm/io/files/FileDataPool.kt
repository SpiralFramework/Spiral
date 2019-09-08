package info.spiralframework.base.jvm.io.files

import info.spiralframework.base.common.io.DataPool
import info.spiralframework.base.common.io.DataSink
import info.spiralframework.base.common.io.DataSource
import java.io.File

@ExperimentalUnsignedTypes
class FileDataPool(val file: File, private val sinkBacker: DataSink<FileOutputFlow> = FileDataSink(file), private val sourceBacker: DataSource<FileInputFlow> = FileDataSource(file)) :
        DataPool<FileInputFlow, FileOutputFlow>,
        DataSink<FileOutputFlow> by sinkBacker,
        DataSource<FileInputFlow> by sourceBacker {
    override suspend fun close() {
        sinkBacker.close()
        sourceBacker.close()
    }
}