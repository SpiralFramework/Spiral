package info.spiralframework.formats.common.scripting.lin

inline class StopScriptEntry(override val rawArguments: IntArray): LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x1A

    override fun format(): String = "Stop Script|"
}