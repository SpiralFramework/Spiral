package info.spiralframework.core.formats.compression

import info.spiralframework.formats.compression.HeaderSPCCompression

object SPCCompressionFormat: CompressionFormat<HeaderSPCCompression> {
    override val name: String = "Spc Compression"

    override val compressionFormat = HeaderSPCCompression
}