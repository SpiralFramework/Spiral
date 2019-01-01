package info.spiralframework.formats.archives

import info.spiralframework.base.assertAsLocaleArgument
import info.spiralframework.formats.utils.DataHandler
import info.spiralframework.formats.utils.readInt32LE
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
class Pak private constructor(val dataSource: () -> InputStream, overrideSanityChecks: Boolean = false): IArchive {
    companion object {
        var SANITY_MAX_FILE_COUNT = 1024
        var SANITY_MIN_FILE_SIZE = 0
        var SANITY_MAX_FILE_SIZE = 64 * 1024 * 1024

        operator fun invoke(overrideSanityChecks: Boolean = false, dataSource: () -> InputStream): Pak? {
            try {
                return Pak(dataSource, overrideSanityChecks)
            } catch (iae: IllegalArgumentException) {
                DataHandler.LOGGER.debug("formats.pak.invalid", dataSource, iae)

                return null
            }
        }

        fun unsafe(overrideSanityChecks: Boolean = false, dataSource: () -> InputStream): Pak = Pak(dataSource, overrideSanityChecks)
    }

    val files: Array<PakEntry>

    init {
        val stream = dataSource()

        try {
            val fileCount = stream.readInt32LE()
            assertAsLocaleArgument(fileCount > 1, "formats.pak.not_enough_files", fileCount)
            assertAsLocaleArgument(overrideSanityChecks || fileCount < SANITY_MAX_FILE_COUNT, "formats.pak.too_many_files", fileCount, SANITY_MAX_FILE_COUNT)

            val offsets = IntArray(fileCount) { index ->
                val offset = stream.readInt32LE()
                assertAsLocaleArgument(offset >= 0, "formats.pak.offset_too_low", index, offset) //That *one* file in DR2...

                return@IntArray offset
            }

            files = Array(fileCount) { index ->
                val offset = offsets[index]
                val size: Int
                if(index == fileCount - 1) {
                    size = -1
                } else {
                    size = offsets[index + 1] - offset
                    assertAsLocaleArgument(overrideSanityChecks || size > SANITY_MIN_FILE_SIZE, "formats.pak.entry_too_small", index, size, SANITY_MIN_FILE_SIZE)
                    assertAsLocaleArgument(overrideSanityChecks || size < SANITY_MAX_FILE_SIZE, "formats.pak.entry_too_large", index, size, SANITY_MAX_FILE_SIZE)
                }
                return@Array PakEntry(index, size, offset, this)
            }
        } finally {
            stream.close()
        }
    }
}