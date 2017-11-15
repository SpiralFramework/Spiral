package org.abimon.spiral.core.objects.images

import org.abimon.spiral.core.*
import org.abimon.spiral.util.CountingInputStream
import org.abimon.visi.io.DataSource

class SRD(val srd: DataSource) {
    companion object {
        private fun readSRDItem(stream: CountingInputStream, source: DataSource): SRDItem? {
            val dataType = stream.readString(4)

            if (dataType.length < 4 || !dataType.startsWith("$"))
                return null

            val dataLen = stream.readUnsignedBigInt()
            val subdataLen = stream.readUnsignedBigInt()
            val padding = stream.readUnsignedBigInt()

            val dataPadding = (0x10 - dataLen % 0x10) % 0x10
            val subdataPadding = (0x10 - subdataLen % 0x10) % 0x10

            val dataOffset = stream.streamOffset //if(stream is OffsetInputStream) stream.offset + stream.count else stream.count
            stream.skip(dataPadding + dataLen)
            val subdataOffset = stream.streamOffset //if(stream is OffsetInputStream) stream.offset + stream.count else stream.count
            stream.skip(subdataPadding + subdataLen)

            return SRDItem(dataType, dataOffset, dataLen, subdataOffset, subdataLen, source)
        }
    }

    val items: MutableList<SRDItem> = ArrayList()

    init {
        srd.seekableUse { original ->
            val stream = CountingInputStream(original)
            loop@ while (true) {
                val item = readSRDItem(stream, srd) ?: break

                when(item.dataType) {
                    "\$CFH" -> items.add(item)
                    "\$CT0" -> items.add(item)
                    "\$RSF" -> items.add(item)
                    "\$TXR" -> {
                        item.data.use { data ->
                            item.subdata.use { subdata ->
                                val unk1 = data.readUnsignedLittleInt()
                                val swiz = data.readShort(unsigned = true, little = true)
                                val disp_width = data.readShort(unsigned = true, little = true)
                                val disp_height = data.readShort(unsigned = true, little = true)
                                val scanline = data.readShort(unsigned = true, little = true)
                                val fmt = data.read() and 0xFF
                                val unk2 = data.read() and 0xFF
                                val palette = data.read() and 0xFF
                                val palette_id = data.read() and 0xFF

                                val imgItem = readSRDItem(subdata, srd)!!
                                imgItem.data.use { img_data ->
                                    img_data.skip(2)
                                    val unk5 = img_data.read() and 0xFF
                                    val mipmap_count = img_data.read() and 0xFF
                                    img_data.skip(8)
                                    val name_offset = img_data.readUnsignedLittleInt()

                                    val mipmaps: MutableList<IntArray> = ArrayList()

                                    for (i in 0 until mipmap_count) {
                                        val mipmap_start = img_data.readUnsignedLittleInt() and 0x00FFFFFF
                                        val mipmap_len = img_data.readUnsignedLittleInt()
                                        val mipmap_unk1 = img_data.readUnsignedLittleInt()
                                        val mipmap_unk2 = img_data.readUnsignedLittleInt()

                                        mipmaps.add(intArrayOf(mipmap_start.toInt(), mipmap_len.toInt(), mipmap_unk1.toInt(), mipmap_unk2.toInt()))
                                    }

                                    img_data.reset()
                                    img_data.skip(name_offset)

                                    val name = img_data.readZeroString()

                                    items.add(TXRItem(
                                            unk1, swiz, disp_width, disp_height,
                                            scanline, fmt, unk2, palette,
                                            palette_id, unk5, mipmaps.toTypedArray(),
                                            name, item, imgItem
                                    ))
                                }
                            }
                        }
                    }
                    else -> {
                        items.add(item)
                    }
                }
            }
        }
    }
}