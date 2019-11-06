package info.spiralframework.formats.common

typealias OpcodeMap<S> = Map<Int, Triple<Array<String>, Int, (Int, IntArray) -> S>>
typealias OpcodeMutableMap<S> = MutableMap<Int, Triple<Array<String>, Int, (Int, IntArray) -> S>>
typealias OpcodeHashMap<S> = HashMap<Int, Triple<Array<String>, Int, (Int, IntArray) -> S>>