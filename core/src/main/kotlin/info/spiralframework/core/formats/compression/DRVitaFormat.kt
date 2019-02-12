package info.spiralframework.core.formats.compression

import info.spiralframework.formats.compression.DRVitaCompression

object DRVitaFormat: CompressionFormat<DRVitaCompression> {
    override val name: String = "Vita Compression"

    override val compressionFormat = DRVitaCompression
}