package info.spiralframework.formats.common.data

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.getOrThrow
import dev.brella.kornea.errors.common.useAndFlatMap
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.extensions.readFloatLE
import dev.brella.kornea.io.common.flow.extensions.readInt32LE
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats

public class Dr1RoomObject(
    public val unk1: Int,
    public val id: Int,
    public val modelID: Int,
    public val x: Float,
    public val y: Float,
    public val z: Float,
    public val width: Float,
    public val height: Float,
    public val perspective: Float,
    public val unk4: Int
) {
    public companion object {
        public const val PREFIX: String = "formats.room_object.dr1"
        public const val NOT_ENOUGH_DATA_KEY: String = "$PREFIX.not_enough_data"

        public suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<Dr1RoomObject> =
            dataSource.openInputFlow().useAndFlatMap { flow -> invoke(context, flow) }

        public suspend operator fun invoke(context: SpiralContext, flow: InputFlow): KorneaResult<Dr1RoomObject> {
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

                return KorneaResult.success(Dr1RoomObject(unk1, id, modelID, x, y, z, width, height, perspective, unk4))
            }
        }
    }
}

@Suppress("FunctionName")
public suspend fun SpiralContext.Dr1RoomObject(dataSource: DataSource<*>): KorneaResult<Dr1RoomObject> = Dr1RoomObject(this, dataSource)
@Suppress("FunctionName")
public suspend fun SpiralContext.Dr1RoomObject(flow: InputFlow): KorneaResult<Dr1RoomObject> = Dr1RoomObject(this, flow)

@Suppress("FunctionName")
public suspend fun SpiralContext.UnsafeDr1RoomObject(dataSource: DataSource<*>): Dr1RoomObject = Dr1RoomObject(this, dataSource).getOrThrow()
@Suppress("FunctionName")
public suspend fun SpiralContext.UnsafeDr1RoomObject(flow: InputFlow): Dr1RoomObject = Dr1RoomObject(this, flow).getOrThrow()