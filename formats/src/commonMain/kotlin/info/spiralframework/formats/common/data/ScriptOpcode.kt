package info.spiralframework.formats.common.data

import info.spiralframework.base.common.text.toIntBaseN
import info.spiralframework.formats.common.OpcodeHashMap
import info.spiralframework.formats.common.OpcodeMap
import info.spiralframework.formats.common.OpcodeMutableMap
import info.spiralframework.formats.common.data.json.JsonOpcode
import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.formats.common.scripting.lin.LinEntry

data class FlagCheckDetails(val flagGroupLength: Int, val endFlagCheckOpcode: Int)
abstract class ScriptOpcode<out T>(open val opcode: Int, open val argumentCount: Int, open val names: Array<String>?, open val entryConstructor: (Int, IntArray) -> T)
data class LinScriptOpcode<T: LinEntry>(override val opcode: Int, override val argumentCount: Int, override val names: Array<String>?, val flagCheckDetails: FlagCheckDetails?, override val entryConstructor: (Int, IntArray) -> T): ScriptOpcode<T>(opcode, argumentCount, names, entryConstructor)

abstract class ScriptOpcodeBuilder<T> {
    var argumentCount: Int = -1
    var names: Array<String>? = null
    lateinit var entryConstructor: (Int, IntArray) -> T
}

open class LinScriptOpcodeBuilder<T: LinEntry>: ScriptOpcodeBuilder<T>() {
    var flagCheckDetails: FlagCheckDetails? = null
}
//open class WrdScriptOpcodeBuilder<T: >

class LinScriptOpcodeListBuilder<T: LinEntry> {
    val opcodes: OpcodeMutableMap<T> = OpcodeHashMap()

    fun opcode(opcode: Int, init: ScriptOpcodeBuilder<T>.() -> Unit) {
        val builder = LinScriptOpcodeBuilder<T>()
        builder.init()
        opcodes[opcode] = LinScriptOpcode(opcode, builder.argumentCount, builder.names, builder.flagCheckDetails, builder.entryConstructor)
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
        opcodes[opcode] = LinScriptOpcode(opcode, argumentCount, names, flagCheckDetails, entryConstructor)
    }

    fun DrGame.ScriptOpcodeFactory<T>.flagCheck(opcode: Int, name: String, flagGroupLength: Int, endFlagCheckOpcode: Int) = flagCheck(opcode, arrayOf(name), flagGroupLength, endFlagCheckOpcode, this::entryFor)
    fun DrGame.ScriptOpcodeFactory<T>.flagCheck(opcode: Int, names: Array<String>?, flagGroupLength: Int, endFlagCheckOpcode: Int) = flagCheck(opcode, names, flagGroupLength, endFlagCheckOpcode, this::entryFor)

    fun flagCheck(opcode: Int, name: String, flagGroupLength: Int, endFlagCheckOpcode: Int, entryConstructor: (Int, IntArray) -> T) = flagCheck(opcode, arrayOf(name), flagGroupLength, endFlagCheckOpcode, entryConstructor)
    fun flagCheck(opcode: Int, names: Array<String>?, flagGroupLength: Int, endFlagCheckOpcode: Int, entryConstructor: (Int, IntArray) -> T) {
        opcodes[opcode] = LinScriptOpcode(opcode, -1, names, FlagCheckDetails(flagGroupLength, endFlagCheckOpcode), entryConstructor)
    }

    fun DrGame.ScriptOpcodeFactory<T>.fromList(list: List<JsonOpcode>) = list.forEach { entry ->
        opcode(
                entry.opcode.toIntBaseN(),
                argumentCount = entry.argCount,
                names = entry.names ?: entry.name?.let { arrayOf(it) }
        )
    }
}

inline fun <reified T: LinEntry> buildLinScriptOpcodes(init: LinScriptOpcodeListBuilder<T>.() -> Unit): OpcodeMap<T> {
    val builder = LinScriptOpcodeListBuilder<T>()
    builder.init()
    return builder.opcodes
}