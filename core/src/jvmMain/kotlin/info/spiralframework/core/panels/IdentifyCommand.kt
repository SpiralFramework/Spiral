package info.spiralframework.core.panels

import dev.brella.kornea.base.common.Optional
import dev.brella.kornea.base.common.use
import dev.brella.kornea.io.common.DataSource
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.cache
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.*
import info.spiralframework.core.common.formats.GenericFormatSuccess
import info.spiralframework.core.common.formats.ReadableSpiralFormat

public interface IdentifyCommand {
    public val identifiableFormats: List<ReadableSpiralFormat<*>>

    public suspend fun beginIdentification(context: SpiralContext, readContext: SpiralProperties, dataSource: DataSource<*>, formats: List<ReadableSpiralFormat<*>>)

    public suspend fun noFormatFound(context: SpiralContext, readContext: SpiralProperties, dataSource: DataSource<*>)
    public suspend fun foundFileFormat(context: SpiralContext, readContext: SpiralProperties, dataSource: DataSource<*>, result: GenericFormatSuccess<Optional<*>>, compressionFormats: List<ReadableCompressionFormat>?)

    public suspend fun finishIdentification(context: SpiralContext, readContext: SpiralProperties, dataSource: DataSource<*>)

    public suspend operator fun invoke(context: SpiralContext, readContext: SpiralProperties, dataSource: DataSource<*>) {
        val (decompressedDataSource, archiveCompressionFormats) = if (dataSource.reproducibility.isUnreliable() || dataSource.reproducibility.isUnstable()) {
            dataSource.cache(context).use { ds -> context.decompress(ds) }
        } else {
            context.decompress(dataSource)
        }

        val result = performIdentification(context, readContext, dataSource, identifiableFormats.sortedAgainst(readContext)) { formats ->
            formats.mapFormatResults { archive -> archive.identify(this, readContext, decompressedDataSource) }
                .filterSuccesses()
                .maxByConfidenceOrNull()
        }

        if (result == null) {
            noFormatFound(context, readContext, dataSource)
            return
        }

        foundFileFormat(context, readContext, dataSource, result, archiveCompressionFormats)
    }
}

public suspend inline fun <T> IdentifyCommand.performIdentification(context: SpiralContext, readContext: SpiralProperties, dataSource: DataSource<*>, formats: List<ReadableSpiralFormat<*>>, operation: SpiralContext.(formats: List<ReadableSpiralFormat<*>>) -> T): T {
    try {
        beginIdentification(context, readContext, dataSource, formats)
        return operation(context, formats)
    } finally {
        finishIdentification(context, readContext, dataSource)
    }
}