package org.abimon.spiral.core.objects.scripting.lin

data class MovieEntry(val id: Int): LinScript {
    constructor(op: Int, args: IntArray) : this((args[0] shl 8) or args[1])

    override val opCode: Int = 0x05
    override val rawArguments: IntArray = intArrayOf(id shr 8, id % 256)

    override fun format(): String = "Movie|${id shr 8}, ${id % 256}"
}