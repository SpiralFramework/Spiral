package org.abimon.spiral.core.objects.scripting.lin

data class TextCountEntry(val lines: Int): LinScript {
    constructor(rem: Int, main: Int): this(rem + main * 256)
    constructor(op: Int, args: IntArray): this(args[0], args[1])

    override val opCode: Int = 0x00
    override val rawArguments: IntArray = intArrayOf(lines % 256, lines / 256)
}