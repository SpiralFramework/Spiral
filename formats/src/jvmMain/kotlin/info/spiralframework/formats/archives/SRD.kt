package info.spiralframework.formats.archives

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.formats.archives.srd.SRDEntry
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.io.jvm.CountingInputStream
import java.io.InputStream

class SRD private constructor(context: SpiralContext, val dataSource: () -> InputStream): IArchive {
    companion object {
        operator fun invoke(context: SpiralContext, dataSource: () -> InputStream): SRD? {
            withFormats(context) {
                try {
                    return SRD(this, dataSource)
                } catch (iae: IllegalArgumentException) {
                    debug("formats.srd.invalid", dataSource, iae)

                    return null
                }
            }
        }

        fun unsafe(context: SpiralContext, dataSource: () -> InputStream): SRD = withFormats(context) { SRD(this, dataSource) }
    }

    val entries: Array<SRDEntry>

    init {
        with(context) {
            val stream = CountingInputStream(dataSource())

            try {
                val entryList: MutableList<SRDEntry> = ArrayList()

                while (stream.available() > 0)
                    entryList.add(SRDEntry(this, stream, this@SRD))

                entries = entryList.toTypedArray()
            } finally {
                stream.close()
            }
        }
    }
}