package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.readNumber
import org.abimon.spiral.util.CountingInputStream
import org.abimon.visi.io.DataSource
import java.util.*

/**
 * Very basic and boring, yet crucial archive format.
 * Very basic structure - an unsigned integer dictating how many files there are, followed by the offsets for each file, and then followed by the data, at the offset indicated.
 *
 * There's two important things to note here - the first is the absence of any sort of filenames or format indicators.
 *
 * Filenames are therefore generated as the index of the file, and a format may be guessed based on some information.
 * SPIRAL also has a method to get the name if it has been recorded previously
 *
 * The second thing to note is that the offset, unlike [WAD] offsets, are ***not*** zero indexed. 0 would, in this case, be right at the start of the file
 */
class Pak(val dataSource: DataSource) {
    val offsets: LongArray
    val files: MutableList<PakFileEntry> = LinkedList()

    init {
        val pak = CountingInputStream(dataSource.inputStream)
        try {
            val numFiles = pak.readNumber(4, true).toInt().coerceAtMost(1024) //Fair sample size
            if (numFiles < 1)
                throw IllegalArgumentException("${dataSource.location} is either not a valid PAK file, or is corrupt ($numFiles < 1)")
            val tmpOffsets = LongArray(numFiles + 1)

            for (i in 0 until numFiles) {
                tmpOffsets[i] = pak.readNumber(4, true)
                if (tmpOffsets[i] < 0)
                    throw IllegalArgumentException("${dataSource.location} is either not a valid PAK file, or is corrupt (${tmpOffsets[i]} < 0)")
                else if (tmpOffsets[i] >= dataSource.size)
                    throw IllegalArgumentException("${dataSource.location} is either not a valid PAK file, or is corrupt (${tmpOffsets[i]} >= ${dataSource.size})")
            }

            tmpOffsets[numFiles] = dataSource.size

            offsets = tmpOffsets.filter { it != 0L }.toLongArray()

            for (i in 0 until offsets.size - 1) {
                if (offsets[i] >= offsets[i + 1])
                    throw IllegalArgumentException("${dataSource.location} is either not a valid PAK file, or is corrupt ($i >= ${i + 1}; ${offsets[i]} >= ${offsets[i + 1]})")
                else if(offsets[i] < pak.count)
                    throw IllegalArgumentException("${dataSource.location} is either not a valid PAK file, or is corrupt ($i < header; ${offsets[i]} < ${pak.count})")
            }

            for (i in 0 until offsets.size - 1)
                files.add(PakFileEntry("$i", offsets[i + 1] - offsets[i], offsets[i], this))
            pak.close()
        } catch(illegal: IllegalArgumentException) {
            pak.close()
            throw illegal
        }
    }
}