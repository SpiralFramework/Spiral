package info.spiralframework.formats.common

import info.spiralframework.formats.common.data.ScriptOpcode
import info.spiralframework.formats.common.scripting.lin.LinEntry

public typealias LinEntryConstructor = (Int, IntArray) -> LinEntry

public typealias OpcodeMap<P, S> = Map<Int, ScriptOpcode<P, S>>
public typealias OpcodeMutableMap<P, S> = MutableMap<Int, ScriptOpcode<P, S>>
public typealias OpcodeHashMap<P, S> = HashMap<Int, ScriptOpcode<P, S>>

public typealias OpcodeCommandTypeMap<S> = Map<Int, (Int) -> S>

public operator fun <P, S> OpcodeMap<P, S>.get(name: String): ScriptOpcode<P, S>? =
    this.values.firstOrNull { opcode -> opcode.names?.contains(name) == true }