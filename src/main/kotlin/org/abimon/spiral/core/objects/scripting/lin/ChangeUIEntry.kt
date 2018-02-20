package org.abimon.spiral.core.objects.scripting.lin

data class ChangeUIEntry(val element: Int, val state: Int): LinScript {
    constructor(opCode: Int, args: IntArray): this(args[0], args[1])

    override val opCode: Int = 0x25
    override val rawArguments: IntArray = intArrayOf(element, state)
}