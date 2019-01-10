package info.spiralframework.formats.compression

import info.spiralframework.formats.utils.readInt32LE
import info.spiralframework.formats.utils.readOrNull
import info.spiralframework.formats.utils.use
import java.io.InputStream
import java.io.OutputStream
import java.util.*

object HeaderSPCCompression: ICompression {
    val MAGIC_NUMBER = 0x43505324

    override val supportsChunking: Boolean = false

    override fun isCompressed(dataSource: () -> InputStream): Boolean = dataSource.use { stream -> stream.readInt32LE() == MAGIC_NUMBER }

    override fun decompressToPipe(dataSource: () -> InputStream, sink: OutputStream) {
        dataSource.use { stream ->
            val magic = stream.readInt32LE()

            val flag = stream.readInt32LE()
            val compressedSize = stream.readInt32LE()
            val decompressedSize = stream.readInt32LE()

            RawSPCCompression.decompressToPipe(flag, stream, sink)
        }
    }
}