package org.abimon.spiral.core.formats

import org.abimon.spiral.core.readString
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.skipBytes
import java.io.OutputStream

object MP4Format: SpiralFormat {
    override val name: String = "MP4"
    override val extension: String? = "mp4"
    override val conversions: Array<SpiralFormat> = arrayOf(IVFFormat)

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

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>) {
        super.convert(format, source, output, params)
    }
}