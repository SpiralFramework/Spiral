package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.formats.common.scripting.osl.NumberValue

inline class Dr1ScreenFadeEntry(override val rawArguments: IntArray) : MutableLinEntry {
    companion object {
        const val FADE_IN_METHOD = 0
        const val FADE_OUT_METHOD = 1

        const val FADE_COLOUR_BLACK = 1
        const val FADE_COLOUR_WHITE = 2
        const val FADE_COLOUR_RED = 3
    }

    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    constructor(fadeMethod: Int, colour: Int, duration: Int): this(intArrayOf(fadeMethod, colour, duration))
    constructor(fadeIn: Boolean, colour: Int, duration: Int): this(intArrayOf(if (fadeIn) FADE_IN_METHOD else FADE_OUT_METHOD, colour, duration))

    override val opcode: Int
        get() = 0x22

    var fadeMethod: Int
        get() = get(0)
        set(value) = set(0, value)

    var fadeIn: Boolean
        get() = get(0) == FADE_IN_METHOD
        set(value) = set(0, if (value) FADE_IN_METHOD else FADE_OUT_METHOD)

    var colour: Int
        get() = get(1)
        set(value) = set(1, value)

    /** Duration in frames */
    var duration: Int
        get() = get(2)
        set(value) = set(2, value)

    @ExperimentalUnsignedTypes
    override fun LinTranspiler.transpile(indent: Int) {
        addOutput {
            repeat(indent) { append('\t') }
            if (colour == FADE_COLOUR_BLACK) {
                if (fadeIn) {
                    append("FadeInFromBlack(")
                    append(duration)
                    append(")")
                } else {
                    append("FadeOutToBlack(")
                    append(duration)
                    append(")")
                }
            } else {
                append(nameFor(this@Dr1ScreenFadeEntry))
                append('|')
                transpileArguments(this)
            }
        }
    }

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
                FADE_COLOUR_BLACK -> "screen_fade_colour_black"
                FADE_COLOUR_WHITE -> "screen_fade_colour_white"
                FADE_COLOUR_RED -> "screen_fade_colour_red"
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