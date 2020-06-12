package info.spiralframework.formats.common.data

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.*
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.base.common.useAndFlatMap
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.erorrs.common.KorneaResult
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.readFloatLE
import org.abimon.kornea.io.common.readInt32LE
import org.abimon.kornea.io.common.useInputFlow

@ExperimentalUnsignedTypes
class Dr1RoomObject(val unk1: Int, val id: Int, val modelID: Int, val x: Float, val y: Float, val z: Float, val width: Float, val height: Float, val perspective: Float, val unk4: Int) {
    companion object {
        const val PREFIX = "formats.room_object.dr1"
        const val NOT_ENOUGH_DATA_KEY = "$PREFIX.not_enough_data"

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<Dr1RoomObject> = dataSource.openInputFlow().useAndFlatMap { flow -> invoke(context, flow) }
        suspend operator fun invoke(context: SpiralContext, flow: InputFlow): KorneaResult<Dr1RoomObject> {
            withFormats(context) {
                val unk1 = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val id = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val modelID = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                val x = flow.readFloatLE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val y = flow.readFloatLE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val z = flow.readFloatLE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                val width = flow.readFloatLE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val height = flow.readFloatLE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                val perspective = flow.readFloatLE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                val unk4 = flow.readInt32LE() ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                return KorneaResult.Success(Dr1RoomObject(unk1, id, modelID, x, y, z, width, height, perspective, unk4))
            }
        }
    }
}

@ExperimentalUnsignedTypes
suspend fun SpiralContext.Dr1RoomObject(dataSource: DataSource<*>) = Dr1RoomObject(this, dataSource)

@ExperimentalUnsignedTypes
suspend fun SpiralContext.Dr1RoomObject(flow: InputFlow) = Dr1RoomObject(this, flow)

@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeDr1RoomObject(dataSource: DataSource<*>) = Dr1RoomObject(this, dataSource).get()

@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeDr1RoomObject(flow: InputFlow) = Dr1RoomObject(this, flow).get()