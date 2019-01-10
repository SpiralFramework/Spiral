package info.spiralframework.formats.archives

import info.spiralframework.base.HeaderInputStream
import info.spiralframework.formats.compression.RawSPCCompression
import info.spiralframework.formats.utils.DataHandler
import info.spiralframework.base.WindowedInputStream
import info.spiralframework.formats.compression.HeaderSPCCompression
import info.spiralframework.formats.utils.sha512Hash
import info.spiralframework.formats.utils.writeInt32LE
import java.io.*

data class SPCEntry(val compressionFlag: Int, val unknownFlag: Int, val compressedSize: Long, val decompressedSize: Long, val name: String, val offset: Long, val spc: SPC) {
    private val decompressedData: File by lazy {
        val hash = ".sha512-${rawInputStream.use(InputStream::sha512Hash)}"
        val tmp = DataHandler.createTmpFile(hash)

        FileOutputStream(tmp).use { outStream ->
            HeaderSPCCompression.decompressToPipe(this::rawInputStream, outStream)
        }

        return@lazy tmp
    }

    val rawInputStream: InputStream
        get() = HeaderInputStream(
                ByteArrayOutputStream().apply {
                    writeInt32LE(HeaderSPCCompression.MAGIC_NUMBER)
                    writeInt32LE(compressionFlag)
                    writeInt32LE(compressedSize)
                    writeInt32LE(decompressedSize)
                }.toByteArray().inputStream(),
                WindowedInputStream(spc.dataSource(), offset, compressedSize)
        )

    val inputStream: InputStream
        get() = FileInputStream(decompressedData)
}