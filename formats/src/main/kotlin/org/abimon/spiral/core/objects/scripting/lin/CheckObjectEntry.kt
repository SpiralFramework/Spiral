package org.abimon.spiral.core.objects.scripting.lin

data class CheckObjectEntry(val objectID: Int): LinScript {
    constructor(opCode: Int, args: IntArray): this(args[0])

    override val opCode: Int = 0x29
    override val rawArguments: IntArray = intArrayOf(objectID)

    override fun format(): String = "Check Object|$objectID"
}