package org.abimon.spiral.core.objects.scripting.lin

data class SetStudentTitleEntry(val characterID: Int, val arg2: Int, val state: Int): LinScript {
    constructor(opCode: Int, args: IntArray): this(args[0], args[1], args[2])

    override val opCode: Int = 0x0F
    override val rawArguments: IntArray = intArrayOf(characterID, arg2, state)

    override fun format(): String = "Set Title|$characterID, $arg2, $state"
}