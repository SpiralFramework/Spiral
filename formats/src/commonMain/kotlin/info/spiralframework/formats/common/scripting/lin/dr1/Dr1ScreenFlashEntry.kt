package info.spiralframework.formats.common.scripting.lin.dr1

import dev.brella.kornea.toolkit.common.freeze
import info.spiralframework.formats.common.scripting.lin.MutableLinEntry
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.formats.common.scripting.osl.RawNumberValue

public class Dr1ScreenFlashEntry(override val rawArguments: IntArray) : MutableLinEntry {
    public companion object {
        public const val RGB_WHITE: Int = 0xFFFFFF
        public const val RGB_RED: Int = 0xFF0000
        public const val RGB_GREEN: Int = 0x00FF00
        public const val RGB_BLUE: Int = 0x0000FF
        public const val RGB_BLACK: Int = 0x000000
    }

    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    public constructor(red: Int, green: Int, blue: Int, fadeInDuration: Int, holdDuration: Int, fadeOutDuration: Int, opacity: Int) : this(intArrayOf(red, green, blue, fadeInDuration, holdDuration, fadeOutDuration, opacity))

    override val opcode: Int
        get() = 0x1F

    public var red: Int
        get() = get(0)
        set(value) = set(0, value)

    public var green: Int
        get() = get(1)
        set(value) = set(1, value)

    public var blue: Int
        get() = get(2)
        set(value) = set(2, value)

    public var rgb: Int
        get() = getInt24BE(0)
        set(value) = setInt24BE(0, value)

    public var fadeInDuration: Int
        get() = get(3)
        set(value) = set(3, value)

    public var holdDuration: Int
        get() = get(4)
        set(value) = set(4, value)

    public var fadeOutDuration: Int
        get() = get(5)
        set(value) = set(5, value)

    public var opacity: Int
        get() = get(6)
        set(value) = set(6, value)

    override fun LinTranspiler.transpileArguments(builder: StringBuilder) {
        with(builder) {
            freeze(rgb) { rgb ->
                append("rgb(")
                if (rgb == RGB_WHITE) {
                    val rgbVariable = "rgb_colour_white"
                    if (rgbVariable !in variables)
                        variables[rgbVariable] = RawNumberValue(RGB_WHITE)

                    append('$')
                    append(rgbVariable)
                } else if (rgb == RGB_BLACK) {
                    val rgbVariable = "rgb_colour_black"
                    if (rgbVariable !in variables)
                        variables[rgbVariable] = RawNumberValue(RGB_BLACK)

                    append('$')
                    append(rgbVariable)
                } else if (rgb == RGB_RED) {
                    val rgbVariable = "rgb_colour_red"
                    if (rgbVariable !in variables)
                        variables[rgbVariable] = RawNumberValue(RGB_RED)

                    append('$')
                    append(rgbVariable)
                } else if (rgb == RGB_GREEN) {
                    val rgbVariable = "rgb_colour_green"
                    if (rgbVariable !in variables)
                        variables[rgbVariable] = RawNumberValue(RGB_GREEN)

                    append('$')
                    append(rgbVariable)
                } else if (rgb == RGB_BLUE) {
                    val rgbVariable = "rgb_colour_blue"
                    if (rgbVariable !in variables)
                        variables[rgbVariable] = RawNumberValue(RGB_BLUE)

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