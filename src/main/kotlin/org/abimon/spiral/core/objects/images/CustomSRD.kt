package org.abimon.spiral.core.objects.images

import org.abimon.spiral.core.*
import org.abimon.spiral.core.utils.*
import org.abimon.visi.io.read
import org.abimon.visi.lang.and
import java.awt.Color
import java.awt.geom.Dimension2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

class CustomSRD(val srd: () -> InputStream, val srdv: () -> InputStream) {
    companion object {
        val STATIC_SRD_END = byteArrayOfInts(0x24, 0x43, 0x54, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        val STATIC_RSI = byteArrayOfInts(0x24, 0x52, 0x53, 0x49)
    }

    val images: MutableMap<String, BufferedImage> = HashMap()
    val customMipmaps: MutableMap<String, Triple<Int, Dimension2D, Array<Pair<Long, () -> InputStream>>>> = HashMap()

    fun image(name: String, img: BufferedImage): CustomSRD {
        images[name] = img
        return this
    }

    fun mipmap(name: String, format: Int, dimensions: Dimension2D, images: Array<Pair<Long, () -> InputStream>>): CustomSRD {
        customMipmaps[name] = format to dimensions and images
        return this
    }

    fun patch(srdOut: OutputStream, srdvOut: OutputStream) {
        var imgOffset: Long = 0
        srdv.use { srdvStream ->
            srd.use { original ->
                val stream = CountingInputStream(original)
                loop@ while (true) {
                    val (data_type, data_S, subdata_S) = readSRDItem(stream, srd) ?: break

                    subdata_S.use big@ { subdata ->
                        data_S.use { data ->
                            when (data_type) {
                                "\$TXR" -> {
                                    val unk1 = data.readUnsignedLittleInt()
                                    val swiz = data.readNumber(2, unsigned = true, little = true)
                                    val disp_width = data.readNumber(2, unsigned = true, little = true)
                                    val disp_height = data.readNumber(2, unsigned = true, little = true)
                                    val scanline = data.readNumber(2, unsigned = true, little = true)
                                    val fmt = data.read() and 0xFF
                                    val unk2 = data.read() and 0xFF
                                    val palette = data.read() and 0xFF
                                    val palette_id = data.read() and 0xFF

                                    val (img_data_type, img_data_S, img_subdata_S) = readSRDItem(subdata as WindowedInputStream, srd)!!
                                    img_data_S.use { img_data ->
                                        val initial = img_data.readNumber(2, true, true)
                                        val unk5 = img_data.read() and 0xFF
                                        val mipmap_count = img_data.read() and 0xFF
                                        val later = img_data.readNumber(8, false, true)
                                        val name_offset = img_data.readUnsignedLittleInt()

                                        val mipmaps: MutableList<IntArray> = ArrayList()

                                        for (i in 0 until mipmap_count) {
                                            val mipmap_start = img_data.readUnsignedLittleInt() and 0x00FFFFFF
                                            val mipmap_len = img_data.readUnsignedLittleInt()
                                            val mipmap_unk1 = img_data.readUnsignedLittleInt()
                                            val mipmap_unk2 = img_data.readUnsignedLittleInt()

                                            mipmaps.add(intArrayOf(mipmap_start.toInt(), mipmap_len.toInt(), mipmap_unk1.toInt(), mipmap_unk2.toInt()))
                                        }

                                        val name = img_data_S.useAt(name_offset, InputStream::readNullTerminatedUTF8String)

                                        if (images.containsKey(name)) {
                                            val origImage = images[name]!!
                                            var img = origImage
                                            val nameBytes = name.toByteArray()

                                            val dataLen = (data as WindowedInputStream).windowSize

                                            //First, we construct the subdata so we can write.

                                            val newSubdata = ByteArrayOutputStream()

                                            //Write the initial number
                                            newSubdata.writeShort(initial)

                                            //Unk5
                                            newSubdata.write(unk5)

                                            //Only one mipmap - the main data
                                            newSubdata.write(mipmap_count)

                                            //Write the thing before "name offset"
                                            newSubdata.writeLong(later, false, true)

                                            //Name offset is relatively static - 16 bytes from the start, plus (mipmap_count * 16)
                                            newSubdata.writeInt(16 + (mipmap_count * 16))

                                            //Mipmap values are also fairly static, and we need a number of them.
                                            for (i in 0 until mipmap_count) {
                                                //Image Offset - Write only the first three bytes. For whatever reason, the fourth byte is also 0x40
                                                newSubdata.writeNumber(imgOffset, 3)
                                                newSubdata.write(0x40)

                                                //Image Len - width * height * 4
                                                newSubdata.writeInt(img.width * img.height * 4)

                                                //Two unknown numbers, always the same though
                                                newSubdata.writeInt(128)
                                                newSubdata.writeInt(0)

                                                //Then write the image to the srdv stream
                                                for (y in 0 until img.height)
                                                    for (x in 0 until img.width) {
                                                        val colour = Color(img.getRGB(x, y), true)

                                                        srdvOut.write(colour.blue)
                                                        srdvOut.write(colour.green)
                                                        srdvOut.write(colour.red)
                                                        srdvOut.write(colour.alpha)
                                                    }

                                                imgOffset += img.width * img.height * 4

                                                val newImg = BufferedImage(maxOf(1, img.width / 2), maxOf(1, img.height / 2), BufferedImage.TYPE_INT_ARGB)

                                                val g = newImg.createGraphics()
                                                g.drawImage(img, 0, 0, newImg.width, newImg.height, null)
                                                g.dispose()

                                                img = newImg
                                            }

                                            //Finally, we write the name
                                            newSubdata.write(nameBytes)

                                            //Now we need to write a zero to the end, so we write a single byte and add that to the size
                                            newSubdata.write(0)

                                            val namePadding = (0x10 - (nameBytes.size + 1) % 0x10) % 0x10
                                            for (i in 0 until namePadding)
                                                newSubdata.write(0)

                                            //And last of all, we need to write the ending section and pad it. Fortunately, that's static
                                            newSubdata.write(STATIC_SRD_END)

                                            //Third, write the header info to the main output stream
                                            srdOut.write(data_type.toByteArray())
                                            srdOut.writeInt(dataLen, true, false)
                                            srdOut.writeInt(newSubdata.size() + 0x10, true, false)
                                            srdOut.writeInt(0, true, false)

                                            //First, write unk1
                                            srdOut.writeInt(unk1)
                                            //Then swiz
                                            srdOut.writeShort(swiz)
                                            //Then the width + height of the overriding image
                                            srdOut.writeShort(origImage.width)
                                            srdOut.writeShort(origImage.height)

                                            //Scanline
                                            srdOut.writeShort(scanline)

                                            //Format must **always** be 1, so I don't have to do fancy encoding
                                            srdOut.write(0x01)

                                            //Then write unk2
                                            srdOut.write(unk2)

                                            //And no palettes
                                            srdOut.write(0)
                                            srdOut.write(0)

                                            //And padding
                                            for (i in 0 until (0x10 - dataLen % 0x10) % 0x10)
                                                srdOut.write(0)

                                            //Before we write the subdata, we write the header for said subdata
                                            srdOut.write(STATIC_RSI)
                                            srdOut.writeInt(newSubdata.size() - 16 - namePadding, true, false)
                                            srdOut.writeInt(0, true, false)
                                            srdOut.writeInt(0, true, false)

                                            //Then, we write the prepared subdata and pad
                                            newSubdata.writeTo(srdOut)

                                            for (i in 0 until (0x10 - newSubdata.size() % 0x10) % 0x10)
                                                srdOut.write(0)
                                        } else if(customMipmaps.containsKey(name)) {
                                            val (mipmapFormat, dimensions, newMipmaps) = customMipmaps[name]!!
                                            val nameBytes = name.toByteArray()

                                            val dataLen = (data as WindowedInputStream).windowSize

                                            //First, we construct the subdata so we can write.

                                            val newSubdata = ByteArrayOutputStream()

                                            //Write the initial number
                                            newSubdata.writeShort(initial)

                                            //Unk5
                                            newSubdata.write(unk5)

                                            //Only one mipmap - the main data
                                            newSubdata.write(newMipmaps.size)

                                            //Write the thing before "name offset"
                                            newSubdata.writeLong(later, false, true)

                                            //Name offset is relatively static - 16 bytes from the start, plus (mipmap_count * 16)
                                            newSubdata.writeInt(16 + (mipmap_count * 16))

                                            //Mipmap values are also fairly static, and we need a number of them.
                                            for (i in 0 until newMipmaps.size) {
                                                val (size, mip) = newMipmaps[i]
                                                //Image Offset - Write only the first three bytes. For whatever reason, the fourth byte is also 0x40
                                                newSubdata.writeNumber(imgOffset, 3)
                                                newSubdata.write(0x40)

                                                //Image Len - width * height * 4
                                                newSubdata.writeInt(size)

                                                //Two unknown numbers, always the same though
                                                newSubdata.writeInt(128)
                                                newSubdata.writeInt(0)

                                                //Then write the image to the srdv stream
                                                mip.use { stream -> stream.copyTo(srdvOut) }

                                                imgOffset += size
                                            }

                                            //Finally, we write the name
                                            newSubdata.write(nameBytes)

                                            //Now we need to write a zero to the end, so we write a single byte and add that to the size
                                            newSubdata.write(0)

                                            val namePadding = (0x10 - (nameBytes.size + 1) % 0x10) % 0x10
                                            for (i in 0 until namePadding)
                                                newSubdata.write(0)

                                            //And last of all, we need to write the ending section and pad it. Fortunately, that's static
                                            newSubdata.write(STATIC_SRD_END)

                                            //Third, write the header info to the main output stream
                                            srdOut.write(data_type.toByteArray())
                                            srdOut.writeInt(dataLen, true, false)
                                            srdOut.writeInt(newSubdata.size() + 0x10, true, false)
                                            srdOut.writeInt(0, true, false)

                                            //First, write unk1
                                            srdOut.writeInt(unk1)
                                            //Then swiz
                                            srdOut.writeShort(swiz)
                                            //Then the width + height of the overriding image
                                            srdOut.writeShort(dimensions.width)
                                            srdOut.writeShort(dimensions.height)

                                            //Scanline
                                            srdOut.writeShort(scanline)

                                            //Format must **always** be 1, so I don't have to do fancy encoding
                                            srdOut.write(mipmapFormat)

                                            //Then write unk2
                                            srdOut.write(unk2)

                                            //And no palettes
                                            srdOut.write(0)
                                            srdOut.write(0)

                                            //And padding
                                            for (i in 0 until (0x10 - dataLen % 0x10) % 0x10)
                                                srdOut.write(0)

                                            //Before we write the subdata, we write the header for said subdata
                                            srdOut.write(STATIC_RSI)
                                            srdOut.writeInt(newSubdata.size() - 16 - namePadding, true, false)
                                            srdOut.writeInt(0, true, false)
                                            srdOut.writeInt(0, true, false)

                                            //Then, we write the prepared subdata and pad
                                            newSubdata.writeTo(srdOut)

                                            for (i in 0 until (0x10 - newSubdata.size() % 0x10) % 0x10)
                                                srdOut.write(0)
                                        } else {
                                            val nameBytes = name.toByteArray()
                                            val dataLen = (data as WindowedInputStream).windowSize

                                            //First, we construct the subdata so we can write.

                                            val newSubdata = ByteArrayOutputStream()

                                            //Write the initial number
                                            newSubdata.writeShort(initial)

                                            //Unk5
                                            newSubdata.write(unk5)

                                            //Only one mipmap - the main data
                                            newSubdata.write(mipmap_count)

                                            //Write the thing before "name offset"
                                            newSubdata.writeLong(later, false, true)

                                            //Name offset is relatively static - 16 bytes from the start, plus (mipmap_count * 16)
                                            newSubdata.writeInt(16 + (mipmap_count * 16))

                                            //Mipmap values are also fairly static, and we need a number of them.
                                            for (i in 0 until mipmap_count) {
                                                //Image Offset - Write only the first three bytes. For whatever reason, the fourth byte is also 0x40
                                                newSubdata.writeNumber(imgOffset, 3)
                                                newSubdata.write(0x40)

                                                //Image Len - width * height * 4
                                                newSubdata.writeInt(mipmaps[i][1])

                                                //Two unknown numbers, always the same though
                                                newSubdata.writeInt(mipmaps[i][2])
                                                newSubdata.writeInt(mipmaps[i][3])

                                                val imgData = srdv.useAt(mipmaps[i][0].toLong()) { stream -> stream.read(mipmaps[i][1]) }

                                                srdvOut.write(imgData)

                                                imgOffset += imgData.size
                                            }

                                            //Finally, we write the name
                                            newSubdata.write(nameBytes)

                                            //Now we need to write a zero to the end, so we write a single byte and add that to the size
                                            newSubdata.write(0)

                                            val namePadding = (0x10 - (nameBytes.size + 1) % 0x10) % 0x10
                                            for (i in 0 until namePadding)
                                                newSubdata.write(0)

                                            //And last of all, we need to write the ending section and pad it. Fortunately, that's static
                                            newSubdata.write(STATIC_SRD_END)

                                            //Third, write the header info to the main output stream
                                            srdOut.write(data_type.toByteArray())
                                            srdOut.writeInt(dataLen, true, false)
                                            srdOut.writeInt(newSubdata.size() + 0x10, true, false)
                                            srdOut.writeInt(0, true, false)

                                            //First, write unk1
                                            srdOut.writeInt(unk1)
                                            //Then swiz
                                            srdOut.writeShort(swiz)
                                            //Then the width + height of the overriding image
                                            srdOut.writeShort(disp_width)
                                            srdOut.writeShort(disp_height)

                                            //Scanline
                                            srdOut.writeShort(scanline)

                                            //Write the original thingy
                                            srdOut.write(fmt)

                                            //Then write unk2
                                            srdOut.write(unk2)

                                            //And no palettes
                                            srdOut.write(palette)
                                            srdOut.write(palette_id)

                                            //And padding
                                            for (i in 0 until (0x10 - dataLen % 0x10) % 0x10)
                                                srdOut.write(0)

                                            //Before we write the subdata, we write the header for said subdata
                                            srdOut.write(STATIC_RSI)
                                            srdOut.writeInt(newSubdata.size() - 16 - namePadding, true, false)
                                            srdOut.writeInt(0, true, false)
                                            srdOut.writeInt(0, true, false)

                                            //Then, we write the prepared subdata and pad
                                            newSubdata.writeTo(srdOut)

                                            for (i in 0 until (0x10 - newSubdata.size() % 0x10) % 0x10)
                                                srdOut.write(0)
                                        }
                                    }
                                }
                                else -> {
                                    val dataLen = data.available()
                                    val subdataLen = subdata.available()

                                    srdOut.write(data_type.toByteArray())
                                    srdOut.writeInt(dataLen, true, false)
                                    srdOut.writeInt(subdataLen, true, false)
                                    srdOut.writeInt(0, true, false)

                                    data.copyTo(srdOut)
                                    for (i in 0 until (0x10 - dataLen % 0x10) % 0x10)
                                        srdOut.write(0)

                                    subdata.copyTo(srdOut)
                                    for (i in 0 until (0x10 - subdataLen % 0x10) % 0x10)
                                        srdOut.write(0)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun readSRDItem(stream: CountingInputStream, source: () -> InputStream): Triple<String, () -> WindowedInputStream, () -> WindowedInputStream>? {
        val data_type = stream.readString(4)

        if (data_type.length < 4 || !data_type.startsWith("$"))
            return null

        val data_len = stream.readUnsignedBigInt()
        val subdata_len = stream.readUnsignedBigInt()
        val padding = stream.readUnsignedBigInt()

        val data_padding = (0x10 - data_len % 0x10) % 0x10
        val subdata_padding = (0x10 - subdata_len % 0x10) % 0x10

        val dataOffset = stream.streamOffset
        val data = { WindowedInputStream(source(), dataOffset, data_len) }  //ByteArray(data_len.toInt()).apply { stream.read(this) }
        stream.skip(data_padding + data_len)

        val subdataOffset = stream.streamOffset
        val subdata = { WindowedInputStream(source(), subdataOffset, subdata_len) } //ByteArray(subdata_len.toInt()).apply { stream.read(this) }
        stream.skip(subdata_padding + subdata_len)

        return data_type to data and subdata
    }
}