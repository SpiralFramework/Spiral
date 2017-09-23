package org.abimon.spiral.core.objects

import org.abimon.spiral.core.formats.CRILAYLAFormat
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.util.OffsetInputStream
import org.abimon.visi.io.ByteArrayDataSource
import org.abimon.visi.io.DataSource
import java.io.ByteArrayInputStream
import java.io.InputStream

data class CPKFileEntry(val fileName: String, val directoryName: String, val fileSize: Long, val extractSize: Long, val offset: Long, val isCompressed: Boolean, val cpk: CPK) : DataSource {
    val name: String = "$directoryName/$fileName"
    override val location: String = "CPK File ${cpk.dataSource.location}, offset $offset bytes (name $name)"

    override val data: ByteArray by lazy { if(isCompressed) CRILAYLAFormat.convertToBytes(SpiralFormat.BinaryFormat, ByteArrayDataSource(rawData), emptyMap()) else rawData }

    override val inputStream: InputStream
        get() = if(isCompressed) ByteArrayInputStream(data) else OffsetInputStream(cpk.dataSource.inputStream, offset, fileSize)
    override val seekableInputStream: InputStream
        get() = if(isCompressed) ByteArrayInputStream(data) else OffsetInputStream(cpk.dataSource.seekableInputStream, offset, fileSize)

    override val size: Long = extractSize

    val rawData: ByteArray
        get() = OffsetInputStream(cpk.dataSource.inputStream, offset, fileSize).use { ByteArray(size.toInt()).apply { it.read(this) } }
}