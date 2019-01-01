package info.spiralframework.core.formats.compression

import info.spiralframework.formats.compression.DRVitaCompression

object DRVitaFormat: CompressionFormat<DRVitaCompression> {
    override val compressionFormat = DRVitaCompression
}