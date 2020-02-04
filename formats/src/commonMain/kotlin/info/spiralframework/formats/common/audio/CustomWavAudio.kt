package info.spiralframework.formats.common.audio

import org.abimon.kornea.io.common.flow.BinaryOutputFlow
import org.abimon.kornea.io.common.flow.OutputFlow
import org.abimon.kornea.io.common.writeInt16LE
import org.abimon.kornea.io.common.writeInt32LE

//TODO: Fix terminology
@ExperimentalUnsignedTypes
class CustomWavAudio {
    companion object {
        val MAGIC_NUMBER_LE = 0x46464952
        val TYPE_MAGIC_NUMBER_LE = 0x45564157
        val FORMAT_CHUNK_MAGIC_NUMBER_LE = 0x20746D66
        val DATA_CHUNK_MAGIC_NUMBER_LE = 0x61746164
    }

    var numberOfChannels: Int = 0
    var sampleRate: Int = 44100

    private val pcmSamples: BinaryOutputFlow = BinaryOutputFlow()

    suspend fun addSamples(array: ShortArray) {
        array.forEach { item -> pcmSamples.writeInt16LE(item) }
    }

    suspend fun addSamples(list: List<Short>) {
        list.forEach { item -> pcmSamples.writeInt16LE(item) }
    }

    @ExperimentalUnsignedTypes
    suspend fun write(out: OutputFlow) {
        val sampleDataSize = pcmSamples.getDataSize().toInt()
        out.writeInt32LE(MAGIC_NUMBER_LE)                                //Marks the file as a riff file. Characters are each 1 byte long.
        out.writeInt32LE(sampleDataSize + 44)                       //Size of the overall file - 8 bytes, in bytes (32-bit integer). Typically, you'd fill this in after creation.
        out.writeInt32LE(TYPE_MAGIC_NUMBER_LE)                           //File Type Header. For our purposes, it always equals "WAVE".
        out.writeInt32LE(FORMAT_CHUNK_MAGIC_NUMBER_LE)                   //Format chunk marker. Includes trailing null
        out.writeInt32LE(16)                                        //Length of format data as listed above
        out.writeInt16LE(1)                                         //Type of format (1 is PCM) - 2 byte integer
        out.writeInt16LE(numberOfChannels)                               //Number of Channels - 2 byte integer
        out.writeInt32LE(sampleRate)                                     //Sample Rate - 32 byte integer. Common values are 44100 (CD), 48000 (DAT). Sample Rate = Number of Samples per second, or Hertz
        out.writeInt32LE((sampleRate * 16 * numberOfChannels) / 8)  //(Sample Rate * BitsPerSample * Channels) / 8.
        out.writeInt16LE((16 * numberOfChannels) / 8)               //(BitsPerSample * Channels) / 8. 1 - 8 bit mono, 2 - 8 bit stereo / 16 bit mono, 4 - 16 bit stereo
        out.writeInt16LE(16)                                        //Bits per sample
        out.writeInt32LE(DATA_CHUNK_MAGIC_NUMBER_LE)                     //"data" chunk header. Marks the beginning of the data section.
        out.writeInt32LE(sampleDataSize)                                 //Size of the data section.
        out.write(pcmSamples.getData())
    }
}