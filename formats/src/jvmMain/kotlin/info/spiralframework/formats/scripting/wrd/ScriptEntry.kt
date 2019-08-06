package info.spiralframework.formats.scripting.wrd

data class ScriptEntry(val scriptID: Int, val labelID: Int): WrdScript {
    constructor(opCode: Int, args: IntArray): this(args[0], args[1])

    override val opCode: Int = 0x10
    override val rawArguments: IntArray = intArrayOf(scriptID, labelID)
}