package org.abimon.spiral.core.objects.scripting.lin

class WaitForInputEntry(override val opCode: Int): LinScript {
    companion object {
        val DR1: WaitForInputEntry
            get() = WaitForInputEntry(0x3A)
        val DR2: WaitForInputEntry
            get() = WaitForInputEntry(0x4B)
    }

    constructor(opCode: Int, args: IntArray): this(opCode)

    override val rawArguments: IntArray = intArrayOf()

    override fun format(): String = "Wait For Input|"
}