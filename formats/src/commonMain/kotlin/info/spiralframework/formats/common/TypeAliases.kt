package info.spiralframework.formats.common

import info.spiralframework.formats.common.data.ScriptOpcode
import info.spiralframework.formats.common.scripting.lin.LinEntry

typealias LinEntryConstructor =  (Int, IntArray) -> LinEntry

typealias OpcodeMap<S> = Map<Int, ScriptOpcode<S>>
typealias OpcodeMutableMap<S> = MutableMap<Int, ScriptOpcode<S>>
typealias OpcodeHashMap<S> = HashMap<Int, ScriptOpcode<S>>