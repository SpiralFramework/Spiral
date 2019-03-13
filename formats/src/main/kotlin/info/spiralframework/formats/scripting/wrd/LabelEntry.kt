package info.spiralframework.formats.scripting.wrd

data class LabelEntry(val labelID: Int): WrdScript {
    constructor(opCode: Int, args: IntArray): this(args[0])

    override val opCode: Int = 0x14
    override val rawArguments: IntArray = intArrayOf(labelID)
}