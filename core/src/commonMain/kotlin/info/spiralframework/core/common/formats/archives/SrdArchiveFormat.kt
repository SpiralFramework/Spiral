package info.spiralframework.core.common.formats.archives

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.filter
import dev.brella.kornea.io.common.DataSource
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.base.common.properties.get
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.SpiralFormatOptionalResult
import info.spiralframework.core.common.formats.SpiralFormatReturnResult
import info.spiralframework.formats.common.archives.srd.SrdArchive

public object SrdArchiveFormat : ReadableSpiralFormat<SrdArchive> {
    override val name: String = "SRD"
    override val extension: String = "srd"

    override suspend fun identify(
        context: SpiralContext,
        readContext: SpiralProperties?,
        source: DataSource<*>
    ): SpiralFormatOptionalResult<SrdArchive> {
        val fileName = readContext[ISpiralProperty.FileName]?.substringAfterLast('/')
        if (fileName != null && fileName.contains('.') && !fileName.substringAfterLast('.').equals(extension, true)) {
            return KorneaResult.errorAsIllegalArgument(-1, "Invalid extension ${fileName.substringAfterLast('.')}")
        }

        return super.identify(context, readContext, source)
    }

    override suspend fun read(
        context: SpiralContext,
        readContext: SpiralProperties?,
        source: DataSource<*>
    ): SpiralFormatReturnResult<SrdArchive> =
        SrdArchive(context, source)
            .filter { srd -> srd.entries.isNotEmpty() }
            .ensureFormatSuccess { srd -> if (srd.entries.size == 1) 0.4 else 0.5 }
}