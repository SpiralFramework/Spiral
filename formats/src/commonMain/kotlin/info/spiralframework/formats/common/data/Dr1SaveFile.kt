package info.spiralframework.formats.common.data

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.readAsciiString
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.base.common.trimNulls
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.errors.common.KorneaResult
import org.abimon.kornea.io.common.*
import org.abimon.kornea.io.common.flow.InputFlow

@ExperimentalUnsignedTypes
class Dr1SaveFile {
    companion object {
        const val MAGIC_NUMBER = 0x617461640000000C
        const val BIN_MAGIC_NUMBER = 0x3AC946E69622E.toInt()

        const val INVALID_MAGIC_NUMBER = 0x0000

        const val NOT_ENOUGH_DATA_KEY = "formats.saves.dr1.not_enough_data"
        const val INVALID_MAGIC_NUMBER_KEY = "formats.saves.dr1.invalid_magic_number"

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<Dr1SaveFile> = dataSource.openInputFlow().useAndFlatMap { flow -> invoke(context, flow) }
        suspend operator fun invoke(context: SpiralContext, flow: InputFlow): KorneaResult<Dr1SaveFile> {
            withFormats(context) {
                val magic = flow.readInt64LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                if (magic != MAGIC_NUMBER) {
                    return KorneaResult.errorAsIllegalArgument(INVALID_MAGIC_NUMBER, localise(INVALID_MAGIC_NUMBER_KEY, "0x${magic.toString(16).padStart(16, '0')}"))
                }

                val fileNumber = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY) //.convertASCIIToInt()
//                require(fileNumber in FILE_NUMBER_RANGE) { localise("formats.save.dr1.range") }

                val binMagic = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                require(binMagic == BIN_MAGIC_NUMBER) { localise("formats.save.dr1.bin") }

                val size = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val danganronpaString = flow.readAsciiString(64)?.trimNulls() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val chapterSectionString = flow.readAsciiString(128)?.trimNulls() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val debugInfoString = flow.readAsciiString(512)?.trimNulls() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                val lastPlayedString = flow.readAsciiString(32)?.trimNulls() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                return KorneaResult.success(Dr1SaveFile())
            }
        }
    }
}

@ExperimentalUnsignedTypes
suspend fun SpiralContext.Dr1SaveFile(dataSource: DataSource<*>) = Dr1SaveFile(this, dataSource)
@ExperimentalUnsignedTypes
suspend fun SpiralContext.Dr1SaveFile(flow: InputFlow) = Dr1SaveFile(this, flow)
@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeDr1SaveFile(dataSource: DataSource<*>) = Dr1SaveFile(this, dataSource).get()
@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeDr1SaveFile(flow: InputFlow) = Dr1SaveFile(this, flow).get()