package org.abimon.spiral.core.objects.video

import org.abimon.spiral.core.utils.*
import java.io.InputStream

class SFL(val dataSource: () -> InputStream) {
    companion object {
        val MAGIC_NUMBER = 0x53464C4C
    }

    data class SFLImage(val width: Int, val height: Int, val unk1: Int, val unk2: Int, val unk3: Int, val unk4: Int)
    data class SFLDisplayImage(val originalWidth: Int, val displayWidth: Int, val originalHeight: Int, val displayHeight: Int, val unk1: Int, val unk2: Int)
    
    val headerUnk1: Int
    val headerUnk2: Int
    val headerUnk3: Int
    
    val unk1: Int
    val unk2: Int
    val unk3: Int
    val unk4: Int

    val headerUnk: ByteArray
    
    val unk5: Int
    val unkFrameCount: Int
    val frameCount: Int
    val unk6: Int

    val unk8: Int
    val unk9: Int
    val unk10: Int
    val unk11: Int

    val images: Array<SFLImage>
    val displayImages: Array<SFLDisplayImage>

    init {
        val stream = dataSource()

        try {
            val magic = stream.readInt32LE()
            assertAsArgument(magic == MAGIC_NUMBER, "Illegal magic number for SFL file $dataSource (Expected ${MAGIC_NUMBER.toHex()}, got ${magic.toHex()})")

            headerUnk1 = stream.readInt32LE()
            headerUnk2 = stream.readInt32LE()
            headerUnk3 = stream.readInt32LE()

            unk1 = stream.readInt32LE()
            unk2 = stream.readInt32LE()
            unk3 = stream.readInt32LE()
            unk4 = stream.readInt32LE()

            if (unk1 != 1)
                DataHandler.LOGGER.debug("unk1 in SFL file {} is not 1, is {}", dataSource, unk1)
            if (unk2 != 0)
                DataHandler.LOGGER.debug("unk2 in SFL file {} is not 0, is {}", dataSource, unk2)
            if (unk3 != 0)
                DataHandler.LOGGER.debug("unk3 in SFL file {} is not 0, is {}", dataSource, unk3)
            if (unk4 != 0)
                DataHandler.LOGGER.debug("unk4 in SFL file {} is not 0, is {}", dataSource, unk4)

            /** Unk2 seems to be some kind of header size, so we read that many bytes? */
            headerUnk = ByteArray(unk2)
            stream.read(headerUnk)

            unk5 = stream.readInt32LE()
            if (unk5 != 2)
                DataHandler.LOGGER.debug("unk5 in SFL file {} is not 2, is {}", dataSource, unk5)

            unkFrameCount = stream.readInt32LE()
            frameCount = stream.readInt32LE()
            unk6 = stream.readInt32LE()
            if (unk6 != 0)
                DataHandler.LOGGER.debug("unk6 in SFL file {} is not 0, is {}", dataSource, unk6)

            unk8 = stream.readInt32LE()
            unk9 = stream.readInt32LE()
            unk10 = stream.readInt32LE()
            unk11 = stream.readInt32LE()

            images = Array(frameCount) {
                return@Array SFLImage(
                        stream.readInt16LE(),
                        stream.readInt16LE(),

                        stream.readInt32LE(),
                        stream.readInt32LE(),
                        stream.readInt32LE(),
                        stream.readInt32LE()
                )
            }

            stream.skip(24)

            displayImages = Array(frameCount) {
                return@Array SFLDisplayImage(
                        stream.readInt32LE(),
                        stream.readInt32LE(),

                        stream.readInt32LE(),
                        stream.readInt32LE(),

                        stream.readInt32LE(),
                        stream.readInt32LE()
                )
            }

            /** SFL COMMAND: <data size, uint32> <some header? 2 uint16s> <command itself, 8 bytes>, <data size bytes> */
        } finally {
            stream.close()
        }
    }
}