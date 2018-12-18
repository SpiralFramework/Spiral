package org.abimon.spiral.core.objects.scripting.lin

class StopScriptEntry(): LinScript {
    companion object static {
        @JvmStatic
        fun fromOp(opCode: Int, args: IntArray): StopScriptEntry = StopScriptEntry()
    }

    constructor(opCode: Int, args: IntArray): this()

    override val opCode: Int = 0x1A
    override val rawArguments: IntArray = intArrayOf()

    override fun format(): String = "Stop Script|"
}