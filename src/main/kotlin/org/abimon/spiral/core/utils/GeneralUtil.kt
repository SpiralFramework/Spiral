package org.abimon.spiral.core.utils

typealias OpCodeMap<A, S> = Map<Int, Triple<Array<String>, Int, (Int, A) -> S>>
typealias OpCodeMutableMap<A, S> = MutableMap<Int, Triple<Array<String>, Int, (Int, A) -> S>>
typealias OpCodeHashMap<A, S> = HashMap<Int, Triple<Array<String>, Int, (Int, A) -> S>>

infix fun <A, B, C> Pair<A, B>.and(c: C): Triple<A, B, C> = Triple(first, second, c)

operator fun <A, S> OpCodeMutableMap<A, S>.set(key: Int, value: Triple<String?, Int, (Int, A) -> S>) {
    if(value.first == null)
        this[key] = Triple(emptyArray<String>(), value.second, value.third)
    else
        this[key] = Triple(arrayOf(value.first!!), value.second, value.third)
}

operator fun <A, S> OpCodeMutableMap<A, S>.set(key: Int, value: Pair<Int, (Int, A) -> S>) {
    this[key] = Triple(emptyArray<String>(), value.first, value.second)
}

fun assertAsArgument(statement: Boolean, illegalArgument: String) {
    if (!statement)
        throw IllegalArgumentException(illegalArgument)
}

fun assertOrThrow(statement: Boolean, ammo: Throwable) {
    if(!statement)
        throw ammo
}