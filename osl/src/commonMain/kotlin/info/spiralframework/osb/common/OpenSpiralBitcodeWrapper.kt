package info.spiralframework.osb.common

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.cast
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.errors.common.map
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import dev.brella.kornea.toolkit.common.closeAfter
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.base.common.text.toHexString
import info.spiralframework.formats.common.withFormats

class OpenSpiralBitcodeWrapper private constructor(val source: DataSource<*>) {
    companion object {
        @ExperimentalStdlibApi
        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<OpenSpiralBitcodeWrapper> =
            withFormats(context) {
                val flow = dataSource.openInputFlow()
                    .getOrBreak { return@withFormats it.cast() }

                closeAfter(flow) {
                    val magic = flow.readInt32LE() ?: return@closeAfter localisedNotEnoughData(OpenSpiralBitcodeParser.NOT_ENOUGH_DATA_KEY)
                    if (magic != OpenSpiralBitcode.MAGIC_NUMBER_LE) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(OpenSpiralBitcodeParser.INVALID_MAGIC, localise(OpenSpiralBitcodeParser.INVALID_MAGIC_KEY, magic.toHexString()))
                    }

                    return@closeAfter KorneaResult.success(OpenSpiralBitcodeWrapper(dataSource))
                }
            }
    }

    @ExperimentalStdlibApi
    @ExperimentalUnsignedTypes
    suspend inline fun <reified T : OpenSpiralBitcodeVisitor> parseBitcode(context: SpiralContext, visitor: T): KorneaResult<T> =
            source.openInputFlow().map { flow -> flow.parseOpenSpiralBitcode(context, visitor) }
}