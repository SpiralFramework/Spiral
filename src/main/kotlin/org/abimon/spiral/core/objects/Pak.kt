package org.abimon.spiral.core.objects

import org.abimon.spiral.core.isDebug
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
    val files: LinkedList<PakFileEntry> = LinkedList()

    init {
        val pak = CountingInputStream(dataSource.inputStream)
        try {
            val numFiles = pak.readNumber(4, true).toInt().coerceAtMost(1024) //Fair sample size
            if (numFiles < 1)
                throw IllegalArgumentException("${dataSource.location} is either not a valid PAK file, or is corrupt ($numFiles < 1)")
            offsets = LongArray(numFiles + 1)

            for (i in 0 until numFiles) {
                offsets[i] = pak.readNumber(4, true)
                if (offsets[i] < 0)
                    throw IllegalArgumentException("${dataSource.location} is either not a valid PAK file, or is corrupt (${offsets[i]} < 0)")
                else if (offsets[i] >= dataSource.size)
                    throw IllegalArgumentException("${dataSource.location} is either not a valid PAK file, or is corrupt (${offsets[i]} >= ${dataSource.size})")
            }

            offsets[numFiles] = dataSource.size

            for (i in 0 until numFiles) {
                if (offsets[i] >= offsets[i + 1])
                    throw IllegalArgumentException("${dataSource.location} is either not a valid PAK file, or is corrupt ($i >= ${i + 1}; ${offsets[i]} >= ${offsets[i + 1]})")
                else if(offsets[i] < pak.count)
                    throw IllegalArgumentException("${dataSource.location} is either not a valid PAK file, or is corrupt ($i < header; ${offsets[i]} < ${pak.count})")
            }

            for (i in 0 until numFiles)
                files.add(PakFileEntry("$i", offsets[i + 1] - offsets[i], offsets[i], this))
            pak.close()
        } catch(illegal: IllegalArgumentException) {
            pak.close()
            if(isDebug) illegal.printStackTrace()
            throw illegal
        }
    }
}