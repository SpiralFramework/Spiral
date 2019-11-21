package info.spiralframework.formats.common.scripting.lin

inline class SpriteEntry(override val rawArguments: IntArray): LinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x1E

    val position: Int
        get() = rawArguments[0]

    val characterID: Int
        get() = rawArguments[1]

    val spriteID: Int
        get() = rawArguments[2]

    val state: Int
        get() = rawArguments[3]

    val transition: Int
        get() = rawArguments[4]

    override fun format(): String = "Sprite|$position, $characterID, $spriteID, $state, $transition"
}