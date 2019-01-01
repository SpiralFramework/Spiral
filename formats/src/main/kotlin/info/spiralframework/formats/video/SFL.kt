package info.spiralframework.formats.video

import info.spiralframework.base.assertAsLocaleArgument
import info.spiralframework.formats.utils.*
import java.io.InputStream

class SFL private constructor(val dataSource: () -> InputStream) {
    companion object {
        val MAGIC_NUMBER = 0x53464C4C
        
        operator fun invoke(dataSource: DataSource): SFL? {
            try {
                 return SFL(dataSource)
            } catch (iae: IllegalArgumentException) {
                DataHandler.LOGGER.debug("formats.sfl.invalid", dataSource, iae)
                
                return null
            }
        }

        fun unsafe(dataSource: DataSource): SFL = SFL(dataSource)
    }

    data class SFLImage(val width: Int, val height: Int, val unk1: Int, val unk2: Int, val unk3: Int, val unk4: Int)
    data class SFLDisplayImage(val originalWidth: Int, val displayWidth: Int, val originalHeight: Int, val displayHeight: Int, val unk1: Int, val unk2: Int)
    data class SFLCommand(val dataLength: Int, val headerLength: Int, val unk1: Int, val header: ByteArray, val data: ByteArray)

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
    val unkPossibleImageData: Array<ByteArray>

    val extraByteData: ByteArray

    val commands: Array<SFLCommand>

    init {
        val stream = CountingInputStream(dataSource())

        try {
            val magic = stream.readInt32LE()
            assertAsLocaleArgument(magic == MAGIC_NUMBER, "formats.sfl.invalid_magic", "0x${magic.toString(16)}", "0x${MAGIC_NUMBER.toString(16)}")

            headerUnk1 = stream.readInt32LE()
            headerUnk2 = stream.readInt32LE()
            headerUnk3 = stream.readInt32LE()

            unk1 = stream.readInt32LE()
            unk2 = stream.readInt32LE()
            unk3 = stream.readInt32LE()
            unk4 = stream.readInt32LE()

            if (unk1 != 1)
                DataHandler.LOGGER.debug("formats.sfl.unk1", unk1)
            if (unk2 != 0)
                DataHandler.LOGGER.debug("formats.sfl.unk2", unk1)
            if (unk3 != 0)
                DataHandler.LOGGER.debug("formats.sfl.unk3", unk1)
            if (unk4 != 0)
                DataHandler.LOGGER.debug("formats.sfl.unk4", unk1)

            /** Unk2 seems to be some kind of header size, so we read that many bytes? */
            headerUnk = ByteArray(unk2)
            stream.read(headerUnk)

            unk5 = stream.readInt32LE()
            if (unk5 != 2)
                DataHandler.LOGGER.debug("formats.sfl.unk5", unk1)

            unkFrameCount = stream.readInt32LE()
            frameCount = stream.readInt32LE()
            unk6 = stream.readInt32LE()
            if (unk6 != 0)
                DataHandler.LOGGER.debug("formats.sfl.unk6", unk1)

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

            println(stream.count)

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

            println(stream.count)

            //I don't understand how, but I'm pretty sure these are bits of image data, I think
            unkPossibleImageData = Array(images.last().unk3 and 0xFF) {
                val byteArray = ByteArray(40)
                stream.read(byteArray)

                return@Array byteArray
            }

            when (unk3) {
                0 -> extraByteData = ByteArray(0)
                1 -> extraByteData = ByteArray(56)
                2 -> extraByteData = ByteArray(112)
                3 -> extraByteData = ByteArray(112)
                4 -> extraByteData = ByteArray(176)
                else -> {
//                    DataHandler.LOGGER.debug("unk3 in SFL file {} is {}", dataSource, unk3)
                    extraByteData = ByteArray(0)
                }
            }

            stream.read(extraByteData)

            val commandList = ArrayList<SFLCommand>()

            /** SFL COMMAND: <data size, uint32> <some header? 2 uint16s> <command itself, 8 bytes>, <data size bytes> */
            while (true) {
                val dataSize = stream.readInt32LE()
                val headerSize = stream.readInt16LE()
                val unk1 = stream.readInt16LE()

                if (dataSize < 0 || headerSize < 8 || unk1 == -1)
                    break

                val header = ByteArray(headerSize - 8)
                val data = ByteArray(dataSize)

                if (stream.read(header) == -1)
                    break

                if (stream.read(data) == -1)
                    break

                commandList.add(SFLCommand(dataSize, headerSize, unk1, header, data))
            }

            commands = commandList.toTypedArray()
        } finally {
            stream.close()
        }
    }
}