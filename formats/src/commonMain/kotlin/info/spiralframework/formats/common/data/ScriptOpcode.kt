package info.spiralframework.formats.common.data

import info.spiralframework.base.common.text.toIntBaseN
import info.spiralframework.formats.common.OpcodeCommandTypeMap
import info.spiralframework.formats.common.OpcodeHashMap
import info.spiralframework.formats.common.OpcodeMap
import info.spiralframework.formats.common.OpcodeMutableMap
import info.spiralframework.formats.common.data.json.JsonOpcode
import info.spiralframework.formats.common.games.DrGame

data class FlagCheckDetails(val flagGroupLength: Int, val endFlagCheckOpcode: Int)
data class ScriptOpcode<in P, out T>(val opcode: Int, val argumentCount: Int, val names: Array<out String>?, val flagCheckDetails: FlagCheckDetails?, val entryConstructor: (Int, P) -> T) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ScriptOpcode<*, *>) return false

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

class ScriptOpcodeBuilder<P, T> {
    var argumentCount: Int = -1
    var names: Array<out String>? = null
    var flagCheckDetails: FlagCheckDetails? = null
    lateinit var entryConstructor: (Int, P) -> T
}

class ScriptOpcodeListBuilder<P, T> {
    val opcodes: OpcodeMutableMap<P, T> = OpcodeHashMap()

    fun opcode(opcode: Int, init: ScriptOpcodeBuilder<P, T>.() -> Unit) {
        val builder = ScriptOpcodeBuilder<P, T>()
        builder.init()
        opcodes[opcode] = ScriptOpcode(opcode, builder.argumentCount, builder.names, builder.flagCheckDetails, builder.entryConstructor)
    }

    fun DrGame.ScriptOpcodeFactory<P, T>.opcode(opcode: Int, argumentCount: Int) = opcode(opcode, argumentCount, this::entryFor)
    fun DrGame.ScriptOpcodeFactory<P, T>.opcode(opcode: Int, argumentCount: Int, flagCheckDetails: FlagCheckDetails?) = opcode(opcode, argumentCount, flagCheckDetails, this::entryFor)
    fun DrGame.ScriptOpcodeFactory<P, T>.opcode(opcode: Int, argumentCount: Int, name: String) = opcode(opcode, argumentCount, name, this::entryFor)
    fun DrGame.ScriptOpcodeFactory<P, T>.opcode(opcode: Int, argumentCount: Int, names: Array<out String>?) = opcode(opcode, argumentCount, names, this::entryFor)
    fun DrGame.ScriptOpcodeFactory<P, T>.opcode(opcode: Int, argumentCount: Int, name: String, flagCheckDetails: FlagCheckDetails?) = opcode(opcode, argumentCount, name, flagCheckDetails, this::entryFor)
    fun DrGame.ScriptOpcodeFactory<P, T>.opcode(opcode: Int, argumentCount: Int, names: Array<out String>?, flagCheckDetails: FlagCheckDetails?) = opcode(opcode, argumentCount, names, flagCheckDetails, this::entryFor)

    fun opcode(opcode: Int, argumentCount: Int, entryConstructor: (Int, P) -> T) = opcode(opcode, argumentCount, null, null, entryConstructor)
    fun opcode(opcode: Int, argumentCount: Int, flagCheckDetails: FlagCheckDetails?, entryConstructor: (Int, P) -> T) = opcode(opcode, argumentCount, null, flagCheckDetails, entryConstructor)
    fun opcode(opcode: Int, argumentCount: Int, name: String, entryConstructor: (Int, P) -> T) = opcode(opcode, argumentCount, arrayOf(name), null, entryConstructor)
    fun opcode(opcode: Int, argumentCount: Int, name: String, flagCheckDetails: FlagCheckDetails?, entryConstructor: (Int, P) -> T) = opcode(opcode, argumentCount, arrayOf(name), flagCheckDetails, entryConstructor)
    fun opcode(opcode: Int, argumentCount: Int, names: Array<out String>?, entryConstructor: (Int, P) -> T) = opcode(opcode, argumentCount, names, null, entryConstructor)
    fun opcode(opcode: Int, argumentCount: Int, names: Array<out String>?, flagCheckDetails: FlagCheckDetails?, entryConstructor: (Int, P) -> T) {
        opcodes[opcode] = ScriptOpcode(opcode, argumentCount, names, flagCheckDetails, entryConstructor)
    }

    fun DrGame.ScriptOpcodeFactory<P, T>.flagCheck(opcode: Int, name: String, flagGroupLength: Int, endFlagCheckOpcode: Int) = flagCheck(opcode, arrayOf(name), flagGroupLength, endFlagCheckOpcode, this::entryFor)
    fun DrGame.ScriptOpcodeFactory<P, T>.flagCheck(opcode: Int, names: Array<out String>?, flagGroupLength: Int, endFlagCheckOpcode: Int) = flagCheck(opcode, names, flagGroupLength, endFlagCheckOpcode, this::entryFor)

    fun flagCheck(opcode: Int, name: String, flagGroupLength: Int, endFlagCheckOpcode: Int, entryConstructor: (Int, P) -> T) = flagCheck(opcode, arrayOf(name), flagGroupLength, endFlagCheckOpcode, entryConstructor)
    fun flagCheck(opcode: Int, names: Array<out String>?, flagGroupLength: Int, endFlagCheckOpcode: Int, entryConstructor: (Int, P) -> T) {
        opcodes[opcode] = ScriptOpcode(opcode, -1, names, FlagCheckDetails(flagGroupLength, endFlagCheckOpcode), entryConstructor)
    }

    fun DrGame.ScriptOpcodeFactory<P, T>.fromList(list: List<JsonOpcode>) = list.forEach { entry ->
        opcode(
                entry.opcode.toIntBaseN(),
                argumentCount = entry.argCount,
                names = entry.names ?: entry.name?.let { arrayOf(it) }
        )
    }
}

class OpcodeCommandTypeBuilder {
    val opcodeCommandTypes: MutableMap<Int, (Int) -> EnumWordScriptCommand> = HashMap()

    fun opcode(opcode: Int, vararg types: EnumWordScriptCommand) {
        opcodeCommandTypes[opcode] = types::get
    }

    fun opcode(opcode: Int, vararg types: Int) {
        opcodeCommandTypes[opcode] = types.map(EnumWordScriptCommand.Companion::invoke)::get
    }

    fun opcode(opcode: Int, func: (Int) -> EnumWordScriptCommand) {
        opcodeCommandTypes[opcode] = func
    }
}

inline fun <reified P, reified T> buildScriptOpcodes(init: ScriptOpcodeListBuilder<P, T>.() -> Unit): OpcodeMap<P, T> {
    val builder = ScriptOpcodeListBuilder<P, T>()
    builder.init()
    return builder.opcodes
}

fun buildOpcodeCommandTypes(init: OpcodeCommandTypeBuilder.() -> Unit): OpcodeCommandTypeMap<EnumWordScriptCommand> {
    val builder = OpcodeCommandTypeBuilder()
    builder.init()
    return builder.opcodeCommandTypes
}