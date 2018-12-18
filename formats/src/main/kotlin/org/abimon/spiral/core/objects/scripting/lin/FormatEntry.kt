package org.abimon.spiral.core.objects.scripting.lin

data class FormatEntry(val format: Int): LinScript {
    constructor(op: Int, args: IntArray): this(args[0])

    override val opCode: Int = 0x03
    override val rawArguments: IntArray = intArrayOf(format)

    override fun format(): String = "Format|$format"
}