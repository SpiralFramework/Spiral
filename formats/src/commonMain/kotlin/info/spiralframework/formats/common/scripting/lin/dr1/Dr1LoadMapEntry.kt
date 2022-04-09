package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.formats.common.scripting.osl.RawNumberValue

public class Dr1LoadMapEntry(override val rawArguments: IntArray) : MutableLinEntry {
    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    public constructor(room: Int, state: Int, arg3: Int): this(intArrayOf(room, state, arg3))

    override val opcode: Int
        get() = 0x15

    public var room: Int
        get() = get(0)
        set(value) = set(0, value)

    public var state: Int
        get() = get(1)
        set(value) = set(1, value)

    public var arg3: Int
        get() = get(2)
        set(value) = set(2, value)

    override fun LinTranspiler.transpileArguments(builder: StringBuilder) {
        with(builder) {
            val mapName = game?.linMapNames
                ?.getOrNull(room)
                ?.lowercase()
                ?.replace(' ', '_')
                ?.replace(LinTranspiler.ILLEGAL_VARIABLE_NAME_CHARACTER_REGEX, "")

            if (mapName != null) {
                var mapVariable = "map_$mapName"
                if (mapVariable !in variables)
                    variables[mapVariable] = RawNumberValue(room)
                else if ((variables.getValue(mapVariable) as? RawNumberValue)?.number != room) {
                    val old = variables.getValue(mapVariable) as RawNumberValue
                    variables.remove(mapVariable)
                    variables["map_${mapName}_${old.number}"] = old
                    variableMappings["\\B\\$${mapVariable}\\b".toRegex()] = "\\\$map_${mapName}_${old.number}"
                    mapVariable = "map_${mapName}_${room}"
                    variables[mapVariable] = RawNumberValue(room)
                }

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