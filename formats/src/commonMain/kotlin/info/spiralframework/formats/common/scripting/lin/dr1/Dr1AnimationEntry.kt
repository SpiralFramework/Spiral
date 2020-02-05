package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

inline class Dr1AnimationEntry(override val rawArguments: IntArray) : MutableLinEntry {
    constructor(opcode: Int, rawArguments: IntArray): this(rawArguments)
    constructor(id: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: Int, frame: Int): this(intArrayOf(id shr 8, id % 256, arg3, arg4, arg5, arg6, arg7, frame))

    override val opcode: Int
        get() = 0x06

    var id: Int
        get() = (rawArguments[0] shl 8) or rawArguments[1]
        set(value) = setInt16BE(0, value)
    var arg3: Int
        get() = rawArguments[2]
        set(value) = set(2, value)
    var arg4: Int
        get() = rawArguments[3]
        set(value) = set(3, value)
    var arg5: Int
        get() = rawArguments[4]
        set(value) = set(4, value)
    var arg6: Int
        get() = rawArguments[5]
        set(value) = set(5, value)
    var arg7: Int
        get() = rawArguments[6]
        set(value) = set(6, value)
    var frame: Int
        get() = rawArguments[7]
        set(value) = set(7, value)

    override fun format(): String = "Animation|${rawArguments[0]}, ${rawArguments[1]}, $arg3, $arg4, $arg5, $arg6, $arg7, $frame"
}