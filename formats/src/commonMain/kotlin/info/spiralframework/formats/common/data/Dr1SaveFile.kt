package info.spiralframework.formats.common.data

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.getOrThrow
import dev.brella.kornea.errors.common.useAndFlatMap
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.extensions.readAsciiString
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.io.common.flow.extensions.readInt64LE
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.base.common.trimNulls
import info.spiralframework.formats.common.withFormats

public class Dr1SaveFile {
    public companion object {
        public const val MAGIC_NUMBER: Long = 0x617461640000000C
        public const val BIN_MAGIC_NUMBER: Long = 0x3AC946E69622E

        public const val INVALID_MAGIC_NUMBER: Int = 0x0000

        public const val NOT_ENOUGH_DATA_KEY: String = "formats.saves.dr1.not_enough_data"
        public const val INVALID_MAGIC_NUMBER_KEY: String = "formats.saves.dr1.invalid_magic_number"

        public suspend operator fun invoke(
            context: SpiralContext,
            dataSource: DataSource<*>
        ): KorneaResult<Dr1SaveFile> = dataSource
            .openInputFlow()
            .useAndFlatMap { flow -> invoke(context, flow) }

        public suspend operator fun invoke(context: SpiralContext, flow: InputFlow): KorneaResult<Dr1SaveFile> {
            withFormats(context) {
                val magic = flow.readInt64LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                if (magic != MAGIC_NUMBER) {
                    return KorneaResult.errorAsIllegalArgument(
                        INVALID_MAGIC_NUMBER,
                        localise(INVALID_MAGIC_NUMBER_KEY, "0x${magic.toString(16).padStart(16, '0')}")
                    )
                }

                val fileNumber =
                    flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY) //.convertASCIIToInt()
//                require(fileNumber in FILE_NUMBER_RANGE) { localise("formats.save.dr1.range") }

                val binMagic = flow.readInt64LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                require(binMagic == BIN_MAGIC_NUMBER) { localise("formats.save.dr1.bin") }

//                val size = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                val danganronpaString =
                    flow.readAsciiString(64)?.trimNulls() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val chapterSectionString =
                    flow.readAsciiString(128)?.trimNulls() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val debugInfoString =
                    flow.readAsciiString(512)?.trimNulls() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                val lastPlayedString =
                    flow.readAsciiString(32)?.trimNulls() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                return KorneaResult.success(Dr1SaveFile())
            }
        }
    }
}

@Suppress("FunctionName")
public suspend fun SpiralContext.Dr1SaveFile(dataSource: DataSource<*>): KorneaResult<Dr1SaveFile> = Dr1SaveFile(this, dataSource)
@Suppress("FunctionName")
public suspend fun SpiralContext.Dr1SaveFile(flow: InputFlow): KorneaResult<Dr1SaveFile> = Dr1SaveFile(this, flow)

@Suppress("FunctionName")
public suspend fun SpiralContext.UnsafeDr1SaveFile(dataSource: DataSource<*>): Dr1SaveFile = Dr1SaveFile(this, dataSource).getOrThrow()
@Suppress("FunctionName")
public suspend fun SpiralContext.UnsafeDr1SaveFile(flow: InputFlow): Dr1SaveFile = Dr1SaveFile(this, flow).getOrThrow()