package info.spiralframework.formats.archives

import info.spiralframework.base.WindowedInputStream
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.util.readInt32LE
import info.spiralframework.base.util.readInt64LE
import info.spiralframework.base.util.readXBytes
import info.spiralframework.base.jvm.crypto.sha512Hash
import info.spiralframework.formats.compression.CRILAYLACompression
import info.spiralframework.formats.utils.DataHandler
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

//TODO: Don't make this a data class? Or figure something else out rather
data class CPKFileEntry private constructor(var context: SpiralContext?, val fileName: String, val directoryName: String, val fileSize: Long, val extractSize: Long, val offset: Long, val isCompressed: Boolean, val cpk: CPK) {
    companion object {
        operator fun invoke(context: SpiralContext, fileName: String, directoryName: String, fileSize: Long, extractSize: Long, offset: Long, isCompressed: Boolean, cpk: CPK) = CPKFileEntry(context, fileName, directoryName, fileSize, extractSize, offset, isCompressed, cpk)
    }
    private val compressedFile: File?

    val inputStream: InputStream
        get() = compressedFile?.let(::FileInputStream) ?: rawInputStream

    val rawInputStream: InputStream
        get() = WindowedInputStream(cpk.dataSource(), offset, fileSize)

    val name: String = if (directoryName.isNotBlank()) "$directoryName/$fileName" else fileName

    init {
        if (!isCompressed || context == null) {
            compressedFile = null
        } else {
            with(context!!) {
                val hash = ".sha512-${rawInputStream.use(InputStream::sha512Hash)}"
                val file = DataHandler.createTmpFile(hash)

                compressedFile = rawInputStream.use { baseStream ->
                    val magic = baseStream.readInt64LE()
                    if (magic != CRILAYLACompression.MAGIC_NUMBER) {
                        //TODO: Rework this bit
                        warn("formats.cpk.wrong_compression", "$directoryName/$fileName", "0x${magic.toString(16)}", "0x${CRILAYLACompression.MAGIC_NUMBER.toString(16)}")
                        return@use null
                    } else {
                        val uncompressedSize = baseStream.readInt32LE()
                        val dataSize = baseStream.readInt32LE()

                        val compressedData = baseStream.readXBytes(dataSize)
                        val rawDataHeader = baseStream.readXBytes(0x100)

                        FileOutputStream(file).use { outStream -> outStream.write(CRILAYLACompression.decompress(this, uncompressedSize, dataSize, compressedData, rawDataHeader)) }
                        return@use file
                    }
                }
                context = null //Since this is a data class, this will break.
            }
        }
    }
}