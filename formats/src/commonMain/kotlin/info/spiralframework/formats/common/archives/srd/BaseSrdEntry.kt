package info.spiralframework.formats.common.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.alignmentNeededFor
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.io.common.*
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.WindowedInputFlow

@ExperimentalUnsignedTypes
abstract class BaseSrdEntry(open val classifier: Int, open val mainDataLength: ULong, open val subDataLength: ULong, open val unknown: Int, open val dataSource: DataSource<*>) {
    companion object {
        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): BaseSrdEntry? {
            try {
                return unsafe(context, dataSource)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.srd_entry.invalid", dataSource, iae) }

                return null
            }
        }
        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>) = requireNotNull(pseudoSafe(context, dataSource))
        suspend fun pseudoSafe(context: SpiralContext, dataSource: DataSource<*>): BaseSrdEntry? {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.srd_entry.not_enough_data") }

                val flow = requireNotNull(dataSource.openInputFlow())

                use(flow) {
                    val classifier = flow.readInt32BE() ?: return null
                    val mainDataLength = requireNotNull(flow.readUInt32BE(), notEnoughData)
                    val subDataLength = requireNotNull(flow.readUInt32BE(), notEnoughData)
                    val unknown = requireNotNull(flow.readInt32BE(), notEnoughData)

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

                    entry.setup(this)
                    return entry
                }
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

    suspend fun openMainDataSource(): DataSource<out InputFlow> = WindowedDataSource(dataSource, 16uL, mainDataLength, closeParent = false)
    suspend fun openMainDataFlow(): InputFlow? {
        val parent = dataSource.openInputFlow() ?: return null
        return WindowedInputFlow(parent, 16uL, mainDataLength)
    }

    suspend fun openSubDataSource(): DataSource<out InputFlow> = WindowedDataSource(dataSource, 16uL + mainDataLength + mainDataLength.alignmentNeededFor(0x10).toUInt(), subDataLength, closeParent = false)
    suspend fun openSubDataFlow(): InputFlow? {
        val parent = dataSource.openInputFlow() ?: return null
        return WindowedInputFlow(parent, 16uL + mainDataLength + mainDataLength.alignmentNeededFor(0x10).toUInt(), subDataLength)
    }

    open suspend fun SpiralContext.setup() {}
}

@ExperimentalUnsignedTypes
suspend fun BaseSrdEntry.setup(context: SpiralContext) = context.setup()