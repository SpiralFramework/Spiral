package org.abimon.spiral.core.data

import org.abimon.visi.io.DataSource
import java.util.*

class BitPool(data: ByteArray, val reversed: Boolean = true) {
    constructor(data: DataSource, reversed: Boolean = true) : this(data.data, reversed)

    val bytes: Queue<Int> = data.apply { if(reversed) reverse() }.map { it.toInt() and 0xFF }.toCollection(LinkedList())
    var bitpool = 0
    var bitsLeft = 0

//    val getNextBits: (Int) -> Int = func@ { bit_count ->
//        var out_bits = 0
//        var num_bits_produced = 0
//
//        while (num_bits_produced < bit_count) {
//            if (bits_left_p == 0) {
//                stream.seek(offset_p)
//                bit_pool_p = stream.read()
//                bits_left_p = 8
//                offset_p -= 1
//            }
//
//            val bits_this_round: Int = minOf(bits_left_p, bit_count - num_bits_produced)
//
//            out_bits = out_bits shl bits_this_round
//            out_bits = out_bits or ((bit_pool_p shr (bits_left_p - bits_this_round)) and ((1 shl bits_this_round) - 1))
//
//            bits_left_p -= bits_this_round
//            num_bits_produced += bits_this_round
//        }
//
//        return@func out_bits
//    }

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