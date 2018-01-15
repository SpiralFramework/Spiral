package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.utils.CountingInputStream
import org.abimon.spiral.core.utils.readInt32LE
import org.abimon.spiral.core.utils.readInt64LE
import org.abimon.spiral.core.utils.readString
import java.io.InputStream

/**
 * A central object to handle the WAD format used by the Steam releases of DR 1 and 2, our primary targets for modding
 *
 * When destructing, component 1 is the major version, 2 is the minor version, 3 is a list of files, 4 is a list of directories, and 5 is the data offset
 * Why would you want that? Who knows
 */
class WAD(val dataSource: () -> InputStream) {
    companion object {
        val MAGIC_NUMBER = 0x52414741
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
            assert(localMagic == MAGIC_NUMBER)

            major = stream.readInt32LE()
            minor = stream.readInt32LE()

            val headerSize = stream.readInt32LE()
            header = ByteArray(headerSize)

            stream.read(header)

            val filecount = stream.readInt32LE()
            files = Array(filecount) { index ->
                val nameLen = stream.readInt32LE()
                val name = stream.readString(nameLen)
                val size = stream.readInt64LE()
                val offset = stream.readInt64LE()

                return@Array WADFileEntry(name, size, offset)
            }

            val directoryCount = stream.readInt32LE()
            directories = Array(directoryCount) { index ->
                val nameLen = stream.readInt32LE()
                val name = stream.readString(nameLen)
                val subEntryCount = stream.readInt32LE()

                val subEntries = Array(subEntryCount) sub@{ subIndex ->
                    val subNameLen = stream.readInt32LE()
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