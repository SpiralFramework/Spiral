package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.utils.*
import java.io.IOException
import java.io.InputStream

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
    fun getRows(dataSource: () -> InputStream, column: String): List<Triple<Int, CPKColumnType, Any>> {
        val data: MutableList<Triple<Int, CPKColumnType, Any>> = ArrayList()
        for (i in 0 until rows.toInt()) {
            val (a, b) = getRow(dataSource, i, column) ?: continue
            data.add(i to a and b)
        }

        return data
    }

    fun getRow(dataSource: () -> InputStream, row: Int, column: String): Pair<CPKColumnType, Any>? {
        var rowOffset = tableOffset + 8 + rowsOffset + (row * rowWidth)

        loop@ for (j in 0 until columns) {
            dataSource().use { stream ->
                var constant = false

                when (schema[j].type and CPK.COLUMN_STORAGE_MASK) {
                    CPK.COLUMN_STORAGE_PERROW -> {
                    }
                    CPK.COLUMN_STORAGE_CONSTANT -> constant = true
                    CPK.COLUMN_STORAGE_ZERO -> {
                        if (schema[j].columnName == column)
                            return CPKColumnType.getForMask(schema[j].type) to 0
                        return@use
                    }
                    else -> println("Unknown storage class")
                }

                val dataOffset = if (constant) schema[j].constantOffset else rowOffset
                try { stream.skip(dataOffset) }
                catch (io: IOException) { io.printStackTrace() }

                val columnType = CPKColumnType.getForMask(schema[j].type)
                val dataObj: Any
                val bytesRead: Int

                when (columnType) {
                    CPKColumnType.TYPE_STRING -> {
                        val num = stream.readInt32BE()
                        dataObj = num
                        bytesRead = 4
                    }
                    CPKColumnType.TYPE_DATA -> {
                        dataObj = (stream.readInt32BE() to stream.readInt32BE())
                        bytesRead = 8
                    }
                    CPKColumnType.TYPE_8BYTE -> {
                        dataObj = stream.readInt64BE()
                        bytesRead = 8
                    }
                    CPKColumnType.TYPE_4BYTE2 -> {
                        dataObj = stream.readInt32BE()
                        bytesRead = 4
                    }
                    CPKColumnType.TYPE_4BYTE -> {
                        dataObj = stream.readInt32BE()
                        bytesRead = 4
                    }
                    CPKColumnType.TYPE_2BYTE2 -> {
                        dataObj = stream.readInt16BE()
                        bytesRead = 2
                    }
                    CPKColumnType.TYPE_2BYTE -> {
                        dataObj = stream.readInt16BE()
                        bytesRead = 2
                    }
                    CPKColumnType.TYPE_FLOAT -> {
                        dataObj = (0.0f to stream.readFloatBE())
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
                        println("Unknown normal type")
                        dataObj = 0
                        bytesRead = 0
                    }
                }

                if (!constant)
                    rowOffset += bytesRead

                if (schema[j].columnName == column)
                    return columnType to (if (columnType == CPKColumnType.TYPE_STRING) CPK.sanitiseStringTable(stringTable, (dataObj as Number).toInt()) else dataObj)
            }
        }

        return null
    }

    fun dump(data: () -> InputStream): Map<String, List<Triple<Int, CPKColumnType, Any>>> = schema.map { it.columnName to getRows(data, it.columnName) }.toMap()
}