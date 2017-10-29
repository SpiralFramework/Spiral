package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.byteArrayOfInts
import org.abimon.spiral.core.writeInt
import org.abimon.spiral.core.writeShort
import org.abimon.visi.io.DataSource
import java.io.OutputStream
import java.util.*

class CustomSPC {
    companion object {
        val SPC_MAGIC = byteArrayOfInts(0x43, 0x50, 0x53, 0x2E)
        val SPC_MAGIC_PADDING = byteArrayOfInts(
                0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF,
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        )

        val SPC_FILECOUNT_PADDING = byteArrayOfInts(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)

        val SPC_TABLE_MAGIC = byteArrayOfInts(0x52, 0x6F, 0x6F, 0x74)
        val SPC_TABLE_PADDING = byteArrayOfInts(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        
        val SPC_ENTRY_PADDING = byteArrayOfInts(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
    }


    val data: MutableMap<String, DataSource> = HashMap<String, DataSource>()

    fun file(name: String, dataSource: DataSource): CustomSPC {
        data[name] = dataSource

        return this
    }

    fun compile(spc: OutputStream) {
        spc.write(SPC_MAGIC)
        spc.write(SPC_MAGIC_PADDING)

        spc.writeInt(data.size)
        spc.writeInt(4)
        spc.write(SPC_FILECOUNT_PADDING)

        spc.write(SPC_TABLE_MAGIC)
        spc.write(SPC_TABLE_PADDING)

        data.forEach { entryName, entryData ->
            val entryNameBytes = entryName.toByteArray(Charsets.UTF_8)
            val dataSize = entryData.size
            spc.writeShort(0x01)
            spc.writeShort(0x04)
            spc.writeInt(dataSize)
            spc.writeInt(dataSize)
            spc.writeInt(entryNameBytes.size)
            spc.write(SPC_ENTRY_PADDING)
            spc.write(entryNameBytes)
            spc.write(0x00)

            for(i in 0 until (0x10 - (entryNameBytes.size + 1) % 0x10) % 0x10)
                spc.write(0x00)

            entryData.pipe(spc)

            for(i in 0 until (0x10 - dataSize % 0x10) % 0x10)
                spc.write(0x00)
        }
    }
}