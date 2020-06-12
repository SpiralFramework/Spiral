package info.spiralframework.osb.common

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.base.common.text.toHexString
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.erorrs.common.KorneaResult
import org.abimon.kornea.erorrs.common.cast
import org.abimon.kornea.erorrs.common.doOnFailure
import org.abimon.kornea.erorrs.common.map
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.readInt32LE
import org.abimon.kornea.io.common.use

class OpenSpiralBitcodeWrapper private constructor(val source: DataSource<*>) {
    companion object {
        @ExperimentalStdlibApi
        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<OpenSpiralBitcodeWrapper> {
            withFormats(context) {
                val flow = dataSource.openInputFlow().doOnFailure { return it.cast() }

                use(flow) {
                    val magic = flow.readInt32LE() ?: return localisedNotEnoughData(OpenSpiralBitcodeParser.NOT_ENOUGH_DATA_KEY)
                    if (magic != OpenSpiralBitcode.MAGIC_NUMBER_LE) {
                        return KorneaResult.Error(OpenSpiralBitcodeParser.INVALID_MAGIC, localise(OpenSpiralBitcodeParser.INVALID_MAGIC_KEY, magic.toHexString()))
                    }

                    return KorneaResult.Success(OpenSpiralBitcodeWrapper(dataSource))
                }
            }
        }
    }

    @ExperimentalStdlibApi
    @ExperimentalUnsignedTypes
    suspend inline fun <reified T : OpenSpiralBitcodeVisitor> parseBitcode(context: SpiralContext, visitor: T): KorneaResult<T> =
            source.openInputFlow().map { flow -> flow.parseOpenSpiralBitcode(context, visitor) }
}