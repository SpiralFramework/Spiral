package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry

inline class Dr1AnimationEntry(override val rawArguments: IntArray) : LinEntry {
    constructor(opcode: Int, rawArguments: IntArray): this(rawArguments)
    constructor(id: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: Int, frame: Int): this(intArrayOf(id shr 8, id % 256, arg3, arg4, arg5, arg6, arg7, frame))

    override val opcode: Int
        get() = 0x06

    val id: Int
        get() = (rawArguments[0] shl 8) or rawArguments[1]
    val arg3: Int
        get() = rawArguments[2]
    val arg4: Int
        get() = rawArguments[3]
    val arg5: Int
        get() = rawArguments[4]
    val arg6: Int
        get() = rawArguments[5]
    val arg7: Int
        get() = rawArguments[6]
    val frame: Int
        get() = rawArguments[7]

    override fun format(): String = "Animation|${rawArguments[0]}, ${rawArguments[1]}, $arg3, $arg4, $arg5, $arg6, $arg7, $frame"
}