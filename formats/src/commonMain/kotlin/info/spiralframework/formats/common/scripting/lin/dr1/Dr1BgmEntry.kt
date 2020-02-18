package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.formats.common.scripting.osl.NumberValue

inline class Dr1BgmEntry(override val rawArguments: IntArray) : MutableLinEntry {
    companion object {
        const val BGM_CLEAR = 0xFF
    }

    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x09

    var bgmID: Int
        get() = get(0)
        set(value) = set(0, value)

    var volume: Int
        get() = get(1)
        set(value) = set(1, value)

    @ExperimentalUnsignedTypes
    override fun LinTranspiler.transpileArguments(builder: StringBuilder) {
        with(builder) {
            val bgmName = game?.linBgmNames
                    ?.getOrNull(bgmID)
                    ?.toLowerCase()
                    ?.replace(' ', '_')
                    ?.replace(LinTranspiler.ILLEGAL_VARIABLE_NAME_CHARACTER_REGEX, "")

            if (bgmName != null) {
                val bgmVariable = "bgm_$bgmName"
                if (bgmVariable !in variables)
                    variables[bgmVariable] = NumberValue(bgmID)

                append('$')
                append(bgmVariable)
                append(", ")
            } else if (bgmID == BGM_CLEAR) {
                val bgmVariable = "bgm_clear"
                if (bgmVariable !in variables)
                    variables[bgmVariable] = NumberValue(BGM_CLEAR)

                append('$')
                append(bgmVariable)
                append(", ")
            } else {
                append(bgmID)
                append(", ")
            }

            append(volume)
            append(", ")

            append(rawArguments[2])
        }
    }
}