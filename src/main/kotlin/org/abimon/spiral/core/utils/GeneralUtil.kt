package org.abimon.spiral.core.utils

import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.DosFileAttributeView
import java.util.*

typealias OpCodeMap<A, S> = Map<Int, Triple<Array<String>, Int, (Int, A) -> S>>
typealias OpCodeMutableMap<A, S> = MutableMap<Int, Triple<Array<String>, Int, (Int, A) -> S>>
typealias OpCodeHashMap<A, S> = HashMap<Int, Triple<Array<String>, Int, (Int, A) -> S>>

typealias UV = Pair<Float, Float>
typealias Vertex = Triple<Float, Float, Float>
typealias TriFace = Triple<Int, Int, Int>

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

fun CacheFile(): File {
    var cacheFile: File
    do {
        cacheFile = File("." + UUID.randomUUID().toString())
    } while (cacheFile.exists())

    cacheFile.createNewFile()
    cacheFile.deleteOnExit()
    if(Files.getFileAttributeView(cacheFile.toPath(), DosFileAttributeView::class.java) != null)
        Files.setAttribute(cacheFile.toPath(), "dos:hidden", true)

    return cacheFile
}