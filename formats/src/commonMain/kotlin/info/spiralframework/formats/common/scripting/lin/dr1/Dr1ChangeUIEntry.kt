package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.formats.common.scripting.osl.RawNumberValue

inline class Dr1ChangeUIEntry(override val rawArguments: IntArray) : MutableLinEntry {
    companion object {
        const val HUD = 3
        const val MAP_LOAD_ANIMATION = 14
        const val PRESENT_SELECTION = 19
    }

    constructor(opcode: Int, rawArguments: IntArray): this(rawArguments)
    constructor(element: Int, state: Int): this(intArrayOf(element, state))
    constructor(element: Int, enabled: Boolean): this(intArrayOf(element, if (enabled) 1 else 0))

    override val opcode: Int
        get() = 0x25

    var element: Int
        get() = rawArguments[0]
        set(value) = set(0, value)

    var state: Int
        get() = rawArguments[1]
        set(value) = set(1, value)

    override fun LinTranspiler.transpile(indent: Int) {
        addOutput {
            repeat(indent) { append('\t') }
            if (state == 0 || state == 1) {
                if (state == 0) append("DisableUI(")
                else append("EnableUI(")
                val itemName = game?.getNameOfLinUIElement(element)
                        ?.toLowerCase()
                        ?.replace(' ', '_')
                        ?.replace(LinTranspiler.ILLEGAL_VARIABLE_NAME_CHARACTER_REGEX, "")

                if (itemName != null) {
                    val uiVariable = "ui_$itemName"
                    if (uiVariable !in variables)
                        variables[uiVariable] = RawNumberValue(element)

                    append('$')
                    append(uiVariable)
                } else {
                    append(element)
                }

                append(')')
            } else {
                append(nameFor(this@Dr1ChangeUIEntry))
                append('|')
                transpileArguments(this)
            }
        }
    }

    override fun LinTranspiler.transpileArguments(builder: StringBuilder) {
        val itemName = game?.getNameOfLinUIElement(element)
                ?.toLowerCase()
                ?.replace(' ', '_')
                ?.replace(LinTranspiler.ILLEGAL_VARIABLE_NAME_CHARACTER_REGEX, "")

        if (itemName != null) {
            val uiVariable = "ui_$itemName"
            if (uiVariable !in variables)
                variables[uiVariable] = RawNumberValue(element)

            builder.append('$')
            builder.append(uiVariable)
            builder.append(", ")
        } else {
            builder.append(element)
            builder.append(", ")
        }

        builder.append(state)
    }
}