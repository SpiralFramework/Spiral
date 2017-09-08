package org.abimon.spiral.core.objects

import org.abimon.spiral.core.data.UTFColumnInfo
import org.abimon.spiral.core.data.UTFTableInfo
import org.abimon.spiral.core.debug
import org.abimon.spiral.core.readNumber
import org.abimon.spiral.core.readString
import org.abimon.spiral.util.CountingInputStream
import org.abimon.spiral.util.SeekableInputStream
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.skipBytes

/** Vita */

class CPK(val dataSource: DataSource) {
    val fileTable: MutableList<CPKFileEntry> = ArrayList()
    val headerInfo: UTFTableInfo
    val tocHeader: UTFTableInfo

    companion object {
        val COLUMN_STORAGE_MASK = 0xF0
        val COLUMN_STORAGE_PERROW = 0x50
        val COLUMN_STORAGE_CONSTANT = 0x30
        val COLUMN_STORAGE_ZERO = 0x10

        val COLUMN_TYPE_MASK = 0x0F
        val COLUMN_TYPE_DATA = 0x0B
        val COLUMN_TYPE_STRING = 0x0A
        val COLUMN_TYPE_FLOAT = 0x08
        val COLUMN_TYPE_8BYTE = 0x06
        val COLUMN_TYPE_4BYTE2 = 0x05
        val COLUMN_TYPE_4BYTE = 0x04
        val COLUMN_TYPE_2BYTE2 = 0x03
        val COLUMN_TYPE_2BYTE = 0x02
        val COLUMN_TYPE_1BYTE2 = 0x01
        val COLUMN_TYPE_1BYTE = 0x00

        val NULL_TERMINATOR = 0x00.toChar()

        fun readTable(dataSource: DataSource, offset: Long): UTFTableInfo {
            return CountingInputStream(dataSource.inputStream).use { cpk ->
                cpk.skipBytes(offset)

                val utfSig = cpk.readString(4)
                if (utfSig != "@UTF")
                    throw IllegalArgumentException("${dataSource.location} is either not a CPK file, or a corrupted/invalid one (UTF Signature '$utfSig' ≠ '@UTF')!")

                val info = UTFTableInfo()

                info.tableOffset = offset
                info.tableSize = cpk.readNumber(4, unsigned = true, little = false)
                info.schemaOffset = 0x20
                info.rowsOffset = cpk.readNumber(4, unsigned = true, little = false)
                info.stringTableOffset = cpk.readNumber(4, unsigned = true, little = false)
                info.dataOffset = cpk.readNumber(4, unsigned = true, little = false)
                val tableNameString = cpk.readNumber(4, unsigned = true, little = false)
                info.columns = cpk.readNumber(2, unsigned = true, little = false).toInt()
                info.rowWidth = cpk.readNumber(2, unsigned = true, little = false).toInt()
                info.rows = cpk.readNumber(4, unsigned = true, little = false)

                info.stringTable = dataSource.inputStream.use { it.skipBytes(info.stringTableOffset + 8 + offset); it.readString((info.dataOffset - info.stringTableOffset).toInt() + 1) }

                val schema: MutableList<UTFColumnInfo> = ArrayList<UTFColumnInfo>(info.columns)

                for (i in 0 until info.columns) {
                    val column = UTFColumnInfo()
                    column.type = cpk.read() and 0xFF
                    column.columnName = info.stringTable.substring(cpk.readNumber(4, unsigned = true, little = false).toInt()).substringBefore(NULL_TERMINATOR)

                    if (column.type and COLUMN_STORAGE_MASK == COLUMN_STORAGE_MASK) {
                        column.constantOffset = cpk.count

                        when (column.type and COLUMN_TYPE_MASK) {
                            COLUMN_TYPE_STRING -> cpk.skipBytes(4)
                            COLUMN_TYPE_8BYTE -> cpk.skipBytes(8)
                            COLUMN_TYPE_DATA -> cpk.skipBytes(8)
                            COLUMN_TYPE_FLOAT -> cpk.skipBytes(4)
                            COLUMN_TYPE_4BYTE -> cpk.skipBytes(4)
                            COLUMN_TYPE_4BYTE2 -> cpk.skipBytes(4)
                            COLUMN_TYPE_2BYTE -> cpk.skipBytes(2)
                            COLUMN_TYPE_2BYTE2 -> cpk.skipBytes(2)
                            COLUMN_TYPE_1BYTE -> cpk.read()
                            COLUMN_TYPE_1BYTE2 -> cpk.read()
                            else -> debug("[CPK] Unknown type for constant: ${column.type}")
                        }
                    }

                    schema.add(column)
                }

                info.schema = schema.toTypedArray()
                info.tableName = info.stringTable.substring(tableNameString.toInt()).substringBefore(NULL_TERMINATOR)

                return@use info
            }
        }
    }

