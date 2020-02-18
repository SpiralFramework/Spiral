package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.base.common.freeze
import info.spiralframework.formats.common.scripting.lin.MutableLinEntry
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.formats.common.scripting.osl.NumberValue

inline class Dr1ScreenFlashEntry(override val rawArguments: IntArray) : MutableLinEntry {
    companion object {
        const val RGB_WHITE = 0xFFFFFF
        const val RGB_RED = 0xFF0000
        const val RGB_GREEN = 0x00FF00
        const val RGB_BLUE = 0x0000FF
        const val RGB_BLACK = 0x000000
    }

    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    constructor(red: Int, green: Int, blue: Int, fadeInDuration: Int, holdDuration: Int, fadeOutDuration: Int, opacity: Int) : this(intArrayOf(red, green, blue, fadeInDuration, holdDuration, fadeOutDuration, opacity))

    override val opcode: Int
        get() = 0x1F

    var red: Int
        get() = get(0)
        set(value) = set(0, value)

    var green: Int
        get() = get(1)
        set(value) = set(1, value)

    var blue: Int
        get() = get(2)
        set(value) = set(2, value)

    var rgb: Int
        get() = getInt24BE(0)
        set(value) = setInt24BE(0, value)

    var fadeInDuration: Int
        get() = get(3)
        set(value) = set(3, value)

    var holdDuration: Int
        get() = get(4)
        set(value) = set(4, value)

    var fadeOutDuration: Int
        get() = get(5)
        set(value) = set(5, value)

    var opacity: Int
        get() = get(6)
        set(value) = set(6, value)

    @ExperimentalUnsignedTypes
    override fun LinTranspiler.transpileArguments(builder: StringBuilder) {
        with(builder) {
            freeze(rgb) { rgb ->
                append("rgb(")
                if (rgb == RGB_WHITE) {
                    val rgbVariable = "rgb_colour_white"
                    if (rgbVariable !in variables)
                        variables[rgbVariable] = NumberValue(RGB_WHITE)

                    append('$')
                    append(rgbVariable)
                } else if (rgb == RGB_BLACK) {
                    val rgbVariable = "rgb_colour_black"
                    if (rgbVariable !in variables)
                        variables[rgbVariable] = NumberValue(RGB_BLACK)

                    append('$')
                    append(rgbVariable)
                } else if (rgb == RGB_RED) {
                    val rgbVariable = "rgb_colour_red"
                    if (rgbVariable !in variables)
                        variables[rgbVariable] = NumberValue(RGB_RED)

                    append('$')
                    append(rgbVariable)
                } else if (rgb == RGB_GREEN) {
                    val rgbVariable = "rgb_colour_green"
                    if (rgbVariable !in variables)
                        variables[rgbVariable] = NumberValue(RGB_GREEN)

                    append('$')
                    append(rgbVariable)
                } else if (rgb == RGB_BLUE) {
                    val rgbVariable = "rgb_colour_blue"
                    if (rgbVariable !in variables)
                        variables[rgbVariable] = NumberValue(RGB_BLUE)

                    append('$')
                    append(rgbVariable)
                } else {
                    append(rgb)
                }
                append("), ")
                append(fadeInDuration)
                append(", ")
                append(holdDuration)
                append(", ")
                append(fadeOutDuration)
                append(", ")
                append(opacity)
            }
        }
    }
}