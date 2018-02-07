package org.abimon.spiral.core.objects.scripting.lin

data class TextEntry(var text: String?, val textID: Int): LinScript {
    constructor(op: Int, args: IntArray): this(null, (args[0] shl 8) or args[1])

    override val opCode: Int = 0x02
    override val rawArguments: IntArray = intArrayOf()
}