package org.abimon.spiral.core.objects.scripting.lin

import java.text.DecimalFormat

data class ScreenFadeEntry(val fadeIn: Boolean, val colour: Int, val frameDuration: Int): LinScript {
    constructor(opCode: Int, args: IntArray): this(args[0] == 0, args[1], args[2])

    override val opCode: Int = 0x22
    override val rawArguments: IntArray = intArrayOf(if(fadeIn) 0 else 1, colour, frameDuration)

    override fun format(): String = buildString {
        append("Fade")

        if (fadeIn)
            append(" in from ")
        else
            append(" out to ")

        when (colour) {
            0 -> append("black (special)")
            1 -> append("black")
            2 -> append("white")
            3 -> append("red")
            else -> append("{$colour}")
        }

        append(" for ${DecimalFormat("#.##").format(frameDuration / 60.0)} seconds")
    }
}