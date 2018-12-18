package info.spiralframework.formats.archives

import info.spiralframework.formats.archives.srd.SRDEntry
import info.spiralframework.formats.utils.CountingInputStream
import java.io.InputStream

class SRD(val dataSource: () -> InputStream) {
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