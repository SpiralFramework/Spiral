package org.abimon.spiral.core.objects.scripting.lin

class EndFlagCheckEntry(): LinScript {
    constructor(opCode: Int, args: IntArray): this()

    override val opCode: Int = 0x3C
    override val rawArguments: IntArray = intArrayOf()
}