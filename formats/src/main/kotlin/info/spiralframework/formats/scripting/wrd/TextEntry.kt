package info.spiralframework.formats.scripting.wrd

data class TextEntry(val id: Int): WrdScript {
    override val opCode: Int = 0x46
    override val rawArguments: IntArray = intArrayOf(id)
}