package info.spiralframework.formats.common.compression

import info.spiralframework.base.common.reverseBits

fun decompressSpcData(data: ByteArray, size: Int = 0): ByteArray {
    val output = ArrayList<Byte>(size)
    var flag = 1
    var pos = 0

    while (pos < data.size) {
        // We use an 8-bit flag to determine whether something is raw data,
        // or if we need to pull from the buffer, going from most to least significant bit.
        // We reverse the bit order to make it easier to work with.
        if (flag == 1) {
            // Add an extra "1" bit so our last flag value will always cause us to read new flag data.
            flag = 0x100 or data[pos++].reverseBits()
        }

        if (pos >= data.size) {
            //Overkill?
            break
        }

        if (flag and 1 == 1) {
            // Raw byte
            output.add(data[pos++])
        } else {
            // Pull from the buffer
            // xxxxxxyy yyyyyyyy
            // Count  -> x + 2 (max length of 65 bytes)
            // Offset -> y (from the beginning of a 1023-byte sliding window)

            val b = data[pos++].toInt() and 0xFF or (data[pos++].toInt() and 0xFF shl 8)

            val count = (b shr 10) + 2
            val offset = output.size - 1024 + (b and 0x3FF)

            for (i in 0 until count) {
                output.add(output[offset + i])
            }
//                buffer.addAll(buffer.slice((buffer.size - 1024 + offset) until (buffer.size - 1024 + offset + count)))
        }

        flag = flag shr 1
    }

    return output.toByteArray()
}