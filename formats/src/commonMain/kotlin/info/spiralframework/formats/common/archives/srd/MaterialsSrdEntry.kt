package info.spiralframework.formats.common.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.readNullTerminatedString
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.BinaryInputFlow
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.bookmark
import org.abimon.kornea.io.common.flow.readBytes
import org.abimon.kornea.io.common.readInt16LE
import org.abimon.kornea.io.common.use

@ExperimentalUnsignedTypes
data class MaterialsSrdEntry(
        override val classifier: Int,
        override val mainDataLength: ULong,
        override val subDataLength: ULong,
        override val unknown: Int,
        override val dataSource: DataSource<*>
) : BaseSrdEntry(classifier, mainDataLength, subDataLength, unknown, dataSource) {
    companion object {
        const val MAGIC_NUMBER_BE = 0x244D4154
    }

    private val materialsMutable: MutableMap<String, String> = HashMap()
    val materials: Map<String, String> = materialsMutable

    @ExperimentalStdlibApi
    override suspend fun SpiralContext.setup() {
        val dataSource = openMainDataSource()
        if (dataSource.reproducibility.isRandomAccess())
            dataSource.openInputFlow()?.use { setup(it) }
        else {
            setup(BinaryInputFlow(dataSource.openInputFlow()?.use { it.readBytes() } ?: return))
        }
    }

    @ExperimentalStdlibApi
    private suspend fun SpiralContext.setup(flow: InputFlow) {
        requireNotNull(flow.seek(0, InputFlow.FROM_BEGINNING))
        materialsMutable.clear()

        flow.skip(20uL)
        val materialsOffset = requireNotNull(flow.readInt16LE())
        val materialsCount = requireNotNull(flow.readInt16LE())

        flow.seek(materialsOffset.toLong(), InputFlow.FROM_BEGINNING)
        for (i in 0 until materialsCount) {
            val textureNameOffset = requireNotNull(flow.readInt16LE())
            val materialTypeOffset = requireNotNull(flow.readInt16LE())

            bookmark(flow) {
                flow.seek(textureNameOffset.toLong(), InputFlow.FROM_BEGINNING)
                val textureName = flow.readNullTerminatedString()

                flow.seek(materialTypeOffset.toLong(), InputFlow.FROM_BEGINNING)
                val materialType = flow.readNullTerminatedString()

                materialsMutable[materialType] = textureName
            }
        }
    }
}