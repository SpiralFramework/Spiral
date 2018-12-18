package org.abimon.spiral.core.objects.models

import org.abimon.spiral.core.objects.ICompilable
import org.abimon.spiral.core.utils.readFloatLE
import org.abimon.spiral.core.utils.readInt32LE
import org.abimon.spiral.core.utils.writeFloatLE
import org.abimon.spiral.core.utils.writeInt32LE
import java.io.InputStream
import java.io.OutputStream

data class RoomObject(var unk1: Int, var id: Int, var modelID: Int, var x: Float, var y: Float, var z: Float, var width: Float, var height: Float, var perspective: Float, var unk4: Int) : ICompilable {
    companion object {
        operator fun invoke(dataSource: () -> InputStream): RoomObject =
                dataSource().use { stream ->
                    val unk1 = stream.readInt32LE()
                    val id = stream.readInt32LE()
                    val modelID = stream.readInt32LE()

                    val x = stream.readFloatLE()
                    val y = stream.readFloatLE()
                    val z = stream.readFloatLE()

                    val width = stream.readFloatLE()
                    val height = stream.readFloatLE()
                    val perspective = stream.readFloatLE()

                    val unk4 = stream.readInt32LE()

                    return@use RoomObject(unk1, id, modelID, x, y, z, width, height, perspective, unk4)
                }
    }

    override val dataSize: Long = 40L
    override fun compile(output: OutputStream) {
        output.writeInt32LE(unk1)
        output.writeInt32LE(id)
        output.writeInt32LE(modelID)

        output.writeFloatLE(x)
        output.writeFloatLE(y)
        output.writeFloatLE(z)

        output.writeFloatLE(width)
        output.writeFloatLE(height)
        output.writeFloatLE(perspective)

        output.writeInt32LE(unk4)
    }
}