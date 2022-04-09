package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.formats.common.scripting.osl.RawNumberValue

public class Dr1AnimationEntry(override val rawArguments: IntArray) : MutableLinEntry {
    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    public constructor(id: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: Int, frame: Int) : this(intArrayOf(id shr 8, id and 0xFF, arg3, arg4, arg5, arg6, arg7, frame))

    override val opcode: Int
        get() = 0x06

    public var id: Int
        get() = getInt16BE(0)
        set(value) = setInt16BE(0, value)
    public var arg3: Int
        get() = rawArguments[2]
        set(value) = set(2, value)
    public var arg4: Int
        get() = rawArguments[3]
        set(value) = set(3, value)
    public var arg5: Int
        get() = rawArguments[4]
        set(value) = set(4, value)
    public var arg6: Int
        get() = rawArguments[5]
        set(value) = set(5, value)
    public var arg7: Int
        get() = rawArguments[6]
        set(value) = set(6, value)
    public var frame: Int
        get() = rawArguments[7]
        set(value) = set(7, value)

    override fun LinTranspiler.transpile(indent: Int) {
        addOutput {
            repeat(indent) { append('\t') }
            if (id >= 3000) {
                if (frame == 1) {
                    append("ShowCutin(")
                    append(id - 3000)
                    append(")")

                    return@addOutput
                } else if (frame == 2) {
                    append("HideCutin(")
                    append(id - 3000)
                    append(")")

                    return@addOutput
                }
            } else if (id >= 2000) {
                val itemName = game?.linItemNames
                    ?.getOrNull(id - 2000)
                    ?.lowercase()
                    ?.replace(' ', '_')
                    ?.replace(LinTranspiler.ILLEGAL_VARIABLE_NAME_CHARACTER_REGEX, "")

                if (frame == 1) {
                    append("ShowItem(")
                    if (itemName != null) {
                        val itemVariable = "item_$itemName"
                        if (itemVariable !in variables)
                            variables[itemVariable] = RawNumberValue(id - 2000)

                        append('$')
                        append(itemVariable)
                    } else {
                        append(id)
                    }
                    append(")")

                    return@addOutput
                } else if (frame == 2) {
                    append("HideItem(")
                    if (itemName != null) {
                        val itemVariable = "item_$itemName"
                        if (itemVariable !in variables)
                            variables[itemVariable] = RawNumberValue(id - 2000)

                        append('$')
                        append(itemVariable)
                    } else {
                        append(id)
                    }
                    append(")")

                    return@addOutput
                }
            }

            append(nameFor(this@Dr1AnimationEntry))
            append('|')
            transpileArguments(this)
        }

    }

    override fun LinTranspiler.transpileArguments(builder: StringBuilder) {
        with(builder) {
            append("int16BE(")
            append(id)
            append("), ")
            append(arg3)
            append(", ")
            append(arg4)
            append(", ")
            append(arg5)
            append(", ")
            append(arg6)
            append(", ")
            append(arg7)
            append(", ")
            append(frame)
        }
    }
}