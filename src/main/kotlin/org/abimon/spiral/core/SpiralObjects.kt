package org.abimon.spiral.core

import org.abimon.spiral.util.CountingInputStream
import org.abimon.visi.collections.toArrayString
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.readPartialBytes
import org.abimon.visi.io.skipBytes
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.set

val STEAM_DANGANRONPA_TRIGGER_HAPPY_HAVOC = "413410"
val STEAM_DANGANRONPA_2_GOODBYE_DESPAIR = "413420"
val spiralHeaderName = "Spiral-Header"
var isDebug = false

/** Vita */

class CPK(val dataSource: DataSource) {

    val fileTable = LinkedList<CPKFileEntry>()
    val cpkData = HashMap<String, ByteArray>()
    val cpkType = HashMap<String, CPKType>()
    val utf: CPKUTF

    init {
        val cpk = CountingInputStream(dataSource.inputStream)

        val cpkMagic = cpk.readString(4)

        if (cpkMagic != "CPK ")
            throw IllegalArgumentException("${dataSource.location} is either not a CPK file, or a corrupted/invalid one!")

        cpk.readNumber(4)

        val utfSize = cpk.readNumber(8, true)

        val cpakEntry = CPKFileEntry(fileName = "CPK_HDR", fileOffset = 0x10, fileSize = utfSize, fileType = "CPK")
        fileTable.add(cpakEntry)

        utf = CPKUTF(cpk.readPartialBytes(utfSize.toInt()), this)

        for (i in 0 until utf.columns.size) {
            cpkData[utf.columns[i].name] = utf.rows[0][i].data
            cpkType[utf.columns[i].name] = utf.rows[0][i].type
        }

        cpkData.forEach { s, bytes -> println("$s: ${bytes.toArrayString()}") }

        println(cpkData["ContentSize"]!!.inputStream().readNumber(8))
    }
}

class CPKUTF(val packet: ByteArray, val cpk: CPK) {

    enum class ColumnFlags(val flag: Int) {
        STORAGE_MASK(0xf0),
        STORAGE_NONE(0x00),
        STORAGE_ZERO(0x10),
        STORAGE_CONSTANT(0x30),
        STORAGE_PERROW(0x50),

        TYPE_MASK(0x0f),
        TYPE_DATA(0x0b),
        TYPE_STRING(0x0a),
        TYPE_FLOAT(0x08),
        TYPE_8BYTE2(0x07),
        TYPE_8BYTE(0x06),
        TYPE_4BYTE2(0x05),
        TYPE_4BYTE(0x04),
        TYPE_2BYTE2(0x03),
        TYPE_2BYTE(0x02),
        TYPE_1BYTE2(0x01),
        TYPE_1BYTE(0x00);

        operator fun component1(): Int = flag
    }

    val tableSize: Int
    val rowsOffset: Int
    val stringsOffset: Int
    val dataOffset: Int

    val tableNameLen: Int
    val numColumns: Int
    val rowLength: Int
    val numRows: Int

    val columns = LinkedList<CPKColumn>()
    val rows = LinkedList<LinkedList<CPKRow>>()

    init {
        val utf = packet.inputStream()
        if (utf.readString(4) != "@UTF")
            throw IllegalArgumentException("${cpk.dataSource.location} is either not a CPK file, or a corrupted/invalid one!")

        println(packet.copyOfRange(0, 16).toArrayString())

        tableSize = (utf.readNumber(4) + 8).toInt()
        rowsOffset = (utf.readNumber(4) + 8).toInt()
        stringsOffset = (utf.readNumber(4) + 8).toInt()
        dataOffset = (utf.readNumber(4) + 8).toInt()

        tableNameLen = utf.readNumber(4).toInt()
        numColumns = utf.readNumber(2).toInt()
        rowLength = utf.readNumber(2).toInt()
        numRows = utf.readNumber(4).toInt()

        for (i in 0 until numColumns) {
            val column = CPKColumn()
            column.flags = utf.read()
            if (column.flags == 0) {
                utf.skip(3)
                column.flags = utf.read()
            }

            column.name = packet.inputStream().skipBytes(utf.readNumber(4) + stringsOffset).readZeroString()
            columns.add(column)
        }

        for (x in 0 until numRows) {
            val currentEntry = LinkedList<CPKRow>()
            val pos = (rowsOffset + (x * rowLength)).toLong()
            val rowInputStream = packet.inputStream().skipBytes(pos)

            loop@ for (y in 0 until numColumns) {
                val currentRow = CPKRow()
                val storageFlag = columns[y].flags and ColumnFlags.STORAGE_MASK.flag

                when (storageFlag) {
                    ColumnFlags.STORAGE_NONE.flag -> {
                        currentEntry.add(currentRow)
                        continue@loop
                    }

                    ColumnFlags.STORAGE_ZERO.flag -> {
                        currentEntry.add(currentRow)
                        continue@loop
                    }

                    ColumnFlags.STORAGE_CONSTANT.flag -> {
                        currentEntry.add(currentRow)
                        continue@loop
                    }
                }

                currentRow.type = CPKType.getValue(columns[y].flags and ColumnFlags.TYPE_MASK.flag)
                currentRow.position = pos

                when (currentRow.type) {
                    CPKType.UINT8 -> currentRow.data = rowInputStream.read().toByte().write()
                    CPKType.UINT16 -> currentRow.data = rowInputStream.readNumber(2).write()
                    CPKType.UINT32 -> currentRow.data = rowInputStream.readNumber(4).write()
                    CPKType.UINT64 -> currentRow.data = rowInputStream.readNumber(8).write()
                    CPKType.UFLOAT -> println("FLOAT!")
                    CPKType.STR -> packet.inputStream().skipBytes(rowInputStream.readNumber(4) + stringsOffset).readZeroString()
                    CPKType.DATA -> {
                        val position = rowInputStream.readNumber(4) + dataOffset
                        currentRow.position = position
                        currentRow.data = packet.copyOfRange(position.toInt(), rowInputStream.readNumber(4).toInt())
                    }
                }

                currentEntry.add(currentRow)
            }
            rows.add(currentEntry)
        }
    }
}

enum class CPKType(vararg val types: Int) {
    UINT8(0, 1),
    UINT16(2, 3),
    UINT32(4, 5),
    UINT64(6, 7),
    UFLOAT(8),
    STR(0xA),
    DATA(0xB);

    companion object {
        fun getValue(type: Int): CPKType {
            val cpkType = values().firstOrNull { it.types.contains(type) } ?: DATA
            return cpkType
        }
    }
}

data class CPKColumn(var flags: Int = 0, var name: String = "")

class CPKRow(var type: CPKType = CPKType.DATA, var data: ByteArray = ByteArray(0), var position: Long = 0) {
    operator fun component1(): CPKType = type
    operator fun component2(): Any {
        when (type) {
            CPKType.UINT8 -> return data.inputStream().readNumber(1, true)
            CPKType.UINT16 -> return data.inputStream().readNumber(2, true)
            CPKType.UINT32 -> return data.inputStream().readNumber(4, true)
            CPKType.UINT64 -> return data.inputStream().readNumber(8, true)
            CPKType.UFLOAT -> return data.inputStream().readFloat(true)
            CPKType.STR -> return String(data, Charset.forName("UTF-8"))
            CPKType.DATA -> return data
        }
    }
}

data class CPKFileEntry(var dirName: String = "", var fileName: String = "", var fileSize: Long = 0, var extractSize: Long = 0, var id: Long = 0, var userString: String = "", var localDir: String = "", var fileOffset: Long = 0, var fileType: String = "")