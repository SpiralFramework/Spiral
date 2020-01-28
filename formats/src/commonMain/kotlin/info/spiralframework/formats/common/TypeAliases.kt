package info.spiralframework.formats.common

import info.spiralframework.formats.common.data.ScriptOpcode
import info.spiralframework.formats.common.scripting.lin.LinEntry

typealias LinEntryConstructor =  (Int, IntArray) -> LinEntry

typealias OpcodeMap<P, S> = Map<Int, ScriptOpcode<P, S>>
typealias OpcodeMutableMap<P, S> = MutableMap<Int, ScriptOpcode<P, S>>
typealias OpcodeHashMap<P, S> = HashMap<Int, ScriptOpcode<P, S>>

typealias OpcodeCommandTypeMap<S> = Map<Int, (Int) -> S>