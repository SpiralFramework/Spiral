package org.abimon.spiral.core.objects.scripting.lin.dr2

import org.abimon.spiral.core.objects.scripting.lin.LinScript

class DR2WaitForInputEntry(): LinScript {
    constructor(opCode: Int, args: IntArray): this()

    override val opCode: Int = 0x4B
    override val rawArguments: IntArray = intArrayOf()
}