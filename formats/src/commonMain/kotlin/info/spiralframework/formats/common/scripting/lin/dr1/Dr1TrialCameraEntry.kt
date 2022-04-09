package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

public class Dr1TrialCameraEntry(override val rawArguments: IntArray) : MutableLinEntry {
    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    public constructor(characterID: Int, cameraID: Int): this(intArrayOf(characterID, (cameraID shr 8) and 0xFF, cameraID and 0xFF))

    override val opcode: Int
        get() = 0x15

    public var characterID: Int
        get() = get(0)
        set(value) = set(0, value)

    public var cameraID: Int
        get() = getInt16BE(1)
        set(value) = setInt16BE(1, value)
}