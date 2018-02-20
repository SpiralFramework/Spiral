package org.abimon.spiral.core.objects.scripting.lin

data class CheckCharacterEntry(val characterID: Int): LinScript {
    constructor(opCode: Int, args: IntArray): this(args[0])

    override val opCode: Int = 0x27
    override val rawArguments: IntArray = intArrayOf(characterID)
}