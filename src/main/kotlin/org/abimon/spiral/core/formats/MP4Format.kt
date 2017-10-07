package org.abimon.spiral.core.formats

import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.core.readString
import org.abimon.spiral.util.MediaWrapper
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.errPrintln
import org.abimon.visi.io.skipBytes
import org.abimon.visi.io.writeTo
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*
import java.util.concurrent.TimeUnit

object MP4Format: SpiralFormat {
    override val name: String = "MP4"
    override val extension: String? = "mp4"
    override val conversions: Array<SpiralFormat> = arrayOf(IVFFormat, OggFormat)

    val chunkTypes = arrayOf("ftyp", "mdat", "moov", "pnot", "udta", "uuid", "moof", "free", "skip", "jP2 ", "wide", "load", "ctab", "imap", "matt", "kmat", "clip", "crgn", "sync", "chap", "tmcd", "scpt", "ssrc", "PICT")
    val subtypes = arrayOf("avc1", "iso2", "isom", "mmp4", "mp41", "mp42", "mp71", "msnv", "ndas", "ndsc", "ndsh", "ndsm", "ndsp", "ndss", "ndxc", "ndxh", "ndxm", "ndxp", "ndxs")

    override fun isFormat(source: DataSource): Boolean = source.use { stream ->
        stream.skipBytes(4)
        val chunkType = stream.readString(4)

        if(chunkType != "ftyp")
            return@use false

        val subtype = stream.readString(4)

        return@use subtype in subtypes
    }

    //override fun canConvert(format: SpiralFormat): Boolean = super.canConvert(format) && MediaWrapper.ffmpeg.isInstalled

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(format, source, output, params)) return true

        if (!MediaWrapper.ffmpeg.isInstalled) {
            errPrintln("ffmpeg is not installed, and thus we cannot convert from an MP4 file to a ${format.name} file")
            return false
        }

        val tmpIn = File("${UUID.randomUUID()}.$extension")
        val tmpOut = File("${UUID.randomUUID()}.${format.extension ?: "mp4"}") //unk won't be a valid conversion, so if all else fails let's be useful

        try {
            FileOutputStream(tmpIn).use { outputStream -> source.use { inputStream -> inputStream.writeTo(outputStream) } }

            if(format in SpiralFormats.audioFormats && format !in SpiralFormats.videoFormats)
                MediaWrapper.ffmpeg.extractAudio(tmpIn, tmpOut)
            else
                MediaWrapper.ffmpeg.convert(tmpIn, tmpOut, 10, TimeUnit.MINUTES)

            FileInputStream(tmpOut).use { inputStream -> inputStream.writeTo(output) }
        } finally {
            tmpIn.delete()
            tmpOut.delete()
        }

        return true
    }
}