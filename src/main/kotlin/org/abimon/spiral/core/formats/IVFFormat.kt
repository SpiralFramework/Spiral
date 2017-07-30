package org.abimon.spiral.core.formats

import org.abimon.spiral.core.byteArrayOfInts
import org.abimon.spiral.util.MediaWrapper
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.errPrintln
import org.abimon.visi.io.readPartialBytes
import org.abimon.visi.io.skipBytes
import java.io.OutputStream
import java.util.*

object IVFFormat: SpiralFormat {
    override val name: String = "IVF"
    override val extension: String? = "ivf"
    override val conversions: Array<SpiralFormat> = emptyArray()

    val initialHeader = byteArrayOfInts(0x44, 0x4B, 0x49, 0x46)
    val secondHeader = byteArrayOfInts(0x56, 0x50, 0x38, 0x30)

    override fun isFormat(source: DataSource): Boolean = source.use { stream ->
        if(!Arrays.equals(stream.readPartialBytes(4), initialHeader))
            return@use false
        stream.skipBytes(4)

        return@use Arrays.equals(stream.readPartialBytes(4), secondHeader)
    }

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>) {
        super.convert(format, source, output, params)

        if(!MediaWrapper.ffmpeg.isInstalled)
            return errPrintln("ffmpeg is not installed, and thus we cannot convert from an IVF file to a ${format.name} file")


    }
}