package info.spiralframework.formats.common.archives.srd

import dev.brella.kornea.base.common.closeAfter
import dev.brella.kornea.errors.common.*
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.WindowedDataSource
import dev.brella.kornea.io.common.flow.*
import dev.brella.kornea.io.common.flow.extensions.readInt32BE
import dev.brella.kornea.io.common.flow.extensions.readUInt32BE
import dev.brella.kornea.io.common.flow.extensions.writeInt32BE
import dev.brella.kornea.io.common.flow.extensions.writeUInt32BE
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.alignmentNeededFor
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats

public abstract class BaseSrdEntry(
    public open val classifier: Int,
    public open val mainDataLength: ULong,
    public open val subDataLength: ULong,
    public open val unknown: Int
) {
    public companion object {
        public const val NOT_ENOUGH_DATA_KEY: String = "formats.srd_entry.not_enough_data"

        public suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<BaseSrdEntry> =
            withFormats(context) {
                val flow = dataSource.openInputFlow()
                    .getOrBreak { return@withFormats it.cast() }

                closeAfter(flow) {
                    val classifier = flow.readInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val mainDataLength =
                        flow.readUInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val subDataLength =
                        flow.readUInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val unknown = flow.readInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    flow.skip(mainDataLength + mainDataLength.alignmentNeededFor(0x10).toULong())
                    flow.skip(subDataLength + subDataLength.alignmentNeededFor(0x10).toULong())

                    val entry = when (classifier) {
                        CFHSrdEntry.MAGIC_NUMBER_BE -> CFHSrdEntry(
                            classifier,
                            mainDataLength.toULong(),
                            subDataLength.toULong(),
                            unknown
                        )
                        CT0SrdEntry.MAGIC_NUMBER_BE -> CT0SrdEntry(
                            classifier,
                            mainDataLength.toULong(),
                            subDataLength.toULong(),
                            unknown
                        )
                        MaterialsSrdEntry.MAGIC_NUMBER_BE -> MaterialsSrdEntry(
                            classifier,
                            mainDataLength.toULong(),
                            subDataLength.toULong(),
                            unknown
                        )
                        MeshSrdEntry.MAGIC_NUMBER_BE -> MeshSrdEntry(
                            classifier,
                            mainDataLength.toULong(),
                            subDataLength.toULong(),
                            unknown
                        )
                        RSFSrdEntry.MAGIC_NUMBER_BE -> RSFSrdEntry(
                            classifier,
                            mainDataLength.toULong(),
                            subDataLength.toULong(),
                            unknown
                        )
                        RSISrdEntry.MAGIC_NUMBER_BE -> RSISrdEntry(
                            classifier,
                            mainDataLength.toULong(),
                            subDataLength.toULong(),
                            unknown
                        )
                        SCNSrdEntry.MAGIC_NUMBER_BE -> SCNSrdEntry(
                            classifier,
                            mainDataLength.toULong(),
                            subDataLength.toULong(),
                            unknown
                        )
                        TRESrdEntry.MAGIC_NUMBER_BE -> TRESrdEntry(
                            classifier,
                            mainDataLength.toULong(),
                            subDataLength.toULong(),
                            unknown
                        )
                        TXISrdEntry.MAGIC_NUMBER_BE -> TXISrdEntry(
                            classifier,
                            mainDataLength.toULong(),
                            subDataLength.toULong(),
                            unknown
                        )
                        TextureSrdEntry.MAGIC_NUMBER_BE -> TextureSrdEntry(
                            classifier,
                            mainDataLength.toULong(),
                            subDataLength.toULong(),
                            unknown
                        )
                        VSDSrdEntry.MAGIC_NUMBER_BE -> VSDSrdEntry(
                            classifier,
                            mainDataLength.toULong(),
                            subDataLength.toULong(),
                            unknown
                        )
                        VTXSrdEntry.MAGIC_NUMBER_BE -> VTXSrdEntry(
                            classifier,
                            mainDataLength.toULong(),
                            subDataLength.toULong(),
                            unknown
                        )
                        else -> UnknownSrdEntry(classifier, mainDataLength.toULong(), subDataLength.toULong(), unknown)
                    }

                    return@closeAfter entry.setup(this, dataSource)
                }
            }
    }

    public val classifierAsString: String by lazy {
        buildString {
            append(((classifier shr 0x18) and 0xFF).toChar())
            append(((classifier shr 0x10) and 0xFF).toChar())
            append(((classifier shr 0x08) and 0xFF).toChar())
            append(((classifier shr 0x00) and 0xFF).toChar())
        }
    }

    public abstract suspend fun setup(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<BaseSrdEntry>
    public suspend fun writeTo(context: SpiralContext, out: OutputFlow) {
        val mainData = BinaryOutputFlow().apply { context.writeMainData(this) }.getData()
        val subData = BinaryOutputFlow().apply { context.writeSubData(this) }.getData()

        out.writeInt32BE(classifier)
        out.writeUInt32BE(mainData.size)
        out.writeUInt32BE(subData.size)
        out.writeInt32BE(unknown)

        out.write(mainData)
        out.write(ByteArray(mainData.size.alignmentNeededFor(16)))
        out.write(subData)
        out.write(ByteArray(subData.size.alignmentNeededFor(16)))

//        context.writeTo(out)
    }

    protected abstract suspend fun SpiralContext.writeMainData(out: OutputFlow)
    protected abstract suspend fun SpiralContext.writeSubData(out: OutputFlow)
}

@Suppress("FunctionName")
public suspend fun SpiralContext.BaseSrdEntry(dataSource: DataSource<*>): KorneaResult<BaseSrdEntry> = BaseSrdEntry(this, dataSource)
@Suppress("FunctionName")
public suspend fun SpiralContext.UnsafeBaseSrdEntry(dataSource: DataSource<*>): BaseSrdEntry = BaseSrdEntry(this, dataSource).getOrThrow()

public fun BaseSrdEntry.openMainDataSource(dataSource: DataSource<*>): DataSource<InputFlow> =
    WindowedDataSource(dataSource, 16uL, mainDataLength, closeParent = false)

public suspend fun BaseSrdEntry.openMainDataFlow(dataSource: DataSource<*>): KorneaResult<InputFlow> =
    dataSource.openInputFlow().map { parent ->
        if (parent is SeekableInputFlow) WindowedInputFlow.Seekable(parent, 16uL, mainDataLength)
        else WindowedInputFlow(parent, 16uL, mainDataLength)
    }

public fun BaseSrdEntry.openSubDataSource(dataSource: DataSource<*>): DataSource<InputFlow> =
    WindowedDataSource(
        dataSource,
        16uL + mainDataLength + mainDataLength.alignmentNeededFor(0x10).toUInt(),
        subDataLength,
        closeParent = false
    )

public suspend fun BaseSrdEntry.openSubDataFlow(dataSource: DataSource<*>): KorneaResult<InputFlow> =
    dataSource.openInputFlow().map { parent ->
        if (parent is SeekableInputFlow) WindowedInputFlow.Seekable(
            parent,
            16uL + mainDataLength + mainDataLength.alignmentNeededFor(0x10).toUInt(),
            subDataLength
        )
        else WindowedInputFlow(
            parent,
            16uL + mainDataLength + mainDataLength.alignmentNeededFor(0x10).toUInt(),
            subDataLength
        )
    }