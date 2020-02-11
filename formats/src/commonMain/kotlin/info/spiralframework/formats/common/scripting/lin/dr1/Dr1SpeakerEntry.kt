package info.spiralframework.formats.common.scripting.lin.dr1

import info.spiralframework.formats.common.scripting.lin.MutableLinEntry
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.formats.common.scripting.osl.NumberValue

inline class Dr1SpeakerEntry(override val rawArguments: IntArray): MutableLinEntry {
    constructor(opcode: Int, rawArguments: IntArray) : this(rawArguments)
    constructor(characterID: Int): this(intArrayOf(characterID))

    override val opcode: Int
        get() = 0x21

    var characterID: Int
        get() = get(0)
        set(value) = set(0, value)

    @ExperimentalUnsignedTypes
    override fun LinTranspiler.transpileArguments(builder: StringBuilder) {
        val speakerName = game?.linCharacterIDs
                ?.get(characterID)
                ?.toLowerCase()
                ?.replace(' ', '_')
                ?.replace(LinTranspiler.ILLEGAL_VARIABLE_NAME_CHARACTER_REGEX, "")
        if (speakerName?.isNotBlank() == true) {
            val speakerVariable = "speaker_$speakerName"
            if (speakerVariable !in variables) {
                variables[speakerVariable] = NumberValue(characterID)
            }

            builder.append('$')
            builder.append(speakerVariable)
        } else {
            builder.append(characterID)
        }
    }
}