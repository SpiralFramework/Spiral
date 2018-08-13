package org.abimon.spiral.core.objects.scripting.lin

data class GoToLabelEntry(val labelID: Int): LinScript {
    constructor(opCode: Int, args: IntArray): this((args[1] shl 8) or args[0])

    override val opCode: Int = 0x34
    override val rawArguments: IntArray = intArrayOf(labelID % 256, labelID shr 8)

    override fun format(): String = "Go To Label|${labelID % 256}, ${labelID shr 8}"
}