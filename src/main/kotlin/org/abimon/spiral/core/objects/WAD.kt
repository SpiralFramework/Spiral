package org.abimon.spiral.core.objects

import org.abimon.spiral.core.readNumber
import org.abimon.spiral.core.readString
import org.abimon.spiral.core.spiralHeaderName
import org.abimon.spiral.util.CountingInputStream
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.readPartialBytes
import java.util.*

/**
 * A central object to handle the WAD format used by the Steam releases of DR 1 and 2, our primary targets for modding
 *
 * When destructing, component 1 is the major version, 2 is the minor version, 3 is a list of files, 4 is a list of directories, and 5 is the data offset
 * Why would you want that? Who knows
 */
class WAD(val dataSource: DataSource) {
    val major: Int
    val minor: Int
    val header: ByteArray

    val files: MutableList<WADFileEntry> = ArrayList<WADFileEntry>()
    val directories: MutableList<WADSubdirectoryEntry> = ArrayList<WADSubdirectoryEntry>()

    val dataOffset: Long

    val spiralHeader: ByteArray?

    operator fun component1(): Int = major
    operator fun component2(): Int = minor
    operator fun component3(): List<WADFileEntry> = files
    operator fun component4(): List<WADSubdirectoryEntry> = directories
    operator fun component5(): Long = dataOffset

    init {
        val wad = CountingInputStream(dataSource.inputStream)

        try {
            val agar = wad.readString(4)

            if (agar != "AGAR")
                throw IllegalArgumentException("${dataSource.location} is either not a WAD file, or a corrupted/invalid one (Magic number ≠ 'AGAR'; is $agar)!")

            major = wad.readNumber(4, true).toInt()
            minor = wad.readNumber(4, true).toInt()
            header = wad.readPartialBytes(wad.readNumber(4, true).toInt())

            val numberOfFiles = wad.readNumber(4, true)

            for (i in 0 until numberOfFiles) {
                val len = wad.readNumber(4, true).toInt()
                val name = wad.readString(len)
                val size = wad.readNumber(8, true)
                val offset = wad.readNumber(8, true)

                files.add(WADFileEntry(name, size, offset, this))
            }

            val numberOfDirectories = wad.readNumber(4, true)

            for (i in 0 until numberOfDirectories) {
                val len = wad.readNumber(4, true).toInt()
                val name = wad.readString(len)
                val subfiles = LinkedList<WADSubfileEntry>()
                val numberOfSubFiles = wad.readNumber(4, true)

                for (j in 0 until numberOfSubFiles) {
                    val subLen = wad.readNumber(4, true).toInt()
                    val subName = wad.readString(subLen)
                    val isFile = wad.read() == 0
                    subfiles.add(WADSubfileEntry(subName, isFile))
                }

                directories.add(WADSubdirectoryEntry(name, subfiles))
            }

            dataOffset = wad.count
            wad.close()

            if (files.any { (name) -> name == spiralHeaderName })
                spiralHeader = files.first { (name) -> name == spiralHeaderName }.data
            else
                spiralHeader = null
        } catch(illegal: IllegalArgumentException) {
            wad.close()
            throw illegal
        }
    }

    fun hasHeader(): Boolean = header.isNotEmpty()
}