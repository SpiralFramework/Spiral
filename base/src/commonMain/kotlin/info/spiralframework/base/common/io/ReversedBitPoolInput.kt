package info.spiralframework.base.common.io

class ReversedBitPoolInput(val bytes: ByteArray, size: Int = bytes.size) {
    val minIndex = bytes.size - size
    var index = bytes.size - 1
    var bitpool: Int
    var bitsLeft: Int

    val isEmpty: Boolean
        get() = index == minIndex && bitsLeft == 0

    fun read(numBits: Int): Int {
        var outBits = 0
        var bitsProduced = 0

        while (bitsProduced < numBits) {
            if (bitsLeft == 0) {
                bitpool = if (index < minIndex) 0 else bytes[index--].toInt().and(0xFF)
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

    init {
        if (index >= minIndex) {
            bitpool = bytes[index--].toInt().and(0xFF)
            bitsLeft = 8
        } else {
            bitpool = 0
            bitsLeft = 0
        }
    }
}