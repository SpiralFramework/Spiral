package info.spiralframework.formats.common.scripting.lin

/** Give truth bullet? */
inline class TruthBulletEntry(override val rawArguments: IntArray): LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x0C

    val arg1: Int
        get() = rawArguments[0]

    val arg2: Int
        get() = rawArguments[1]

    override fun format(): String = "Truth Bullet|$arg1, $arg2"
}