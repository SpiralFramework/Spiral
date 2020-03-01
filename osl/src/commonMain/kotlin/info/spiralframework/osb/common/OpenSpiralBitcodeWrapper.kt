package info.spiralframework.osb.common

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.readInt32LE
import org.abimon.kornea.io.common.use

class OpenSpiralBitcodeWrapper private constructor(val source: DataSource<*>) {
    companion object {
        @ExperimentalStdlibApi
        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): OpenSpiralBitcodeWrapper? {
            try {
                return unsafe(context, dataSource)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("formats.lin.invalid", dataSource, iae) }

                return null
            }
        }

        @ExperimentalStdlibApi
        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): OpenSpiralBitcodeWrapper {
            withFormats(context) {
                val flow = requireNotNull(dataSource.openInputFlow())

                use(flow) {
                    val magic = requireNotNull(flow.readInt32LE()) { context.localise("${OpenSpiralBitcodeParser.PREFIX}.not_enough_data") }
                    require(magic == OpenSpiralBitcode.MAGIC_NUMBER_LE) { context.localise("${OpenSpiralBitcodeParser.PREFIX}.invalid_magic") }

                    return OpenSpiralBitcodeWrapper(dataSource)
                }
            }
        }
    }

    @ExperimentalStdlibApi
    @ExperimentalUnsignedTypes
    suspend fun <T : OpenSpiralBitcodeVisitor> parseBitcode(context: SpiralContext, visitor: T): T? =
            source.openInputFlow()?.use { flow -> flow.parseOpenSpiralBitcode(context, visitor) }
}