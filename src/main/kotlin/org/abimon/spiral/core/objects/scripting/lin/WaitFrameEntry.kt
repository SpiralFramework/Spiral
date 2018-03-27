package org.abimon.spiral.core.objects.scripting.lin

class WaitFrameEntry(override val opCode: Int): LinScript {
    companion object {
        val DR1: WaitFrameEntry
            get() = WaitFrameEntry(0x3B)

        val DR2: WaitFrameEntry
            get() = WaitFrameEntry(0x4C)
    }
    constructor(opCode: Int, args: IntArray): this(opCode)

    override val rawArguments: IntArray = intArrayOf()
}