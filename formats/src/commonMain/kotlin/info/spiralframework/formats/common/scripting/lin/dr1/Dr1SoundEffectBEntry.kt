package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

public class Dr1SoundEffectBEntry(override val rawArguments: IntArray) : MutableLinEntry {
    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    public constructor(arg1: Int, arg2: Int) : this(intArrayOf(arg1, arg2))

    override val opcode: Int
        get() = 0x0B

//    val sfxID: Int
//        get() =

    public var arg1: Int
        get() = get(0)
        set(value) = set(0, value)

    public var arg2: Int
        get() = get(1)
        set(value) = set(1, value)
}