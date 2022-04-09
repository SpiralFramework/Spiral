package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.formats.common.scripting.osl.RawNumberValue

public class Dr1BgmEntry(override val rawArguments: IntArray) : MutableLinEntry {
    public companion object {
        public const val BGM_CLEAR: Int = 0xFF
    }

    public constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)

    override val opcode: Int
        get() = 0x09

    public var bgmID: Int
        get() = get(0)
        set(value) = set(0, value)

    public var volume: Int
        get() = get(1)
        set(value) = set(1, value)

    override fun LinTranspiler.transpileArguments(builder: StringBuilder) {
        with(builder) {
            val bgmName = game?.linBgmNames
                ?.getOrNull(bgmID)
                ?.lowercase()
                ?.replace(' ', '_')
                ?.replace(LinTranspiler.ILLEGAL_VARIABLE_NAME_CHARACTER_REGEX, "")

            if (bgmName != null) {
                val bgmVariable = "bgm_$bgmName"
                if (bgmVariable !in variables)
                    variables[bgmVariable] = RawNumberValue(bgmID)

                append('$')
                append(bgmVariable)
                append(", ")
            } else if (bgmID == BGM_CLEAR) {
                val bgmVariable = "bgm_clear"
                if (bgmVariable !in variables)
                    variables[bgmVariable] = RawNumberValue(BGM_CLEAR)

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