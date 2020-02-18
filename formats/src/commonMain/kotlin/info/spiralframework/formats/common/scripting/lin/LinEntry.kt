package info.spiralframework.formats.common.scripting.lin

import info.spiralframework.formats.common.scripting.osl.LinTranspiler

interface LinEntry {
    companion object {
        val EMPTY_ARGUMENT_ARRAY = IntArray(0)
    }

    val opcode: Int
    val rawArguments: IntArray

    fun get(index: Int): Int = rawArguments[index]
    fun getInt16LE(index: Int) = (rawArguments[index] or (rawArguments[index + 1] shl 8))
    fun getInt16BE(index: Int) = (rawArguments[index + 1] or (rawArguments[index] shl 8))
    fun getInt24LE(index: Int) = (rawArguments[index] or (rawArguments[index + 1] shl 8) or (rawArguments[index + 2] shl 16))
    fun getInt24BE(index: Int) = (rawArguments[index + 2] or (rawArguments[index + 1] shl 8) or (rawArguments[index] shl 16))

    @ExperimentalUnsignedTypes
    fun LinTranspiler.transpile(indent: Int = 0) {
        addOutput {
            repeat(indent) { append('\t') }
            append(nameFor(this@LinEntry))
            append('|')
            transpileArguments(this)
        }
    }

    @ExperimentalUnsignedTypes
    fun LinTranspiler.transpileArguments(builder: StringBuilder) {
        rawArguments.joinTo(builder)
    }
}

interface MutableLinEntry : LinEntry {
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

    fun setInt24LE(index: Int, value: Int) {
        rawArguments[index + 0] = (value shr 0) and 0xFF
        rawArguments[index + 1] = (value shr 8) and 0xFF
        rawArguments[index + 2] = (value shr 16) and 0xFF
    }

    fun setInt24BE(index: Int, value: Int) {
        rawArguments[index + 0] = (value shr 16) and 0xFF
        rawArguments[index + 1] = (value shr 8) and 0xFF
        rawArguments[index + 2] = (value shr 0) and 0xFF
    }
}

@ExperimentalUnsignedTypes
fun LinEntry.transpile(transpiler: LinTranspiler, indent: Int = 0) = transpiler.transpile(indent)

@ExperimentalUnsignedTypes
fun LinEntry.transpileArguments(transpiler: LinTranspiler, builder: StringBuilder) = transpiler.transpileArguments(builder)