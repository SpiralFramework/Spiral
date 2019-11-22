package info.spiralframework.formats.common.data

import info.spiralframework.base.common.text.toIntBaseN
import info.spiralframework.formats.common.OpcodeHashMap
import info.spiralframework.formats.common.OpcodeMap
import info.spiralframework.formats.common.OpcodeMutableMap
import info.spiralframework.formats.common.data.json.JsonOpcode
import info.spiralframework.formats.common.games.DrGame

data class FlagCheckDetails(val flagGroupLength: Int, val endFlagCheckOpcode: Int)
data class ScriptOpcode<out T>(val opcode: Int, val argumentCount: Int, val names: Array<String>?, val flagCheckDetails: FlagCheckDetails?, val entryConstructor: (Int, IntArray) -> T) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ScriptOpcode<*>) return false

        if (opcode != other.opcode) return false
        if (argumentCount != other.argumentCount) return false
        if (names != null) {
            if (other.names == null) return false
            if (!names.contentEquals(other.names)) return false
        } else if (other.names != null) return false
        if (flagCheckDetails != other.flagCheckDetails) return false
        if (entryConstructor != other.entryConstructor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = opcode
        result = 31 * result + argumentCount
        result = 31 * result + (names?.contentHashCode() ?: 0)
        result = 31 * result + flagCheckDetails.hashCode()
        result = 31 * result + entryConstructor.hashCode()
        return result
    }

}

class ScriptOpcodeBuilder<T> {
    var argumentCount: Int = -1
    var names: Array<String>? = null
    var flagCheckDetails: FlagCheckDetails? = null
    lateinit var entryConstructor: (Int, IntArray) -> T
}

class ScriptOpcodeListBuilder<T> {
    val opcodes: OpcodeMutableMap<T> = OpcodeHashMap()

    fun opcode(opcode: Int, init: ScriptOpcodeBuilder<T>.() -> Unit) {
        val builder = ScriptOpcodeBuilder<T>()
        builder.init()
        opcodes[opcode] = ScriptOpcode(opcode, builder.argumentCount, builder.names, builder.flagCheckDetails, builder.entryConstructor)
    }

    fun DrGame.ScriptOpcodeFactory<T>.opcode(opcode: Int, argumentCount: Int) = opcode(opcode, argumentCount, this::entryFor)
    fun DrGame.ScriptOpcodeFactory<T>.opcode(opcode: Int, argumentCount: Int, flagCheckDetails: FlagCheckDetails?) = opcode(opcode, argumentCount, flagCheckDetails, this::entryFor)
    fun DrGame.ScriptOpcodeFactory<T>.opcode(opcode: Int, argumentCount: Int, name: String) = opcode(opcode, argumentCount, name, this::entryFor)
    fun DrGame.ScriptOpcodeFactory<T>.opcode(opcode: Int, argumentCount: Int, names: Array<String>?) = opcode(opcode, argumentCount, names, this::entryFor)
    fun DrGame.ScriptOpcodeFactory<T>.opcode(opcode: Int, argumentCount: Int, name: String, flagCheckDetails: FlagCheckDetails?) = opcode(opcode, argumentCount, name, flagCheckDetails, this::entryFor)
    fun DrGame.ScriptOpcodeFactory<T>.opcode(opcode: Int, argumentCount: Int, names: Array<String>?, flagCheckDetails: FlagCheckDetails?) = opcode(opcode, argumentCount, names, flagCheckDetails, this::entryFor)

    fun opcode(opcode: Int, argumentCount: Int, entryConstructor: (Int, IntArray) -> T) = opcode(opcode, argumentCount, null, null, entryConstructor)
    fun opcode(opcode: Int, argumentCount: Int, flagCheckDetails: FlagCheckDetails?, entryConstructor: (Int, IntArray) -> T) = opcode(opcode, argumentCount, null, flagCheckDetails, entryConstructor)
    fun opcode(opcode: Int, argumentCount: Int, name: String, entryConstructor: (Int, IntArray) -> T) = opcode(opcode, argumentCount, arrayOf(name), null, entryConstructor)
    fun opcode(opcode: Int, argumentCount: Int, name: String, flagCheckDetails: FlagCheckDetails?, entryConstructor: (Int, IntArray) -> T) = opcode(opcode, argumentCount, arrayOf(name), flagCheckDetails, entryConstructor)
    fun opcode(opcode: Int, argumentCount: Int, names: Array<String>?, entryConstructor: (Int, IntArray) -> T) = opcode(opcode, argumentCount, names, null, entryConstructor)
    fun opcode(opcode: Int, argumentCount: Int, names: Array<String>?, flagCheckDetails: FlagCheckDetails?, entryConstructor: (Int, IntArray) -> T) {
        opcodes[opcode] = ScriptOpcode(opcode, argumentCount, names, flagCheckDetails, entryConstructor)
    }

    fun DrGame.ScriptOpcodeFactory<T>.flagCheck(opcode: Int, name: String, flagGroupLength: Int, endFlagCheckOpcode: Int) = flagCheck(opcode, arrayOf(name), flagGroupLength, endFlagCheckOpcode, this::entryFor)
    fun DrGame.ScriptOpcodeFactory<T>.flagCheck(opcode: Int, names: Array<String>?, flagGroupLength: Int, endFlagCheckOpcode: Int) = flagCheck(opcode, names, flagGroupLength, endFlagCheckOpcode, this::entryFor)

    fun flagCheck(opcode: Int, name: String, flagGroupLength: Int, endFlagCheckOpcode: Int, entryConstructor: (Int, IntArray) -> T) = flagCheck(opcode, arrayOf(name), flagGroupLength, endFlagCheckOpcode, entryConstructor)
    fun flagCheck(opcode: Int, names: Array<String>?, flagGroupLength: Int, endFlagCheckOpcode: Int, entryConstructor: (Int, IntArray) -> T) {
        opcodes[opcode] = ScriptOpcode(opcode, -1, names, FlagCheckDetails(flagGroupLength, endFlagCheckOpcode), entryConstructor)
    }

    fun DrGame.ScriptOpcodeFactory<T>.fromList(list: List<JsonOpcode>) = list.forEach { entry ->
        opcode(
                entry.opcode.toIntBaseN(),
                argumentCount = entry.argCount,
                names = entry.names ?: entry.name?.let { arrayOf(it) }
        )
    }
}

inline fun <reified T> buildScriptOpcodes(init: ScriptOpcodeListBuilder<T>.() -> Unit): OpcodeMap<T> {
    val builder = ScriptOpcodeListBuilder<T>()
    builder.init()
    return builder.opcodes
}