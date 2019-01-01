package info.spiralframework.core.formats.compression

import info.spiralframework.formats.compression.CRILAYLACompression

object CRILAYLAFormat: CompressionFormat<CRILAYLACompression> {
    override val compressionFormat = CRILAYLACompression
}