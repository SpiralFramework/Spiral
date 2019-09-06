package info.spiralframework.formats.archives

import info.spiralframework.base.HeaderInputStream
import info.spiralframework.base.WindowedInputStream
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.util.sha512Hash
import info.spiralframework.base.util.writeInt32LE
import info.spiralframework.formats.compression.HeaderSPCCompression
import info.spiralframework.formats.utils.DataHandler
import java.io.*

//TODO: Don't make this a data class? Or figure something else out rather
data class SPCEntry private constructor(var context: SpiralContext?, val compressionFlag: Int, val unknownFlag: Int, val compressedSize: Long, val decompressedSize: Long, val name: String, val offset: Long, val spc: SPC) {
    companion object {
        operator fun invoke(context: SpiralContext, compressionFlag: Int, unknownFlag: Int, compressedSize: Long, decompressedSize: Long, name: String, offset: Long, spc: SPC) =
                SPCEntry(context, compressionFlag, unknownFlag, compressedSize, decompressedSize, name, offset, spc)
    }

    private val decompressedData: File

    val rawInputStream: InputStream
        get() = openNewRawInputStream()

    val inputStream: InputStream
        get() = FileInputStream(decompressedData)

    fun openNewRawInputStream(): InputStream = HeaderInputStream(
            ByteArrayOutputStream().apply {
                writeInt32LE(HeaderSPCCompression.MAGIC_NUMBER)
                writeInt32LE(compressionFlag)
                writeInt32LE(compressedSize)
                writeInt32LE(decompressedSize)
            }.toByteArray().inputStream(),
            WindowedInputStream(spc.dataSource(), offset, compressedSize)
    )

    init {
        with(requireNotNull(context)) {
            val hash = ".sha512-${rawInputStream.use(InputStream::sha512Hash)}"
            decompressedData = DataHandler.createTmpFile(hash)

            FileOutputStream(decompressedData).use { outStream ->
                HeaderSPCCompression.decompressToPipe(this, this@SPCEntry::openNewRawInputStream, outStream)
            }
        }
    }
}