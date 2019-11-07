package info.spiralframework.core.formats.compression

import info.spiralframework.formats.compression.CRILAYLACompression

object CRILAYLAFormat: CompressionFormat<CRILAYLACompression> {
    override val name: String = "CRILAYLA Compression"
    override val extension: String = "cmp"

    override val compressionFormat = CRILAYLACompression
}