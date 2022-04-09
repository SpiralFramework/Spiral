package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

public class Dr1SetStudentReportInfoEntry(override val rawArguments: IntArray) : MutableLinEntry {
    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    public constructor(characterID: Int, arg2: Int, state: Int) : this(intArrayOf(characterID, arg2, state))

    override val opcode: Int
        get() = 0x10

    public var characterID: Int
        get() = get(0)
        set(value) = set(0, value)

    public var arg2: Int
        get() = get(1)
        set(value) = set(1, value)

    public var state: Int
        get() = get(2)
        set(value) = set(2, value)
}