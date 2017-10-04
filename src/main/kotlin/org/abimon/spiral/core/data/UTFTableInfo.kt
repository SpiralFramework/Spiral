package org.abimon.spiral.core.data

import org.abimon.spiral.core.objects.CPK
import org.abimon.spiral.core.readNumber
import org.abimon.spiral.util.SeekableInputStream
import org.abimon.spiral.util.debug
import org.abimon.visi.io.DataSource
import org.abimon.visi.lang.and

data class UTFTableInfo(
        var tableOffset: Long = -1,
        var tableSize: Long = -1,
        var schemaOffset: Long = -1,
        var rowsOffset: Long = -1,
        var stringTableOffset: Long = -1,
        var dataOffset: Long = -1,
        var stringTable: String = "",
        var tableName: String = "",
        var columns: Int = -1,
        var rowWidth: Int = -1,
        var rows: Long = -1,
        var schema: Array<UTFColumnInfo> = emptyArray()
) {
    fun getRows(dataSource: DataSource, column: String): List<Triple<Int, CPKColumnType, Any>> {
        val data: MutableList<Triple<Int, CPKColumnType, Any>> = ArrayList()
        dataSource.seekableInputStream.use {
            val stream = SeekableInputStream(it)
            for (i in 0 until rows.toInt()) {
                var rowOffset = tableOffset + 8 + rowsOffset + (i * rowWidth)
                loop@ for (j in 0 until columns) {
                    var constant = false

                    when (schema[j].type and CPK.COLUMN_STORAGE_MASK) {
                        CPK.COLUMN_STORAGE_PERROW -> {
                        }
                        CPK.COLUMN_STORAGE_CONSTANT -> constant = true
                        CPK.COLUMN_STORAGE_ZERO -> {
                            if (schema[j].columnName == column)
                                data.add(i to CPKColumnType.getForMask(schema[j].type) and 0)
                            continue@loop
                        }
                        else -> debug("Unknown storage class")
                    }

                    val dataOffset = if (constant) schema[j].constantOffset else rowOffset
                    stream.seek(dataOffset)

                    val dataPair = i to CPKColumnType.getForMask(schema[j].type)
                    val dataObj: Any
                    val bytesRead: Int

                    when (dataPair.second) {
                        CPKColumnType.TYPE_STRING -> {
                            val num = stream.readNumber(4, unsigned = true, little = false)
                            dataObj = num
                            bytesRead = 4
                        }
                        CPKColumnType.TYPE_DATA -> {
                            dataObj = (stream.readNumber(4, unsigned = true, little = false) to stream.readNumber(4, unsigned = true, little = false))
                            bytesRead = 8
                        }
                        CPKColumnType.TYPE_8BYTE -> {
                            dataObj = stream.readNumber(8, unsigned = true, little = false)
                            bytesRead = 8
                        }
                        CPKColumnType.TYPE_4BYTE2 -> {
                            dataObj = stream.readNumber(4, unsigned = true, little = false)
                            bytesRead = 4
                        }
                        CPKColumnType.TYPE_4BYTE -> {
                            dataObj = stream.readNumber(4, unsigned = true, little = false)
                            bytesRead = 4
                        }
                        CPKColumnType.TYPE_2BYTE2 -> {
                            dataObj = stream.readNumber(2, unsigned = true, little = false)
                            bytesRead = 2
                        }
                        CPKColumnType.TYPE_2BYTE -> {
                            dataObj = stream.readNumber(2, unsigned = true, little = false)
                            bytesRead = 2
                        }
                        CPKColumnType.TYPE_FLOAT -> {
                            dataObj = (0.0f to stream.readNumber(4, unsigned = true, little = false))
                            bytesRead = 4
                        }
                        CPKColumnType.TYPE_1BYTE2 -> {
                            dataObj = (stream.read() and 0xFF)
                            bytesRead = 1
                        }
                        CPKColumnType.TYPE_1BYTE -> {
                            dataObj = (stream.read() and 0xFF)
                            bytesRead = 1
                        }
                        else -> {
                            debug("Unknown normal type")
                            dataObj = 0
                            bytesRead = 0
                        }
                    }

                    if (!constant)
                        rowOffset += bytesRead

                    if (schema[j].columnName == column)
                        data.add(dataPair and (if (dataPair.second == CPKColumnType.TYPE_STRING) CPK.sanitiseStringTable(stringTable, (dataObj as Number).toInt()) else dataObj))
                }
            }
        }

        return data
    }

    fun dump(data: DataSource): Map<String, List<Triple<Int, CPKColumnType, Any>>> = schema.map { it.columnName to getRows(data, it.columnName) }.toMap()
}