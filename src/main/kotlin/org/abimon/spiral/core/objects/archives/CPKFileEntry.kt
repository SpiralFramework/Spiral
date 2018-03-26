package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.objects.compression.CRILAYLACompression
import org.abimon.spiral.core.utils.WindowedInputStream
import org.abimon.spiral.core.utils.readInt32LE
import org.abimon.spiral.core.utils.readInt64LE
import org.abimon.spiral.core.utils.readXBytes
import java.io.ByteArrayInputStream
import java.io.InputStream

data class CPKFileEntry(val fileName: String, val directoryName: String, val fileSize: Long, val extractSize: Long, val offset: Long, val isCompressed: Boolean, val cpk: CPK) {
    val inputStream: InputStream
        get() {
            if (isCompressed) {
                val baseStream = WindowedInputStream(cpk.dataSource(), offset, fileSize)

                val magic = baseStream.readInt64LE()
                if (magic != CRILAYLACompression.MAGIC_NUMBER) {
                    System.err.println("CRILAYLA compression was indicated for $directoryName/$fileName, but magic number was 0x${magic.toString(16)}, not 0x${CRILAYLACompression.MAGIC_NUMBER.toString(16)}")
                } else {
                    val uncompressedSize = baseStream.readInt32LE()
                    val dataSize = baseStream.readInt32LE()

                    val compressedData = baseStream.readXBytes(dataSize)
                    val rawDataHeader = baseStream.readXBytes(0x100)

                    return ByteArrayInputStream(CRILAYLACompression.decompress(uncompressedSize, dataSize, compressedData, rawDataHeader))
                }
            }

            return WindowedInputStream(cpk.dataSource(), offset, fileSize)
        }
}