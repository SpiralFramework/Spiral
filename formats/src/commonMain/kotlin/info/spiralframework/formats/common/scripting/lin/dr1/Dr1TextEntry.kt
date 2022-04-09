package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry
import info.spiralframework.formats.common.scripting.osl.LinTranspiler

public class Dr1TextEntry(override val rawArguments: IntArray) : MutableLinEntry {
    public constructor(textID: Int) : this(intArrayOf((textID shr 8) and 0xFF, textID and 0xFF))
    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x02

    public var textID: Int
        get() = getInt16BE(0)
        set(value) = setInt16BE(0, value)

    override fun LinTranspiler.transpileArguments(builder: StringBuilder) {
        builder.append('"')
        builder.append(lin[textID].sanitise())
        builder.append('"')
    }
}