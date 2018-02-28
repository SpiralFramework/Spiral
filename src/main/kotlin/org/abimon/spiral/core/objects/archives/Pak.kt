package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.utils.assertAsArgument
import org.abimon.spiral.core.utils.readInt32LE
import java.io.InputStream

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
class Pak(val dataSource: () -> InputStream) {
    companion object {
        var SANITY_MAX_FILE_COUNT = 1024
        var SANITY_MIN_FILE_SIZE = 0
        var SANITY_MAX_FILE_SIZE = 64 * 1024 * 1024
    }

    val files: Array<PakEntry>

    init {
        val stream = dataSource()

        try {
            val fileCount = stream.readInt32LE()
            assertAsArgument(fileCount > 1, "Illegal number of files in Pak File (Was $fileCount, expected > 1)")
            assertAsArgument(fileCount < SANITY_MAX_FILE_COUNT, "Illegal number of files in Pak File (was $fileCount, expected < $SANITY_MAX_FILE_COUNT); If you are converting a valid file then you'll need to bump up the maximum file count!")

            val offsets = IntArray(fileCount) { index ->
                val offset = stream.readInt32LE()
                assertAsArgument(offset >= 0, "Illegal offset for file $index in Pak File (Was $offset, expected >= 0)") //That *one* file in DR2...

                return@IntArray offset
            }

            files = Array(fileCount) { index ->
                val offset = offsets[index]
                val size: Int
                if(index == fileCount - 1) {
                    size = -1
                } else {
                    size = offsets[index + 1] - offset
                    assertAsArgument(size > SANITY_MIN_FILE_SIZE, "Illegal size for file $index in Pak File (Was $size, expected > $SANITY_MIN_FILE_SIZE")
                    assertAsArgument(size < SANITY_MAX_FILE_SIZE, "Illegal size for file $index in Pak File (Was $size, expected < $SANITY_MAX_FILE_SIZE")
                }
                return@Array PakEntry(index, size, offset)
            }
        } finally {
            stream.close()
        }
    }
}