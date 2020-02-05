package info.spiralframework.formats.common.data

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.*
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.io.common.DataSource
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.readFloatLE
import org.abimon.kornea.io.common.readInt32LE
import org.abimon.kornea.io.common.useInputFlow

@ExperimentalUnsignedTypes
class Dr1RoomObject(val unk1: Int, val id: Int, val modelID: Int, val x: Float, val y: Float, val z: Float, val width: Float, val height: Float, val perspective: Float, val unk4: Int) {
    companion object {
        const val PREFIX = "formats.room_object.dr1"

        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): Dr1RoomObject? = dataSource.useInputFlow { flow -> invoke(context, flow) }
        suspend operator fun invoke(context: SpiralContext, flow: InputFlow): Dr1RoomObject? {
            try {
                return unsafe(context, flow)
            } catch (iae: IllegalArgumentException) {
                withFormats(context) { debug("$PREFIX.invalid", flow, iae) }

                return null
            }
        }

        suspend fun unsafe(context: SpiralContext, dataSource: DataSource<*>): Dr1RoomObject = requireNotNull(dataSource.useInputFlow { flow -> unsafe(context, flow) })
        suspend fun unsafe(context: SpiralContext, flow: InputFlow): Dr1RoomObject {
            withFormats(context) {
                val notEnoughData: () -> Any = { localise("$PREFIX.not_enough_data") }

                val unk1 = requireNotNull(flow.readInt32LE(), notEnoughData)
                val id = requireNotNull(flow.readInt32LE(), notEnoughData)
                val modelID = requireNotNull(flow.readInt32LE(), notEnoughData)

                val x = requireNotNull(flow.readFloatLE(), notEnoughData)
                val y = requireNotNull(flow.readFloatLE(), notEnoughData)
                val z = requireNotNull(flow.readFloatLE(), notEnoughData)

                val width = requireNotNull(flow.readFloatLE(), notEnoughData)
                val height = requireNotNull(flow.readFloatLE(), notEnoughData)
                val perspective = requireNotNull(flow.readFloatLE(), notEnoughData)

                val unk4 = requireNotNull(flow.readInt32LE(), notEnoughData)

                return Dr1RoomObject(unk1, id, modelID, x, y, z, width, height, perspective, unk4)
            }
        }
    }
}

@ExperimentalUnsignedTypes
suspend fun SpiralContext.Dr1RoomObject(dataSource: DataSource<*>) = Dr1RoomObject(this, dataSource)
@ExperimentalUnsignedTypes
suspend fun SpiralContext.Dr1RoomObject(flow: InputFlow) = Dr1RoomObject(this, flow)
@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeDr1RoomObject(dataSource: DataSource<*>) = Dr1RoomObject.unsafe(this, dataSource)
@ExperimentalUnsignedTypes
suspend fun SpiralContext.UnsafeDr1RoomObject(flow: InputFlow) = Dr1RoomObject.unsafe(this, flow)