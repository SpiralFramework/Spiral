package info.spiralframework.formats.archives

import info.spiralframework.base.WindowedInputStream
import info.spiralframework.base.util.readInt32LE
import info.spiralframework.base.util.readInt64LE
import info.spiralframework.base.util.readXBytes
import info.spiralframework.formats.compression.CRILAYLACompression
import info.spiralframework.formats.utils.*
import java.io.ByteArrayInputStream
import java.io.InputStream

data class CPKFileEntry(val fileName: String, val directoryName: String, val fileSize: Long, val extractSize: Long, val offset: Long, val isCompressed: Boolean, val cpk: CPK) {
    val inputStream: InputStream
        get() {
            if (isCompressed) {
                val baseStream = rawInputStream

                val magic = baseStream.readInt64LE()
                if (magic != CRILAYLACompression.MAGIC_NUMBER) {
                    DataHandler.LOGGER.warn("formats.cpk.wrong_compression", "$directoryName/$fileName", "0x${magic.toString(16)}", "0x${CRILAYLACompression.MAGIC_NUMBER.toString(16)}")
                } else {
                    val uncompressedSize = baseStream.readInt32LE()
                    val dataSize = baseStream.readInt32LE()

                    val compressedData = baseStream.readXBytes(dataSize)
                    val rawDataHeader = baseStream.readXBytes(0x100)

                    return ByteArrayInputStream(CRILAYLACompression.decompress(uncompressedSize, dataSize, compressedData, rawDataHeader))
                }
            }

            return rawInputStream
        }

    val rawInputStream: InputStream
        get() = WindowedInputStream(cpk.dataSource(), offset, fileSize)

    val name: String = if (directoryName.isNotBlank()) "$directoryName/$fileName" else fileName
}