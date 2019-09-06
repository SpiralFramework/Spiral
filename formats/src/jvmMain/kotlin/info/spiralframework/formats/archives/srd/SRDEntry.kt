package info.spiralframework.formats.archives.srd

import info.spiralframework.base.CountingInputStream
import info.spiralframework.base.WindowedInputStream
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.util.readInt32BE
import info.spiralframework.base.util.readString
import info.spiralframework.formats.archives.SRD
import info.spiralframework.formats.common.withFormats
import info.spiralframework.formats.utils.align
import java.io.InputStream

open class SRDEntry(context: SpiralContext, val dataType: String, val offset: Long, val dataLength: Int, val subdataLength: Int, val srd: SRD) {
    companion object {
        operator fun invoke(context: SpiralContext, stream: CountingInputStream, srd: SRD): SRDEntry {
            withFormats(context) {
                val dataType = stream.readString(4)

                val dataLength = stream.readInt32BE()
                val subdataLength = stream.readInt32BE()
                val padding = stream.readInt32BE()

                val offset = stream.streamOffset
                val skip = (dataLength + subdataLength + dataLength.align() + subdataLength.align()).toLong()
                require(skip >= 0) { localise("formats.srd.invalid_skip", skip, dataLength, subdataLength) }
                stream.skip(skip)

                when (dataType) {
                    "\$TXI" -> return TXIEntry(this, dataType, offset, dataLength, subdataLength, srd)
                    "\$TXR" -> return TXREntry(this, dataType, offset, dataLength, subdataLength, srd)
                    "\$RSI" -> return RSIEntry(this, dataType, offset, dataLength, subdataLength, srd)
                    "\$VTX" -> return VTXEntry(this, dataType, offset, dataLength, subdataLength, srd)
                    "\$MAT" -> return MATEntry(this, dataType, offset, dataLength, subdataLength, srd)
                    "\$MSH" -> return MSHEntry(this, dataType, offset, dataLength, subdataLength, srd)
                    else -> return SRDEntry(this, dataType, offset, dataLength, subdataLength, srd)
                }
            }
        }
    }

    val dataStream: InputStream
        get() = WindowedInputStream(srd.dataSource(), offset.toLong(), dataLength.toLong())

    val subdataStream: InputStream
        get() = WindowedInputStream(srd.dataSource(), (offset + dataLength + dataLength.align()), subdataLength.toLong())

    val size: Int = dataLength + subdataLength + dataLength.align() + subdataLength.align()

    open val rsiEntry: RSIEntry? by lazy {
        if (subdataLength > 16)
            return@lazy (subdataStream as WindowedInputStream).use { substream -> SRDEntry(context, substream, srd) } as? RSIEntry
        return@lazy null
    }
}