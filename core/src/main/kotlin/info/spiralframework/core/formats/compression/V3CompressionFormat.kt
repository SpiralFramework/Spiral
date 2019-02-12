package info.spiralframework.core.formats.compression

import info.spiralframework.formats.compression.V3Compression

object V3CompressionFormat: CompressionFormat<V3Compression> {
    override val name: String = "V3 Compression"

    override val compressionFormat = V3Compression
}