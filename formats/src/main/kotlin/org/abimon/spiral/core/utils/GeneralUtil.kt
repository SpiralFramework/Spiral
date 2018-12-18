package org.abimon.spiral.core.utils

import org.abimon.spiral.core.objects.archives.srd.RSIEntry
import java.io.InputStream
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*

typealias OpCodeMap<A, S> = Map<Int, Triple<Array<String>, Int, (Int, A) -> S>>
typealias OpCodeMutableMap<A, S> = MutableMap<Int, Triple<Array<String>, Int, (Int, A) -> S>>
typealias OpCodeHashMap<A, S> = HashMap<Int, Triple<Array<String>, Int, (Int, A) -> S>>

typealias UV = Pair<Float, Float>
typealias Vertex = Triple<Float, Float, Float>
typealias TriFace = Triple<Int, Int, Int>

typealias Mipmap = RSIEntry.ResourceArray
typealias VertexBlock = RSIEntry.ResourceArray
typealias IndexBlock = RSIEntry.ResourceArray
typealias FaceBlock = RSIEntry.ResourceArray

infix fun <A, B, C> Pair<A, B>.and(c: C): Triple<A, B, C> = Triple(first, second, c)

operator fun <A, S> OpCodeMutableMap<A, S>.set(key: Int, value: Triple<String?, Int, (Int, A) -> S>) {
    if (value.first == null)
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
    if (!statement)
        throw ammo
}

fun Float.roundToPrecision(places: Int = 4): Float {
    try {
        return BigDecimal(java.lang.Float.toString(this)).setScale(places, BigDecimal.ROUND_HALF_UP).toFloat()
    } catch (nfe: NumberFormatException) {
        nfe.printStackTrace()
        throw nfe
    }
}

fun DecimalFormat.formatPair(pair: Pair<Float, Float>): Pair<String, String> = Pair(format(pair.first), format(pair.second))
fun DecimalFormat.formatTriple(triple: Triple<Float, Float, Float>): Triple<String, String, String> = Triple(format(triple.first), format(triple.second), format(triple.third))

fun Int.align(size: Int = 0x10): Int = (size - this % size) % size
fun Long.align(size: Int = 0x10): Int = ((size - this % size) % size).toInt()

fun <T> (() -> InputStream).use(op: (InputStream) -> T): T = this().use(op)
fun <T> (() -> InputStream).useAt(offset: Number, op: (InputStream) -> T): T = this().use { stream ->
    stream.skip(offset.toLong())
    return@use op(stream)
}

inline fun <reified T : Any> Any.castToTypedArray(): Array<T>? {
    when (this) {
        is Array<*> -> return this.mapNotNull { any -> if (any is T) any else null }.toTypedArray()
        is Iterable<*> -> return this.mapNotNull { any -> if (any is T) any else null }.toTypedArray()
        else -> return null
    }
}

fun String.removeEscapes(): String =
        buildString {
            var escaping: Boolean = false

            this@removeEscapes.forEach { c ->
                if (escaping) {
                    when (c) {
                        'n' -> append('\n')
                        't' -> append('\t')
                        'b' -> append('\b')
                        'r' -> append('\r')
                        '0' -> append(0x00.toChar())
                        else -> {
                            append('\\')
                            append(c)
                        }
                    }

                    escaping = false
                } else if (c == '\\') {
                    escaping = true
                } else {
                    append(c)
                }
            }
        }

fun ByteArray.foldToInt16LE(): IntArray = IntArray(size / 2) { i -> (this[i * 2 + 1].toInt() and 0xFF shl 8) or (this[i * 2].toInt() and 0xFF) }
fun ByteArray.foldToInt16BE(): IntArray = IntArray(size / 2) { i -> (this[i * 2].toInt() and 0xFF shl 8) or (this[i * 2 + 1].toInt() and 0xFF) }

fun <T> Array<T>.mapInPlace(transform: (T) -> T): Array<T> {
    for (i in 0 until size)
        this[i] = transform(this[i])

    return this
}