package info.spiralframework.formats.common.archives.srd

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.io.common.EnumSeekMode
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.SeekableInputFlow
import dev.brella.kornea.io.common.flow.bookmark
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.extensions.readNullTerminatedString
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData

public data class MaterialsSrdEntry(
        override val classifier: Int,
        override val mainDataLength: ULong,
        override val subDataLength: ULong,
        override val unknown: Int
) : SrdEntryWithData.WithRsiSubdata(classifier, mainDataLength, subDataLength, unknown) {
    public companion object {
        public const val MAGIC_NUMBER_BE: Int = 0x244D4154
    }

    private val materialsMutable: MutableMap<String, String> = HashMap()
    val materials: Map<String, String> = materialsMutable

    override suspend fun SpiralContext.setup(flow: SeekableInputFlow): KorneaResult<MaterialsSrdEntry> {
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

    override suspend fun SpiralContext.writeMainData(out: OutputFlow) {
        TODO("Not yet implemented")
    }

    override suspend fun SpiralContext.writeSubData(out: OutputFlow) {
        TODO("Not yet implemented")
    }
}