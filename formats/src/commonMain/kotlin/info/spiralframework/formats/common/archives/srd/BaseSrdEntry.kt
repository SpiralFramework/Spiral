package info.spiralframework.formats.common.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.alignmentNeededFor
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.cast
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.errors.common.map
import dev.brella.kornea.io.common.*
import dev.brella.kornea.io.common.flow.*
import dev.brella.kornea.io.common.flow.extensions.readInt32BE
import dev.brella.kornea.io.common.flow.extensions.readUInt32BE
import dev.brella.kornea.toolkit.common.closeAfter
import info.spiralframework.base.common.text.toHexString

@ExperimentalUnsignedTypes
abstract class BaseSrdEntry(open val classifier: Int, open val mainDataLength: ULong, open val subDataLength: ULong, open val unknown: Int, open val dataSource: DataSource<*>) {
    companion object {
        const val NOT_ENOUGH_DATA_KEY = "formats.srd_entry.not_enough_data"

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<BaseSrdEntry> =
            withFormats(context) {
                val flow = dataSource.openInputFlow()
                    .getOrBreak { return@withFormats it.cast() }

                closeAfter(flow) {
                    val classifier = flow.readInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val mainDataLength = flow.readUInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val subDataLength = flow.readUInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    val unknown = flow.readInt32BE() ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    flow.skip(mainDataLength + mainDataLength.alignmentNeededFor(0x10).toULong())
                    flow.skip(subDataLength + subDataLength.alignmentNeededFor(0x10).toULong())

                    val entry = when (classifier) {
                        CFHSrdEntry.MAGIC_NUMBER_BE -> CFHSrdEntry(classifier, mainDataLength.toULong(), subDataLength.toULong(), unknown, dataSource)
                        CT0SrdEntry.MAGIC_NUMBER_BE -> CT0SrdEntry(classifier, mainDataLength.toULong(), subDataLength.toULong(), unknown, dataSource)
                        MaterialsSrdEntry.MAGIC_NUMBER_BE -> MaterialsSrdEntry(classifier, mainDataLength.toULong(), subDataLength.toULong(), unknown, dataSource)
                        MeshSrdEntry.MAGIC_NUMBER_BE -> MeshSrdEntry(classifier, mainDataLength.toULong(), subDataLength.toULong(), unknown, dataSource)
                        RSFSrdEntry.MAGIC_NUMBER_BE -> RSFSrdEntry(classifier, mainDataLength.toULong(), subDataLength.toULong(), unknown, dataSource)
                        RSISrdEntry.MAGIC_NUMBER_BE -> RSISrdEntry(classifier, mainDataLength.toULong(), subDataLength.toULong(), unknown, dataSource)
                        SCNSrdEntry.MAGIC_NUMBER_BE -> SCNSrdEntry(classifier, mainDataLength.toULong(), subDataLength.toULong(), unknown, dataSource)
                        TRESrdEntry.MAGIC_NUMBER_BE -> TRESrdEntry(classifier, mainDataLength.toULong(), subDataLength.toULong(), unknown, dataSource)
                        TXISrdEntry.MAGIC_NUMBER_BE -> TXISrdEntry(classifier, mainDataLength.toULong(), subDataLength.toULong(), unknown, dataSource)
                        TextureSrdEntry.MAGIC_NUMBER_BE -> TextureSrdEntry(classifier, mainDataLength.toULong(), subDataLength.toULong(), unknown, dataSource)
                        VSDSrdEntry.MAGIC_NUMBER_BE -> VSDSrdEntry(classifier, mainDataLength.toULong(), subDataLength.toULong(), unknown, dataSource)
                        VTXSrdEntry.MAGIC_NUMBER_BE -> VTXSrdEntry(classifier, mainDataLength.toULong(), subDataLength.toULong(), unknown, dataSource)
                        else -> UnknownSrdEntry(classifier, mainDataLength.toULong(), subDataLength.toULong(), unknown, dataSource)
                    }

                    return@closeAfter entry.setup(this)
                }
            }
    }

    val classifierAsString: String by lazy {
        buildString {
            append(((classifier shr 0x18) and 0xFF).toChar())
            append(((classifier shr 0x10) and 0xFF).toChar())
            append(((classifier shr 0x08) and 0xFF).toChar())
            append(((classifier shr 0x00) and 0xFF).toChar())
        }
    }

    suspend fun openMainDataSource(): DataSource<out InputFlow> =
        WindowedDataSource(dataSource, 16uL, mainDataLength, closeParent = false)

    suspend fun openMainDataFlow(): KorneaResult<InputFlow> =
        dataSource.openInputFlow().map { parent ->
            if (parent is SeekableInputFlow) WindowedInputFlow.Seekable(parent, 16uL, mainDataLength)
            else WindowedInputFlow(parent, 16uL, mainDataLength)
        }

    suspend fun openSubDataSource(): DataSource<InputFlow> =
        WindowedDataSource(dataSource, 16uL + mainDataLength + mainDataLength.alignmentNeededFor(0x10).toUInt(), subDataLength, closeParent = false)

    suspend fun openSubDataFlow(): KorneaResult<InputFlow> =
        dataSource.openInputFlow().map { parent ->
            if (parent is SeekableInputFlow) WindowedInputFlow.Seekable(parent, 16uL + mainDataLength + mainDataLength.alignmentNeededFor(0x10).toUInt(), subDataLength)
            else WindowedInputFlow(parent, 16uL + mainDataLength + mainDataLength.alignmentNeededFor(0x10).toUInt(), subDataLength)
        }

    abstract suspend fun setup(context: SpiralContext): KorneaResult<BaseSrdEntry>
}

@ExperimentalUnsignedTypes
suspend fun SpiralContext.BaseSrdEntry(dataSource: DataSource<*>) = BaseSrdEntry(this, dataSource)

@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeBaseSrdEntry(dataSource: DataSource<*>) = BaseSrdEntry(this, dataSource).get()