package org.abimon.spiral.util

operator fun SemanticVersion.compareTo(semver: SemanticVersion): Int {
    if(this.first > semver.first)
        return 1
    else if(this.first < semver.first)
        return -1

    if(this.second > semver.second)
        return 1
    else if(this.second < semver.second)
        return -1

    if(this.third > semver.third)
        return 1
    else if(this.third < semver.third)
        return -1

    return 0
}

fun intArrayOfPairs(vararg pairs: Pair<Int, Int>): IntArray {
    val array = IntArray(pairs.size * 2)
    for(i in pairs.indices) {
        val (a, b) = pairs[i]
        array[i * 2] = a
        array[i * 2 + 1] = b
    }

    return array
}

fun Pair(array: IntArray): Pair<Int, Int> = Pair(array[0], array[1])

fun Number.toUnsignedByte(): Int = this.toByte().toInt() and 0xFF