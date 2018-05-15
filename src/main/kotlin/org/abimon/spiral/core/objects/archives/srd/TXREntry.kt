package org.abimon.spiral.core.objects.archives.srd

import org.abimon.spiral.core.objects.archives.SRD
import org.abimon.spiral.core.utils.readInt16LE
import org.abimon.spiral.core.utils.readInt32LE

open class TXREntry(dataType: String, offset: Long, dataLength: Int, subdataLength: Int, srd: SRD): SRDEntry(dataType, offset, dataLength, subdataLength, srd) {
    val unk1: Int
    val swizzle: Int
    val displayWidth: Int
    val displayHeight: Int
    val scanline: Int
    val format: Int
    val unk2: Int
    val palette: Int
    val paletteID: Int

    override val rsiEntry: RSIEntry = super.rsiEntry!!

    init {
        val stream = dataStream

        try {
            unk1 = stream.readInt32LE()
            swizzle = stream.readInt16LE()
            displayWidth = stream.readInt16LE()
            displayHeight = stream.readInt16LE()
            scanline = stream.readInt16LE()
            format = stream.read() and 0xFF
            unk2 = stream.read() and 0xFF
            palette = stream.read() and 0xFF
            paletteID = stream.read() and 0xFF
        } finally {
            stream.close()
        }
    }
}