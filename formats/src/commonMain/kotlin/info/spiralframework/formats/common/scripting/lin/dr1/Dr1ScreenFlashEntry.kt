package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

inline class Dr1ScreenFlashEntry(override val rawArguments: IntArray) : MutableLinEntry {
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
        get() = (red and 255 shl 16) or (green and 255 shl 8) or (blue and 255 shl 0)
        set(value) {
            red = (value shr 16) and 0xFF
            green = (value shr 8) and 0xFF
            blue = (value shr 0) and 0xFF
        }

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
}