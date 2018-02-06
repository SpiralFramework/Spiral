package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.utils.readInt32LE
import org.abimon.spiral.core.utils.readInt64LE
import org.abimon.spiral.core.utils.writeInt32LE
import org.abimon.spiral.core.utils.writeInt64LE
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class CustomCPK(val baseCPK: () -> InputStream) {
    val files: MutableMap<String, Pair<Long, () -> InputStream>> = HashMap()

    fun add(name: String, data: File) = add(name, data.length()) { FileInputStream(data) }
    fun add(name: String, size: Long, supplier: () -> InputStream) = files.put(name.replace(File.separator, "/"), size to supplier)

    fun compile(output: OutputStream) {
        baseCPK().use { stream ->
            stream.skip(4)

            output.writeInt32LE(CPK.MAGIC_NUMBER)
            output.writeInt64LE(stream.readInt64LE())
            output.writeInt32LE(stream.readInt32LE())

            //write table


        }
    }

//    fun writeTable(output: OutputStream, table: UTFTableInfo) {
//        output.writeInt32LE(CPK.UTF_MAGIC_NUMBER)
//
//        output.writeInt32BE(table.tableSize)
//        output.writeUInt32BE(table.rowsOffset)
//        output.writeUInt32BE(table.stringTableOffset)
//        output.writeUInt32BE(table.dataOffset)
//
//        val stringTable = table.stringTable
//
//        output.writeInt32BE(stringTable.indexOf(table.tableName))
//
//        output.writeInt16BE(table.columns)
//        output.writeInt16BE(table.rowWidth)
//        output.writeInt32BE(table.rows)
//    }


}