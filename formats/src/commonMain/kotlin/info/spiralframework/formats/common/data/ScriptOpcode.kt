package info.spiralframework.formats.common.data

import info.spiralframework.base.common.text.toIntBaseN
import info.spiralframework.formats.common.OpcodeCommandTypeMap
import info.spiralframework.formats.common.OpcodeHashMap
import info.spiralframework.formats.common.OpcodeMap
import info.spiralframework.formats.common.OpcodeMutableMap
import info.spiralframework.formats.common.data.json.JsonOpcode
import info.spiralframework.formats.common.games.DrGame

public data class FlagCheckDetails(val flagGroupLength: Int, val endFlagCheckOpcode: Int)
public data class ScriptOpcode<in P, out T>(val opcode: Int, val argumentCount: Int, val names: Array<out String>?, val flagCheckDetails: FlagCheckDetails?, val entryConstructor: (Int, P) -> T) {
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

public class ScriptOpcodeBuilder<P, T> {
    public var argumentCount: Int = -1
    public var names: Array<out String>? = null
    public var flagCheckDetails: FlagCheckDetails? = null
    public lateinit var entryConstructor: (Int, P) -> T
}

public class ScriptOpcodeListBuilder<P, T> {
    public val opcodes: OpcodeMutableMap<P, T> = OpcodeHashMap()

    public fun opcode(opcode: Int, init: ScriptOpcodeBuilder<P, T>.() -> Unit) {
        val builder = ScriptOpcodeBuilder<P, T>()
        builder.init()
        opcodes[opcode] = ScriptOpcode(opcode, builder.argumentCount, builder.names, builder.flagCheckDetails, builder.entryConstructor)
    }

    public fun DrGame.ScriptOpcodeFactory<P, T>.opcode(opcode: Int, argumentCount: Int): Unit = opcode(opcode, argumentCount, this::entryFor)
    public fun DrGame.ScriptOpcodeFactory<P, T>.opcode(opcode: Int, argumentCount: Int, flagCheckDetails: FlagCheckDetails?): Unit = opcode(opcode, argumentCount, flagCheckDetails, this::entryFor)
    public fun DrGame.ScriptOpcodeFactory<P, T>.opcode(opcode: Int, argumentCount: Int, name: String): Unit = opcode(opcode, argumentCount, name, this::entryFor)
    public fun DrGame.ScriptOpcodeFactory<P, T>.opcode(opcode: Int, argumentCount: Int, names: Array<out String>?): Unit = opcode(opcode, argumentCount, names, this::entryFor)
    public fun DrGame.ScriptOpcodeFactory<P, T>.opcode(opcode: Int, argumentCount: Int, name: String, flagCheckDetails: FlagCheckDetails?): Unit = opcode(opcode, argumentCount, name, flagCheckDetails, this::entryFor)
    public fun DrGame.ScriptOpcodeFactory<P, T>.opcode(opcode: Int, argumentCount: Int, names: Array<out String>?, flagCheckDetails: FlagCheckDetails?): Unit = opcode(opcode, argumentCount, names, flagCheckDetails, this::entryFor)

    public fun opcode(opcode: Int, argumentCount: Int, entryConstructor: (Int, P) -> T): Unit = opcode(opcode, argumentCount, null, null, entryConstructor)
    public fun opcode(opcode: Int, argumentCount: Int, flagCheckDetails: FlagCheckDetails?, entryConstructor: (Int, P) -> T): Unit = opcode(opcode, argumentCount, null, flagCheckDetails, entryConstructor)
    public fun opcode(opcode: Int, argumentCount: Int, name: String, entryConstructor: (Int, P) -> T): Unit = opcode(opcode, argumentCount, arrayOf(name), null, entryConstructor)
    public fun opcode(opcode: Int, argumentCount: Int, name: String, flagCheckDetails: FlagCheckDetails?, entryConstructor: (Int, P) -> T): Unit = opcode(opcode, argumentCount, arrayOf(name), flagCheckDetails, entryConstructor)
    public fun opcode(opcode: Int, argumentCount: Int, names: Array<out String>?, entryConstructor: (Int, P) -> T): Unit = opcode(opcode, argumentCount, names, null, entryConstructor)
    public fun opcode(opcode: Int, argumentCount: Int, names: Array<out String>?, flagCheckDetails: FlagCheckDetails?, entryConstructor: (Int, P) -> T) {
        opcodes[opcode] = ScriptOpcode(opcode, argumentCount, names, flagCheckDetails, entryConstructor)
    }

    public fun DrGame.ScriptOpcodeFactory<P, T>.flagCheck(opcode: Int, name: String, flagGroupLength: Int, endFlagCheckOpcode: Int): Unit = flagCheck(opcode, arrayOf(name), flagGroupLength, endFlagCheckOpcode, this::entryFor)
    public fun DrGame.ScriptOpcodeFactory<P, T>.flagCheck(opcode: Int, names: Array<out String>?, flagGroupLength: Int, endFlagCheckOpcode: Int): Unit = flagCheck(opcode, names, flagGroupLength, endFlagCheckOpcode, this::entryFor)

    public fun flagCheck(opcode: Int, name: String, flagGroupLength: Int, endFlagCheckOpcode: Int, entryConstructor: (Int, P) -> T): Unit = flagCheck(opcode, arrayOf(name), flagGroupLength, endFlagCheckOpcode, entryConstructor)
    public fun flagCheck(opcode: Int, names: Array<out String>?, flagGroupLength: Int, endFlagCheckOpcode: Int, entryConstructor: (Int, P) -> T) {
        opcodes[opcode] = ScriptOpcode(opcode, -1, names, FlagCheckDetails(flagGroupLength, endFlagCheckOpcode), entryConstructor)
    }

    public fun DrGame.ScriptOpcodeFactory<P, T>.fromList(list: List<JsonOpcode>): Unit = list.forEach { entry ->
        opcode(
                entry.opcode.toIntBaseN(),
                argumentCount = entry.argCount,
                names = entry.names ?: entry.name?.let { arrayOf(it) }
        )
    }
}

public class OpcodeCommandTypeBuilder {
    public val opcodeCommandTypes: MutableMap<Int, (Int) -> EnumWordScriptCommand> = HashMap()

    public fun opcode(opcode: Int, vararg types: EnumWordScriptCommand) {
        opcodeCommandTypes[opcode] = types::get
    }

    public fun opcode(opcode: Int, vararg types: Int) {
        opcodeCommandTypes[opcode] = types.map(EnumWordScriptCommand.Companion::invoke)::get
    }

    public fun opcode(opcode: Int, func: (Int) -> EnumWordScriptCommand) {
        opcodeCommandTypes[opcode] = func
    }
}

public inline fun <reified P, reified T> buildScriptOpcodes(init: ScriptOpcodeListBuilder<P, T>.() -> Unit): OpcodeMap<P, T> {
    val builder = ScriptOpcodeListBuilder<P, T>()
    builder.init()
    return builder.opcodes
}

public fun buildOpcodeCommandTypes(init: OpcodeCommandTypeBuilder.() -> Unit): OpcodeCommandTypeMap<EnumWordScriptCommand> {
    val builder = OpcodeCommandTypeBuilder()
    builder.init()
    return builder.opcodeCommandTypes
}