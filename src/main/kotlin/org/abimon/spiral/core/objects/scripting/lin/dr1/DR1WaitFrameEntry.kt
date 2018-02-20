package org.abimon.spiral.core.objects.scripting.lin.dr1

import org.abimon.spiral.core.objects.scripting.lin.LinScript

class DR1WaitFrameEntry(): LinScript {
    constructor(opCode: Int, args: IntArray): this()

    override val opCode: Int = 0x3B
    override val rawArguments: IntArray = intArrayOf()
}