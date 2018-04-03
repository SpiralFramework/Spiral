package org.abimon.spiral.core.formats.video

import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.audio.OggFormat
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.readString
import org.abimon.spiral.util.MediaWrapper
import org.abimon.visi.io.errPrintln
import org.abimon.visi.io.skipBytes
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit

object MP4Format: SpiralFormat {
    override val name: String = "MP4"
    override val extension: String? = "mp4"
    override val conversions: Array<SpiralFormat> = arrayOf(IVFFormat, OggFormat)

    val chunkTypes = arrayOf("ftyp", "mdat", "moov", "pnot", "udta", "uuid", "moof", "free", "skip", "jP2 ", "wide", "load", "ctab", "imap", "matt", "kmat", "clip", "crgn", "sync", "chap", "tmcd", "scpt", "ssrc", "PICT")
    val subtypes = arrayOf("avc1", "iso2", "isom", "mmp4", "mp41", "mp42", "mp71", "msnv", "ndas", "ndsc", "ndsh", "ndsm", "ndsp", "ndss", "ndxc", "ndxh", "ndxm", "ndxp", "ndxs")

    override fun isFormat(game: DRGame?, name: String?, dataSource: () -> InputStream): Boolean = dataSource().use { stream ->
        stream.skipBytes(4)
        val chunkType = stream.readString(4)

        if(chunkType != "ftyp")
            return@use false

        val subtype = stream.readString(4)

        return@use subtype in subtypes
    }

    //override fun canConvert(format: SpiralFormat): Boolean = super.canConvert(format) && MediaWrapper.ffmpeg.isInstalled

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(game, format, name, dataSource, output, params)) return true

        if (!MediaWrapper.ffmpeg.isInstalled) {
            errPrintln("ffmpeg is not installed, and thus we cannot convert from an MP4 file to a ${format.name} file")
            return false
        }

        val tmpIn = File("${UUID.randomUUID()}.$extension")
        val tmpOut = File("${UUID.randomUUID()}.${format.extension ?: "mp4"}") //unk won't be a valid conversion, so if all else fails let's be useful

        try {
            FileOutputStream(tmpIn).use { outputStream -> dataSource().use { inputStream -> inputStream.copyTo(outputStream) } }

            if(format in SpiralFormats.audioFormats && format !in SpiralFormats.videoFormats)
                MediaWrapper.ffmpeg.extractAudio(tmpIn, tmpOut)
            else
                MediaWrapper.ffmpeg.convert(tmpIn, tmpOut, 10, TimeUnit.MINUTES)

            FileInputStream(tmpOut).use { inputStream -> inputStream.copyTo(output) }
        } finally {
            tmpIn.delete()
            tmpOut.delete()
        }

        return true
    }
}