package org.abimon.spiral.core.objects.scripting.lin

data class SoundEffectAEntry(val id: Int, val volume: Int = 100): LinScript {
    constructor(op: Int, args: IntArray): this((args[0] shl 8) or args[1], args[2])

    override val opCode: Int = 0x0A
    override val rawArguments: IntArray = intArrayOf(id shr 8, id % 256, volume)
}