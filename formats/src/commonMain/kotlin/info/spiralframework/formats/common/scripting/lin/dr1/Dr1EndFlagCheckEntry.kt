package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.LinEntry

public class Dr1EndFlagCheckEntry(override val rawArguments: IntArray): LinEntry {
    public constructor(): this(LinEntry.EMPTY_ARGUMENT_ARRAY)
    public constructor(opcode: Int, rawArguments: IntArray) : this()

    override val opcode: Int
        get() = 0x3C
}