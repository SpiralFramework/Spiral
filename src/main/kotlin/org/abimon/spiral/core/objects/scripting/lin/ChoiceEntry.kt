package org.abimon.spiral.core.objects.scripting.lin

data class ChoiceEntry(val arg1: Int): LinScript {
    constructor(opCode: Int, args: IntArray): this(args[0])

    override val opCode: Int = 0x2B
    override val rawArguments: IntArray = intArrayOf(arg1)

    override fun format(): String = "Choice|$arg1"
}