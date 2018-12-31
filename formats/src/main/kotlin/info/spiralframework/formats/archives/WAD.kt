package info.spiralframework.formats.archives

import info.spiralframework.base.assertAsLocaleArgument
import info.spiralframework.formats.utils.*

/**
 * A central object to handle the WAD format used by the Steam releases of DR 1 and 2, our primary targets for modding
 *
 * When destructing, component 1 is the major version, 2 is the minor version, 3 is a list of files, 4 is a list of directories, and 5 is the data offset
 * Why would you want that? Who knows
 */
class WAD private constructor(val dataSource: DataSource) {
    companion object {
        val MAGIC_NUMBER = 0x52414741

        var MIN_FILE_COUNT = 1
            set(value) {
                field = value
                FILE_COUNT_RANGE = value ..MAX_FILE_COUNT
            }
        var MAX_FILE_COUNT = 1024 * 1024
            set(value) {
                field = value
                FILE_COUNT_RANGE = MIN_FILE_COUNT.. value
            }
        var FILE_COUNT_RANGE: IntRange = MIN_FILE_COUNT..MAX_FILE_COUNT

        var MIN_FILENAME_LENGTH = 1
            set(value) {
                field = value
                FILENAME_LENGTH_RANGE = value ..MAX_FILENAME_LENGTH
            }
        var MAX_FILENAME_LENGTH = 1024 * 1024
            set(value) {
                field = value
                FILENAME_LENGTH_RANGE = MIN_FILENAME_LENGTH.. value
            }
        var FILENAME_LENGTH_RANGE: IntRange = MIN_FILENAME_LENGTH..MAX_FILENAME_LENGTH

        operator fun invoke(dataSource: DataSource): WAD? {
            try {
                return WAD(dataSource)
            } catch (iae: IllegalArgumentException) {
                DataHandler.LOGGER.debug("formats.wad.invalid", dataSource, iae)

                return null
            }
        }

        fun unsafe(dataSource: DataSource): WAD = WAD(dataSource)
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
            assertAsLocaleArgument(localMagic == MAGIC_NUMBER, "formats.wad.invalid_magic", localMagic, MAGIC_NUMBER)

            major = stream.readInt32LE()
            minor = stream.readInt32LE()

            val headerSize = stream.readInt32LE()
            header = ByteArray(headerSize)

            stream.read(header)

            val fileCount = stream.readInt32LE()
            assertAsLocaleArgument(fileCount in FILE_COUNT_RANGE, "formats.wad.invalid_file_count", fileCount, MIN_FILE_COUNT, MAX_FILE_COUNT)

            files = Array(fileCount) { index ->
                val nameLen = stream.readInt32LE()
                assertAsLocaleArgument(nameLen in FILENAME_LENGTH_RANGE, "formats.wad.invalid_file_name_length", nameLen, MIN_FILENAME_LENGTH, MAX_FILENAME_LENGTH)
                val name = stream.readString(nameLen)
                val size = stream.readInt64LE()
                val offset = stream.readInt64LE()

                return@Array WADFileEntry(name, size, offset, this)
            }

            val directoryCount = stream.readInt32LE()
            directories = Array(directoryCount) { index ->
                val nameLen = stream.readInt32LE()
                val name = stream.readString(nameLen)
                val subEntryCount = stream.readInt32LE()

                val subEntries = Array(subEntryCount) sub@{ subIndex ->
                    val subNameLen = stream.readInt32LE()
                    assertAsLocaleArgument(subNameLen in FILENAME_LENGTH_RANGE, "formats.wad.invalid_subfile_name_length", subNameLen, MIN_FILENAME_LENGTH, MAX_FILENAME_LENGTH)
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