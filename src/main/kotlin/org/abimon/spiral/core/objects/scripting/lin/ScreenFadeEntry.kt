package org.abimon.spiral.core.objects.scripting.lin

data class ScreenFadeEntry(val fadeIn: Boolean, val colour: Int, val frameDuration: Int): LinScript {
    constructor(opCode: Int, args: IntArray): this(args[0] == 0, args[1], args[2])

    override val opCode: Int = 0x2A
    override val rawArguments: IntArray = intArrayOf(if(fadeIn) 0 else 1, colour, frameDuration)
}