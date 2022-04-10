package info.spiralframework.formats.common.archives.srd

import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.BinaryInputFlow
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.SeekableInputFlow
import dev.brella.kornea.io.common.flow.readAndClose
import dev.brella.kornea.toolkit.common.oneTimeMutableInline
import info.spiralframework.base.common.SpiralContext

public abstract class SrdEntryWithData(classifier: Int, mainDataLength: ULong, subDataLength: ULong, unknown: Int) : BaseSrdEntry(classifier, mainDataLength, subDataLength, unknown) {
    public abstract class WithRsiSubdata(classifier: Int, mainDataLength: ULong, subDataLength: ULong, unknown: Int): SrdEntryWithData(classifier, mainDataLength, subDataLength, unknown) {
        public var rsiEntry: RSISrdEntry by oneTimeMutableInline()

        override suspend fun setup(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<WithRsiSubdata> {
            return RSISrdEntry(context, openSubDataSource(dataSource)).flatMap { rsiEntry ->
                this.rsiEntry = rsiEntry

                super.setup(context, dataSource).cast()
            }
        }
    }

    override suspend fun setup(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<SrdEntryWithData> {
        val mainDataSource = openMainDataSource(dataSource)
        return if (mainDataSource.reproducibility.isRandomAccess())
            mainDataSource.openInputFlow()
                .filterToInstanceTyped<SeekableInputFlow, InputFlow> { flow ->
                    KorneaResult.success(BinaryInputFlow(flow.readAndClose()))
                }.flatMap { flow -> context.setup(flow) }
        else {
            mainDataSource
                .openInputFlow()
                .useAndFlatMap { flow -> context.setup(BinaryInputFlow(flow.readAndClose())) }
        }
    }

    protected abstract suspend fun SpiralContext.setup(flow: SeekableInputFlow): KorneaResult<SrdEntryWithData>
}