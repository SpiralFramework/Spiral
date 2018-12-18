package info.spiralframework.formats.archives

import info.spiralframework.formats.compression.SPCCompression
import info.spiralframework.formats.utils.DataHandler
import info.spiralframework.formats.utils.WindowedInputStream
import info.spiralframework.formats.utils.sha512Hash
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

data class SPCEntry(val compressionFlag: Int, val unknownFlag: Int, val compressedSize: Long, val decompressedSize: Long, val name: String, val offset: Long, val spc: SPC) {
    private val decompressedData: File by lazy {
        println("Decompressing $this")
        val hash = ".sha512-${rawInputStream.use(InputStream::sha512Hash)}"

        return@lazy DataHandler.cacheFileWithNameAndData(hash) { file ->
            rawInputStream.use { baseStream ->
                FileOutputStream(file).use { outStream ->
                    SPCCompression.decompressToPipe(compressionFlag, baseStream, outStream)
                }
            }
        }
    }

    val rawInputStream: InputStream
        get() = WindowedInputStream(spc.dataSource(), offset, compressedSize)

    val inputStream: InputStream
        get() = FileInputStream(decompressedData)
}