package org.abimon.spiral.core.utils

import org.abimon.spiral.core.objects.scripting.lin.LinScript

typealias OpCodeMap=Map<Int, Triple<Array<String>, Int, (Int, IntArray) -> LinScript>>
typealias OpCodeMutableMap=MutableMap<Int, Triple<Array<String>, Int, (Int, IntArray) -> LinScript>>
typealias OpCodeHashMap=HashMap<Int, Triple<Array<String>, Int, (Int, IntArray) -> LinScript>>

infix fun <A, B, C> Pair<A, B>.and(c: C): Triple<A, B, C> = Triple(first, second, c)

operator fun OpCodeMutableMap.set(key: Int, value: Triple<String?, Int, (Int, IntArray) -> LinScript>) {
    if(value.first == null)
        this[key] = Triple(emptyArray<String>(), value.second, value.third)
    else
        this[key] = Triple(arrayOf(value.first!!), value.second, value.third)
}

operator fun OpCodeMutableMap.set(key: Int, value: Pair<Int, (Int, IntArray) -> LinScript>) {
    this[key] = Triple(emptyArray<String>(), value.first, value.second)
}