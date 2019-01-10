package info.spiralframework.formats.archives

import info.spiralframework.formats.archives.srd.SRDEntry
import info.spiralframework.base.CountingInputStream
import info.spiralframework.formats.utils.DataHandler
import java.io.InputStream

class SRD private constructor(val dataSource: () -> InputStream): IArchive {
    companion object {
        operator fun invoke(dataSource: () -> InputStream): SRD? {
            try {
                return SRD(dataSource)
            } catch (iae: IllegalArgumentException) {
                DataHandler.LOGGER.debug("formats.srd.invalid", dataSource, iae)

                return null
            }
        }

        fun unsafe(dataSource: () -> InputStream): SRD = SRD(dataSource)
    }

    val entries: Array<SRDEntry>

    init {
        val stream = CountingInputStream(dataSource())

        try {
            val entryList: MutableList<SRDEntry> = ArrayList()

            while (stream.available() > 0)
                entryList.add(SRDEntry(stream, this))

            entries = entryList.toTypedArray()
        } finally {
            stream.close()
        }
    }
}