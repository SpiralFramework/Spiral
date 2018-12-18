package org.abimon.spiral.core.utils

import java.util.*

/** Specifically called CriBitPool because it needs to be reversed because fuck you */
class CriBitPool(data: ByteArray, val reversed: Boolean = true) {
    val bytes: Queue<Int> = data.apply { if(reversed) reverse() }.map { it.toInt() and 0xFF }.toCollection(LinkedList())
    var bitpool = 0
    var bitsLeft = 0

    val isEmpty: Boolean
        get() = bytes.isEmpty()

    fun read(numBits: Int): Int {
        var outBits = 0
        var bitsProduced = 0

        while (bitsProduced < numBits) {
            if (bitsLeft == 0) {
                bitpool = if (isEmpty) 0 else bytes.poll()
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