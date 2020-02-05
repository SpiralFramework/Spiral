package info.spiralframework.formats.common.scripting.lin

interface LinEntry {
    companion object {
        val EMPTY_ARGUMENT_ARRAY = IntArray(0)
    }

    val opcode: Int
    val rawArguments: IntArray

    fun get(index: Int): Int = rawArguments[index]
    fun getInt16LE(index: Int) = (rawArguments[index] or (rawArguments[index + 1] shl 8))
    fun getInt16BE(index: Int) = (rawArguments[index + 1] or (rawArguments[index] shl 8))

    /**
     * This should be an interpretable statement in something like OSL
     */
    fun format(): String = "0x${opcode.toString(16).padStart(2, '0').toUpperCase()}|${rawArguments.joinToString()}"
}

interface MutableLinEntry: LinEntry {
    fun set(index: Int, value: Int) {
        rawArguments[index] = value and 0xFF
    }
    fun setInt16LE(index: Int, value: Int) {
        rawArguments[index + 0] = (value shr 0) and 0xFF
        rawArguments[index + 1] = (value shr 8) and 0xFF
    }
    fun setInt16BE(index: Int, value: Int) {
        rawArguments[index + 0] = (value shr 8) and 0xFF
        rawArguments[index + 1] = (value shr 0) and 0xFF
    }
}