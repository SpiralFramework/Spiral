package org.abimon.spiral.core.objects.scripting.lin

data class SetLabelEntry(val id: Int): LinScript {
    constructor(opCode: Int, args: IntArray): this((args[1] shl 8) or args[0])

    override val opCode: Int = 0x2A
    override val rawArguments: IntArray = intArrayOf(id % 256, id shr 8)
}