    init {
        val cpk = SeekableInputStream(dataSource.seekableInputStream)
        try {
            val cpkMagic = cpk.readString(4)

            if (cpkMagic != "CPK ")
                throw IllegalArgumentException("${dataSource.location} is either not a CPK file, or a corrupted/invalid one (Magic '$cpkMagic' ≠ 'CPK ')!")

            headerInfo = readTable(dataSource, 0x10)
            if (headerInfo.rows != 1L)
                throw IllegalArgumentException("${dataSource.location} is either not a CPK file, or a corrupted/invalid one (Number of header rows '${headerInfo.rows}' ≠ 1)!")

            val tocOffset = (headerInfo.getRows(dataSource, "TocOffset")[0].third as? Number)?.toLong() ?: throw IllegalArgumentException("${dataSource.location} is either not a CPK file, or a corrupted/invalid one (No column or no value for 'TocOffset')!")
            val contentOffset = (headerInfo.getRows(dataSource, "ContentOffset")[0].third as? Number)?.toInt() ?: throw IllegalArgumentException("${dataSource.location} is either not a CPK file, or a corrupted/invalid one (No column or no value for 'ContentOffset')!")
            val fileCount = (headerInfo.getRows(dataSource, "Files")[0].third as? Number)?.toInt() ?: throw IllegalArgumentException("${dataSource.location} is either not a CPK file, or a corrupted/invalid one (No column or no value for 'Files')!")

            cpk.seek(tocOffset)

            val tocSignature = cpk.readString(4)
            if (tocSignature != "TOC ")
                throw IllegalArgumentException("${dataSource.location} is either not a CPK file, or a corrupted/invalid one (TOC Signature at $tocOffset '$tocSignature' ≠ 'TOC ')!")

            tocHeader = readTable(dataSource, tocOffset + 0x10)

            if (tocHeader.rows != fileCount.toLong())
                throw IllegalArgumentException("${dataSource.location} is either not a CPK file, or a corrupted/invalid one (TocHeader#rows '${tocHeader.rows}' ≠ CpkHeader#Files '$fileCount')!")

            val filenames = tocHeader.getRows(dataSource, "FileName").map { (_, _, index) -> tocHeader.stringTable.substring((index as? Number ?: 0).toInt()).substringBefore(NULL_TERMINATOR) }
            val dirnames = tocHeader.getRows(dataSource, "DirName").map { (_, _, index) -> tocHeader.stringTable.substring((index as? Number ?: 0).toInt()).substringBefore(NULL_TERMINATOR) }
            val fileSizes = tocHeader.getRows(dataSource, "FileSize").map { (_, _, size) -> (size as? Number ?: -1).toLong() }
            val extractSizes = tocHeader.getRows(dataSource, "ExtractSize").map { (_, _, size) -> (size as? Number ?: -1).toLong() }
            val fileOffsets = tocHeader.getRows(dataSource, "FileOffset").map { (_, _, size) -> (size as? Number ?: -1).toLong() }

            for (i in 0 until tocHeader.rows.toInt()) {
                val filename = filenames[i]
                val dirname = dirnames[i]

                val filesize = fileSizes[i]
                val extractSize = extractSizes[i]

                val isCompressed = extractSize > filesize

                val offset = fileOffsets[i] + Math.min(contentOffset.toLong(), tocOffset)

                fileTable.add(CPKFileEntry(filename, dirname, filesize, extractSize, offset, isCompressed, this))
            }
        } catch (th: Throwable) {
            cpk.close()
            throw th
        }

        cpk.close()
    }
}