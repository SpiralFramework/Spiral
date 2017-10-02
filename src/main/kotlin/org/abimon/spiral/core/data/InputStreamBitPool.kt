package org.abimon.spiral.core.data

import java.io.InputStream

class InputStreamBitPool(val inputStream: InputStream) {
    var bitpool = 0
    var bitsLeft = 0

    val isEmpty: Boolean
        get() = inputStream.available() == 0

    fun read(numBits: Int): Int {
        var outBits = 0
        var bitsProduced = 0

        while (bitsProduced < numBits) {
            if (bitsLeft == 0) {
                bitpool = if (isEmpty) 0 else inputStream.read()
                bitsLeft = 8
            }

            val bitsThisRound = minOf(bitsLeft, numBits - bitsProduced)

            outBits = outBits shl bitsThisRound
            outBits = outBits or ((bitpool shr (bitsLeft - bitsThisRound)) and ((1 shl bitsThisRound) - 1))

            bitsLeft -= bitsThisRound
            bitsProduced += bitsThisRound
        }

        return outBits
    }

    operator fun get(bits: Int): Int = read(bits)
}