package info.spiralframework.formats.common.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.getValue
import info.spiralframework.base.common.properties.oneTimeMutable
import info.spiralframework.base.common.properties.setValue
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.BinaryInputFlow
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.readAndClose
import org.abimon.kornea.io.common.readInt16LE
import org.abimon.kornea.io.common.readInt32LE
import org.abimon.kornea.io.common.use

@ExperimentalUnsignedTypes
data class TextureSrdEntry(
        override val classifier: Int,
        override val mainDataLength: ULong,
        override val subDataLength: ULong,
        override val unknown: Int,
        override val dataSource: DataSource<*>
) : BaseSrdEntry(classifier, mainDataLength, subDataLength, unknown, dataSource) {
    companion object {
        const val MAGIC_NUMBER_BE = 0x24545852
    }

    var rsiEntry: RSISrdEntry by oneTimeMutable()
    val mipmaps: Array<RSISrdEntry.ResourceIndex>
        get() = rsiEntry.resources

    var unk1: Int by oneTimeMutable()
    var swizzle: Int by oneTimeMutable()
    var displayWidth: Int by oneTimeMutable()
    var displayHeight: Int by oneTimeMutable()
    var scanline: Int by oneTimeMutable()
    var format: Int by oneTimeMutable()
    var unk2: Int by oneTimeMutable()
    var palette: Int by oneTimeMutable()
    var paletteID: Int by oneTimeMutable()
    
    @ExperimentalStdlibApi
    override suspend fun SpiralContext.setup() {
        rsiEntry = RSISrdEntry.unsafe(this, openSubDataSource())

        val dataSource = openMainDataSource()
        if (dataSource.reproducibility.isRandomAccess())
            requireNotNull(dataSource.openInputFlow()).use { setup(it) }
        else {
            setup(BinaryInputFlow(requireNotNull(dataSource.openInputFlow()).readAndClose()))
        }
    }

    @ExperimentalStdlibApi
    private suspend fun SpiralContext.setup(flow: InputFlow) {
        requireNotNull(flow.seek(0, InputFlow.FROM_BEGINNING))

        unk1 = requireNotNull(flow.readInt32LE())
        swizzle = requireNotNull(flow.readInt16LE())
        displayWidth = requireNotNull(flow.readInt16LE())
        displayHeight = requireNotNull(flow.readInt16LE())
        scanline = requireNotNull(flow.readInt16LE())
        format = requireNotNull(flow.read()) and 0xFF
        unk2 = requireNotNull(flow.read()) and 0xFF
        palette = requireNotNull(flow.read()) and 0xFF
        paletteID = requireNotNull(flow.read()) and 0xFF
    }
}