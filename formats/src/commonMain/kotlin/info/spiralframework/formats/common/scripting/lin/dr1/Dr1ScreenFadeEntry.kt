package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry

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

    override fun format(): String = "Screen Fade|$fadeMethod, $colour, $duration"
}