package org.abimon.spiral.core.objects.scripting.lin

data class ShowBackgroundEntry(val backgroundID: Int, val state: Int): LinScript {
    constructor(opCode: Int, args: IntArray): this((args[1] shl 8) or args[0], args[2])

    override val opCode: Int = 0x30
    override val rawArguments: IntArray = intArrayOf(backgroundID % 256, backgroundID shr 8, state)

    override fun format(): String = "Show Background|$backgroundID, $state"
}