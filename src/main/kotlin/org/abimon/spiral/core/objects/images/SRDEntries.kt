package org.abimon.spiral.core.objects.images

import org.abimon.spiral.util.OffsetInputStream
import org.abimon.visi.io.DataSource

open class SRDItem(val dataType: String, val dataOffset: Long, val dataLen: Long, val subdataOffset: Long, val subdataLen: Long, val parent: DataSource) {
    val data: OffsetInputStream
        get() = OffsetInputStream(parent.seekableInputStream, dataOffset, dataLen)
    val subdata: OffsetInputStream
        get() = OffsetInputStream(parent.seekableInputStream, subdataOffset, subdataLen)

    operator fun component1(): String = dataType
    operator fun component2(): OffsetInputStream = data
    operator fun component3(): OffsetInputStream = subdata
}

class TXRItem(
        val unk1: Long, val swiz: Int, val dispWidth: Int, val dispHeight: Int,
        val scanline: Int, val format: Int, val unk2: Int, val palette: Int,
        val paletteId: Int, val unk5: Int, val mipmaps: Array<IntArray>,
        val name: String, val parentItem: SRDItem
): SRDItem("\$TXR", parentItem.dataOffset, parentItem.dataLen, parentItem.subdataOffset, parentItem.subdataLen, parentItem.parent)