package info.spiralframework.formats.common.scripting.lin

import info.spiralframework.formats.common.scripting.osl.LinTranspiler

public interface LinEntry {
    public companion object {
        public val EMPTY_ARGUMENT_ARRAY: IntArray = IntArray(0)
    }

    public val opcode: Int
    public val rawArguments: IntArray

    public fun test(index: Int, against: Int): Boolean = rawArguments[index] == against
    public fun get(index: Int): Int = rawArguments[index]
    public fun getInt16LE(index: Int): Int = (rawArguments[index] or (rawArguments[index + 1] shl 8))
    public fun getInt16BE(index: Int): Int = (rawArguments[index + 1] or (rawArguments[index] shl 8))
    public fun getInt24LE(index: Int): Int =
        (rawArguments[index] or (rawArguments[index + 1] shl 8) or (rawArguments[index + 2] shl 16))

    public fun getInt24BE(index: Int): Int =
        (rawArguments[index + 2] or (rawArguments[index + 1] shl 8) or (rawArguments[index] shl 16))

    public fun LinTranspiler.transpile(indent: Int = 0) {
        addOutput {
            repeat(indent) { append('\t') }
            append(nameFor(this@LinEntry))
            append('|')
            transpileArguments(this)
        }
    }

    public fun LinTranspiler.transpileArguments(builder: StringBuilder) {
        rawArguments.joinTo(builder)
    }
}

public interface MutableLinEntry : LinEntry {
    public fun set(index: Int, value: Int) {
        rawArguments[index] = value and 0xFF
    }

    public fun setInt16LE(index: Int, value: Int) {
        rawArguments[index + 0] = (value shr 0) and 0xFF
        rawArguments[index + 1] = (value shr 8) and 0xFF
    }

    public fun setInt16BE(index: Int, value: Int) {
        rawArguments[index + 0] = (value shr 8) and 0xFF
        rawArguments[index + 1] = (value shr 0) and 0xFF
    }

    public fun setInt24LE(index: Int, value: Int) {
        rawArguments[index + 0] = (value shr 0) and 0xFF
        rawArguments[index + 1] = (value shr 8) and 0xFF
        rawArguments[index + 2] = (value shr 16) and 0xFF
    }

    public fun setInt24BE(index: Int, value: Int) {
        rawArguments[index + 0] = (value shr 16) and 0xFF
        rawArguments[index + 1] = (value shr 8) and 0xFF
        rawArguments[index + 2] = (value shr 0) and 0xFF
    }
}

public fun LinEntry.transpile(transpiler: LinTranspiler, indent: Int = 0): Unit =
    transpiler.transpile(indent)

public fun LinEntry.transpileArguments(transpiler: LinTranspiler, builder: StringBuilder): Unit =
    transpiler.transpileArguments(builder)