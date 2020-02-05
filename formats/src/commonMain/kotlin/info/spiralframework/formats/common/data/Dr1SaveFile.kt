package info.spiralframework.formats.common.data

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.readAsciiString
import info.spiralframework.base.common.trimNulls
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.readInt32LE
import org.abimon.kornea.io.common.readInt64LE
import org.abimon.kornea.io.common.useInputFlow

@ExperimentalUnsignedTypes
class Dr1SaveFile {
    companion object {
        const val MAGIC_NUMBER = 0x617461640000000C
        const val BIN_MAGIC_NUMBER = 0x3AC946E69622E.toInt()

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): Dr1SaveFile? = dataSource.useInputFlow { flow -> invoke(context, flow) }
        suspend operator fun invoke(context: SpiralContext, flow: InputFlow): Dr1SaveFile? {
            try {
                return unsafe(context, flow)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.saves.dr1.invalid", flow, iae) }

                return null
            }
        }

        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): Dr1SaveFile = requireNotNull(dataSource.useInputFlow { flow -> unsafe(context, flow) })
        suspend fun unsafe(context: SpiralContext, flow: InputFlow): Dr1SaveFile {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("formats.saves.dr1.not_enough_data") }

                val magic = requireNotNull(flow.readInt64LE(), notEnoughData)
                require(magic == MAGIC_NUMBER) { localise("formats.save.dr1") }

                val fileNumber = requireNotNull(flow.readInt32LE(), notEnoughData) //.convertASCIIToInt()
//                require(fileNumber in FILE_NUMBER_RANGE) { localise("formats.save.dr1.range") }

                val binMagic = requireNotNull(flow.readInt32LE(), notEnoughData)
                require(binMagic == BIN_MAGIC_NUMBER) { localise("formats.save.dr1.bin") }

                val size = requireNotNull(flow.readInt32LE(), notEnoughData)
                val danganronpaString = requireNotNull(flow.readAsciiString(64), notEnoughData).trimNulls()
                val chapterSectionString = requireNotNull(flow.readAsciiString(128), notEnoughData).trimNulls()
                val debugInfoString = requireNotNull(flow.readAsciiString(512), notEnoughData).trimNulls()

                val lastPlayedString = requireNotNull(flow.readAsciiString(32), notEnoughData).trimNulls()

                return Dr1SaveFile()
            }
        }
    }
}

@ExperimentalUnsignedTypes
suspend fun SpiralContext.Dr1SaveFile(dataSource: DataSource<*>) = Dr1SaveFile(this, dataSource)
@ExperimentalUnsignedTypes
suspend fun SpiralContext.Dr1SaveFile(flow: InputFlow) = Dr1SaveFile(this, flow)
@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeDr1SaveFile(dataSource: DataSource<*>) = Dr1SaveFile.unsafe(this, dataSource)
@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeDr1SaveFile(flow: InputFlow) = Dr1SaveFile.unsafe(this, flow)