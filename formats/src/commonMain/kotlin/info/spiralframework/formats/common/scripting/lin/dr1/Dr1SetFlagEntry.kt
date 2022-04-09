package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry
import info.spiralframework.formats.common.scripting.osl.FlagIDValue
import info.spiralframework.formats.common.scripting.osl.LinTranspiler

public class Dr1SetFlagEntry(override val rawArguments: IntArray) : MutableLinEntry {
    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    public constructor(flagGroup: Int, flagID: Int, state: Int) : this(intArrayOf(flagGroup, flagID, state))
    public constructor(flagGroup: Int, flagID: Int, enabled: Boolean) : this(intArrayOf(flagGroup, flagID, if (enabled) 1 else 0))

    override val opcode: Int
        get() = 0x26

    public var flagGroup: Int
        get() = get(0)
        set(value) = set(0, value)

    public var flagID: Int
        get() = get(1)
        set(value) = set(1, value)

    public var state: Int
        get() = get(2)
        set(value) = set(2, value)

    public var enabled: Boolean
        get() = get(2) > 0
        set(value) = set(2, if (value) 1 else 0)

    override fun LinTranspiler.transpile(indent: Int) {
        addOutput {
            repeat(indent) { append('\t') }
            if (state == 0 || state == 1) {
                if (state == 0) append("DisableFlag(")
                else append("EnableFlag(")

                val flagName = game?.getLinFlagName(flagGroup, flagID)
                    ?.lowercase()
                    ?.replace(' ', '_')
                    ?.replace(LinTranspiler.ILLEGAL_VARIABLE_NAME_CHARACTER_REGEX, "")

                if (flagName != null) {
                    val flagVariable = "flag_$flagName"
                    if (flagVariable !in variables)
                        variables[flagVariable] = FlagIDValue((flagGroup shl 8) or flagID)

                    append('$')
                    append(flagVariable)
                } else {
                    append("flagID(")
                    append(flagGroup)
                    append(", ")
                    append(flagID)
                    append(")")
                }

                append(')')
            } else {
                append(nameFor(this@Dr1SetFlagEntry))
                append('|')
                transpileArguments(this)
            }
        }
    }

    override fun LinTranspiler.transpileArguments(builder: StringBuilder) {
        val flagName = game?.getLinFlagName(flagGroup, flagID)
            ?.lowercase()
            ?.replace(' ', '_')
            ?.replace(LinTranspiler.ILLEGAL_VARIABLE_NAME_CHARACTER_REGEX, "")

        if (flagName != null) {
            val flagVariable = "flag_$flagName"
            if (flagVariable !in variables)
                variables[flagVariable] = FlagIDValue((flagGroup shl 8) or flagID)

            builder.append('$')
            builder.append(flagVariable)
            builder.append(", ")
        } else {
            builder.append("flagID(")
            builder.append(flagGroup)
            builder.append(", ")
            builder.append(flagID)
            builder.append("),")
        }

        builder.append(state)
    }
}