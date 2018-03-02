package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.utils.*
import java.io.InputStream

/**
 * A central object to handle the WAD format used by the Steam releases of DR 1 and 2, our primary targets for modding
 *
 * When destructing, component 1 is the major version, 2 is the minor version, 3 is a list of files, 4 is a list of directories, and 5 is the data offset
 * Why would you want that? Who knows
 */
class WAD private constructor(val dataSource: () -> InputStream) {
    companion object {
        val MAGIC_NUMBER = 0x52414741

        var MIN_FILE_COUNT = 1
            set(value) {
                field = value
                FILE_COUNT_RANGE = value .. MAX_FILE_COUNT
            }
        var MAX_FILE_COUNT = 1024 * 1024
            set(value) {
                field = value
                FILE_COUNT_RANGE = MIN_FILE_COUNT .. value
            }
        var FILE_COUNT_RANGE: IntRange = MIN_FILE_COUNT .. MAX_FILE_COUNT

        var MIN_FILENAME_LENGTH = 1
            set(value) {
                field = value
                FILENAME_LENGTH_RANGE = value .. MAX_FILENAME_LENGTH
            }
        var MAX_FILENAME_LENGTH = 1024 * 1024
            set(value) {
                field = value
                FILENAME_LENGTH_RANGE = MIN_FILENAME_LENGTH .. value
            }
        var FILENAME_LENGTH_RANGE: IntRange = MIN_FILENAME_LENGTH .. MAX_FILENAME_LENGTH

        operator fun invoke(dataSource: () -> InputStream): WAD? {
            try {
                return WAD(dataSource)
            } catch (iae: IllegalArgumentException) {
                return null
            }
        }
    }

    val major: Int
    val minor: Int
    val header: ByteArray

    val files: Array<WADFileEntry>
    val directories: Array<WADSubdirectoryEntry>

    val dataOffset: Long

    init {
        val stream = CountingInputStream(dataSource())

        try {
            val localMagic = stream.readInt32LE()
            assertAsArgument(localMagic == MAGIC_NUMBER, "Illegal magic number for WAD File (Was $localMagic, expected $MAGIC_NUMBER)")

            major = stream.readInt32LE()
            minor = stream.readInt32LE()

            val headerSize = stream.readInt32LE()
            header = ByteArray(headerSize)

            stream.read(header)

            val filecount = stream.readInt32LE()
            assertAsArgument(filecount in FILE_COUNT_RANGE, "Illegal file count for WAD File (Was $filecount, expected $MIN_FILE_COUNT ≤ $filecount ≤ $MAX_FILE_COUNT)")

            files = Array(filecount) { index ->
                val nameLen = stream.readInt32LE()
                assertAsArgument(nameLen in FILENAME_LENGTH_RANGE, "Illegal filename length for WAD File (Was $nameLen, expected $MIN_FILENAME_LENGTH ≤ $nameLen ≤ $MAX_FILENAME_LENGTH)")
                val name = stream.readString(nameLen)
                val size = stream.readInt64LE()
                val offset = stream.readInt64LE()

                return@Array WADFileEntry(name, size, offset, this)
            }

            val directoryCount = stream.readInt32LE()
            directories = Array(directoryCount) { index ->
                val nameLen = stream.readInt32LE()
                assertAsArgument(nameLen in FILENAME_LENGTH_RANGE, "Illegal directory name length for WAD File (Was $nameLen, expected $MIN_FILENAME_LENGTH ≤ $nameLen ≤ $MAX_FILENAME_LENGTH)")
                val name = stream.readString(nameLen)
                val subEntryCount = stream.readInt32LE()

                val subEntries = Array(subEntryCount) sub@{ subIndex ->
                    val subNameLen = stream.readInt32LE()
                    assertAsArgument(nameLen in FILENAME_LENGTH_RANGE, "Illegal subfile name length for WAD File (Was $subNameLen, expected $MIN_FILENAME_LENGTH ≤ $subNameLen ≤ $MAX_FILENAME_LENGTH)")
                    val subName = stream.readString(subNameLen)

                    val isDirectory = stream.read() == 1

                    return@sub subName to isDirectory
                }

                return@Array WADSubdirectoryEntry(name, subEntries)
            }

            dataOffset = stream.streamOffset
        } finally {
            stream.close()
        }
    }
}