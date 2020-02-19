package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.formats.common.scripting.osl.RawNumberValue

inline class Dr1LoadMapEntry(override val rawArguments: IntArray) : MutableLinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    constructor(room: Int, state: Int, arg3: Int): this(intArrayOf(room, state, arg3))

    override val opcode: Int
        get() = 0x15

    var room: Int
        get() = get(0)
        set(value) = set(0, value)

    var state: Int
        get() = get(1)
        set(value) = set(1, value)

    var arg3: Int
        get() = get(2)
        set(value) = set(2, value)

    @ExperimentalUnsignedTypes
    override fun LinTranspiler.transpileArguments(builder: StringBuilder) {
        with(builder) {
            val mapName = game?.linMapNames
                    ?.getOrNull(room)
                    ?.toLowerCase()
                    ?.replace(' ', '_')
                    ?.replace(LinTranspiler.ILLEGAL_VARIABLE_NAME_CHARACTER_REGEX, "")

            if (mapName != null) {
                val mapVariable = "map_$mapName"
                if (mapVariable !in variables)
                    variables[mapVariable] = RawNumberValue(room)

                append('$')
                append(mapVariable)
                append(", ")
            } else {
                append(room)
                append(", ")
            }

            append(state)
            append(", ")
            append(arg3)
        }
    }
}