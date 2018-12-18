package org.abimon.spiral.core.objects.scripting.lin.udg

import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.LinTextScript

data class UDGTextEntry(override var text: String?, override var textID: Int): LinScript, LinTextScript {
    constructor(op: Int, args: IntArray): this(null, (args[0] shl 8) or args[1])

    override val opCode: Int = 0x01
    override val rawArguments: IntArray = intArrayOf(textID shr 8 and 0xFF, textID and 0xFF)
    override val writeBOM: Boolean = false
}