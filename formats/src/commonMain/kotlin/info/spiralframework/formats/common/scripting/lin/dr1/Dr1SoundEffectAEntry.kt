package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

public class Dr1SoundEffectAEntry(override val rawArguments: IntArray): MutableLinEntry {
    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    public constructor(sfxID: Int, volume: Int): this(intArrayOf((sfxID shr 8) and 0xFF, sfxID and 0xFF, volume))

    override val opcode: Int
        get() = 0x0A

    public var sfxID: Int
        get() = getInt16BE(0)
        set(value) = setInt16BE(0, value)

    public var volume: Int
        get() = get(0)
        set(value) = set(2, value)
}