package org.abimon.spiral.core.objects.archives

import org.abimon.spiral.core.objects.archives.srd.SRDEntry
import org.abimon.spiral.core.utils.CountingInputStream
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