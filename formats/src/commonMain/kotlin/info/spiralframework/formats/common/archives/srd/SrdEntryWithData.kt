package info.spiralframework.formats.common.archives.srd

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.cast
import dev.brella.kornea.errors.common.filterToInstance
import dev.brella.kornea.errors.common.flatMap
import dev.brella.kornea.errors.common.useAndFlatMap
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.BinaryInputFlow
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.SeekableInputFlow
import dev.brella.kornea.io.common.flow.readAndClose
import dev.brella.kornea.toolkit.common.oneTimeMutableInline
import info.spiralframework.base.common.SpiralContext

abstract class SrdEntryWithData(classifier: Int, mainDataLength: ULong, subDataLength: ULong, unknown: Int, dataSource: DataSource<*>) : BaseSrdEntry(classifier, mainDataLength, subDataLength, unknown, dataSource) {
    abstract class WithRsiSubdata(classifier: Int, mainDataLength: ULong, subDataLength: ULong, unknown: Int, dataSource: DataSource<*>): SrdEntryWithData(classifier, mainDataLength, subDataLength, unknown, dataSource) {
        var rsiEntry: RSISrdEntry by oneTimeMutableInline()

        override suspend fun setup(context: SpiralContext): KorneaResult<WithRsiSubdata> {
            return RSISrdEntry(context, openSubDataSource()).flatMap { rsiEntry ->
                this.rsiEntry = rsiEntry

                super.setup(context).cast()
            }
        }
    }

    override suspend fun setup(context: SpiralContext): KorneaResult<SrdEntryWithData> {
        val dataSource = openMainDataSource()
        if (dataSource.reproducibility.isRandomAccess())
            return dataSource.openInputFlow()
                .filterToInstance<InputFlow, SeekableInputFlow> { flow -> KorneaResult.success(BinaryInputFlow(flow.readAndClose())) }
                .flatMap { flow -> context.setup(flow) }
        else {
            return dataSource
                .openInputFlow()
                .useAndFlatMap { flow -> context.setup(BinaryInputFlow(flow.readAndClose())) }
        }
    }

    protected abstract suspend fun SpiralContext.setup(flow: SeekableInputFlow): KorneaResult<SrdEntryWithData>
}