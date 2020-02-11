package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.formats.common.scripting.osl.NumberValue

inline class Dr1ScreenFadeEntry(override val rawArguments: IntArray) : MutableLinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    constructor(fadeMethod: Int, colour: Int, duration: Int): this(intArrayOf(fadeMethod, colour, duration))
    constructor(fadeIn: Boolean, colour: Int, duration: Int): this(intArrayOf(if (fadeIn) 0 else 1, colour, duration))

    override val opcode: Int
        get() = 0x22

    var fadeMethod: Int
        get() = get(0)
        set(value) = set(0, value)

    var fadeIn: Boolean
        get() = get(0) == 0
        set(value) = set(0, if (value) 0 else 1)

    var colour: Int
        get() = get(1)
        set(value) = set(1, value)

    /** Duration in frames */
    var duration: Int
        get() = get(2)
        set(value) = set(2, value)

    @ExperimentalUnsignedTypes
    override fun LinTranspiler.transpileArguments(builder: StringBuilder) {
        with(builder) {
            if (fadeMethod == 0 || fadeMethod == 1) {
                val fadeVariable = if (fadeIn) "screen_fade_in" else "screen_fade_out"
                if (fadeVariable !in variables)
                    variables[fadeVariable] = NumberValue(fadeMethod)

                append('$')
                append(fadeVariable)
                append(", ")
            } else {
                append(fadeMethod)
                append(", ")
            }

            val colourVariable = when (colour) {
                1 -> "screen_fade_colour_black"
                2 -> "screen_fade_colour_white"
                3 -> "screen-fade_colour_red"
                else -> null
            }

            if (colourVariable != null) {
                if (colourVariable !in variables)
                    variables[colourVariable] = NumberValue(colour)

                append('$')
                append(colourVariable)
                append(", ")
            } else {
                append(colour)
                append(", ")
            }

            append(duration)
        }
    }
}