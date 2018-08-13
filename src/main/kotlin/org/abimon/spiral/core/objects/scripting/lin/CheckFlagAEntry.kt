package org.abimon.spiral.core.objects.scripting.lin

import org.abimon.spiral.core.utils.and

class CheckFlagAEntry: LinScript {
    val flags: Array<Triple<Int, Int, Int>>
    val flagOperations: IntArray
    val failLabel: Int

    override val opCode: Int = 0x35
    override val rawArguments: IntArray

    constructor(opCode: Int, args: IntArray) {
        val potentialTuples = args.size - 2

        val tuples: MutableList<Triple<Int, Int, Int>> = ArrayList()

        for (i in 0 until potentialTuples / 4)
            tuples.add(args[i * 4] to args[i * 4 + 1] and ((args[i * 4 + 2] shl 8) or args[i * 4 + 3]))

        flags = tuples.toTypedArray()
        flagOperations = IntArray(potentialTuples % 4) { index -> args[potentialTuples / 4 + index] }
        failLabel = (args[args.size - 2] shl 8) or args[args.size - 1]

        rawArguments = args
    }

    constructor(flags: Array<Triple<Int, Int, Int>>, flagOperations: IntArray, failLabel: Int) {
        this.flags = flags
        this.flagOperations = flagOperations
        this.failLabel = failLabel

        val rawArgs: MutableList<Int> = ArrayList()

        this.flags.forEach { (group, id, state) ->
            rawArgs.add(group % 256)
            rawArgs.add(id % 256)
            rawArgs.add(state shr 8)
            rawArgs.add(state % 256)
        }

        flagOperations.forEach { flagOp -> rawArgs.add(flagOp) }

        rawArgs.add(failLabel shr 8)
        rawArgs.add(failLabel % 256)

        rawArguments = rawArgs.toIntArray()
    }

    override fun format(): String = "Check Flag A|${rawArguments.joinToString()}"
}