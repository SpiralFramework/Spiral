package info.spiralframework.base.common.io

class BitPoolInput(val bytes: ByteArray, size: Int = bytes.size) {
    val maxIndex = size
    var index = 0
    var bitpool: Int
    var bitsLeft: Int

    val isEmpty: Boolean
        get() = index == maxIndex && bitsLeft == 0

    fun read(numBits: Int): Int {
        var outBits = 0
        var bitsProduced = 0

        while (bitsProduced < numBits) {
            if (bitsLeft == 0) {
                bitpool = if (index >= maxIndex) 0 else bytes[index++].toInt().and(0xFF)
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

    fun peek(numBits: Int): Int {
        var outBits = 0
        var bitsProduced = 0

        var shadowedBitpool = bitpool
        var shadowedIndex = index
        var shadowedBitsLeft = bitsLeft

        while (bitsProduced < numBits) {
            if (shadowedBitsLeft == 0) {
                shadowedBitpool = if (shadowedIndex >= maxIndex) 0 else bytes[shadowedIndex++].toInt().and(0xFF)
                shadowedBitsLeft = 8
            }

            val bitsThisRound = minOf(shadowedBitsLeft, numBits - bitsProduced)

            outBits = outBits shl bitsThisRound
            outBits = outBits or ((shadowedBitpool shr (shadowedBitsLeft - bitsThisRound)) and ((1 shl bitsThisRound) - 1))

            shadowedBitsLeft -= bitsThisRound
            bitsProduced += bitsThisRound
        }

        return outBits
    }

    init {
        if (index < maxIndex) {
            bitpool = bytes[index++].toInt().and(0xFF)
            bitsLeft = 8
        } else {
            bitpool = 0
            bitsLeft = 0
        }
    }
}