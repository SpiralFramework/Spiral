package info.spiralframework.formats.common.archives.srd

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.readNullTerminatedString
import info.spiralframework.base.common.locale.localisedNotEnoughData
import org.abimon.kornea.errors.common.KorneaResult
import org.abimon.kornea.errors.common.cast
import org.abimon.kornea.errors.common.getOrBreak
import org.abimon.kornea.errors.common.filterToInstance
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.EnumSeekMode
import org.abimon.kornea.io.common.flow.BinaryInputFlow
import org.abimon.kornea.io.common.flow.SeekableInputFlow
import org.abimon.kornea.io.common.flow.bookmark
import org.abimon.kornea.io.common.flow.readBytes
import org.abimon.kornea.io.common.readInt16LE
import org.abimon.kornea.io.common.useAndFlatMap
import org.kornea.toolkit.common.oneTimeMutableInline

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

    var rsiEntry: RSISrdEntry by oneTimeMutableInline()
    private val materialsMutable: MutableMap<String, String> = HashMap()
    val materials: Map<String, String> = materialsMutable

    @ExperimentalStdlibApi
    override suspend fun SpiralContext.setup(): KorneaResult<MaterialsSrdEntry> {
        rsiEntry = RSISrdEntry(this, openSubDataSource()).getOrBreak { return it.cast() }

        val dataSource = openMainDataSource()
        if (dataSource.reproducibility.isRandomAccess())
            return dataSource.openInputFlow().filterToInstance<SeekableInputFlow>().useAndFlatMap { flow -> setup(flow) }
        else {
            return dataSource.openInputFlow().useAndFlatMap { flow -> setup(BinaryInputFlow(flow.readBytes())) }
        }
    }

    @ExperimentalStdlibApi
    private suspend fun SpiralContext.setup(flow: SeekableInputFlow): KorneaResult<MaterialsSrdEntry> {
        flow.seek(0, EnumSeekMode.FROM_BEGINNING)
        materialsMutable.clear()

        flow.skip(20uL)
        val materialsOffset = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        val materialsCount = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        flow.seek(materialsOffset.toLong(), EnumSeekMode.FROM_BEGINNING)
        for (i in 0 until materialsCount) {
            val textureNameOffset = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
            val materialTypeOffset = flow.readInt16LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

            bookmark(flow) {
                flow.seek(textureNameOffset.toLong(), EnumSeekMode.FROM_BEGINNING)
                val textureName = flow.readNullTerminatedString()

                flow.seek(materialTypeOffset.toLong(), EnumSeekMode.FROM_BEGINNING)
                val materialType = flow.readNullTerminatedString()

                materialsMutable[materialType] = textureName
            }
        }

        return KorneaResult.success(this@MaterialsSrdEntry)
    }
}