package info.spiralframework.formats.scripting.lin

import info.spiralframework.formats.common.scripting.lin.LinEntry

data class SoundEffectAEntry(val id: Int, val volume: Int = 100): LinEntry {
    constructor(op: Int, args: IntArray): this((args[0] shl 8) or args[1], args[2])

    override val opcode: Int = 0x0A
    override val rawArguments: IntArray = intArrayOf(id shr 8, id % 256, volume)

    override fun format(): String = "SFX A|${id shr 8}, ${id % 256}, $volume"
}