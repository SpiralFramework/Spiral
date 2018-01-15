package org.abimon.spiral.core.objects.archives

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
    val files: Array<PakEntry>

    init {
        val stream = dataSource()

        try {
            val fileCount = stream.readInt32LE()
            assert(fileCount > 1)

            val offsets = IntArray(fileCount) { index ->
                val offset = stream.readInt32LE()
                assert(offset > 0)

                return@IntArray offset
            }

            files = Array(fileCount) { index ->
                val name = "$index"
                val offset = offsets[index]
                val size = if(index == fileCount - 1) -1 else offsets[index + 1]

                return@Array PakEntry(name, size, offset)
            }
        } finally {
            stream.close()
        }
    }
}