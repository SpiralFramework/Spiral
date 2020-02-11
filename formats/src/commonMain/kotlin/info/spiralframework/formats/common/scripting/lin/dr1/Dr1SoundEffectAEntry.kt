package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

inline class Dr1SoundEffectAEntry(override val rawArguments: IntArray): MutableLinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    constructor(sfxID: Int, volume: Int): this(intArrayOf((sfxID shr 8) and 0xFF, sfxID and 0xFF, volume))

    override val opcode: Int
        get() = 0x0A

    var sfxID: Int
        get() = getInt16BE(0)
        set(value) = setInt16BE(0, value)

    var volume: Int
        get() = get(0)
        set(value) = set(2, value)
}