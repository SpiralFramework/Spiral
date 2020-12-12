package info.spiralframework.base.common

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.korneaNotEnoughData
import dev.brella.kornea.io.common.*
import info.spiralframework.base.common.text.toIntOrNullBaseN
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@ExperimentalUnsignedTypes
infix fun UInt.alignmentNeededFor(alignment: Int): Long = (alignment - this % alignment) % alignment

@ExperimentalUnsignedTypes
infix fun ULong.alignmentNeededFor(alignment: Int): Long = (alignment - this % alignment) % alignment
infix fun Int.alignmentNeededFor(alignment: Int): Int = (alignment - this % alignment) % alignment
infix fun Long.alignmentNeededFor(alignment: Int): Int = ((alignment - this % alignment) % alignment).toInt()

@ExperimentalUnsignedTypes
infix fun UInt.alignedTo(alignment: Int): ULong = this + alignmentNeededFor(alignment)

@ExperimentalUnsignedTypes
infix fun ULong.alignedTo(alignment: Int): ULong = this + alignmentNeededFor(alignment)
infix fun Int.alignedTo(alignment: Int): Int = alignmentNeededFor(alignment) + this
infix fun Long.alignedTo(alignment: Int): Long = alignmentNeededFor(alignment) + this

fun ByteArray.toHexString(): String = buildString {
    this@toHexString.forEach { byte ->
        append(
            byte.toInt()
                .and(0xFF)
                .toString(16)
                .padStart(2, '0')
        )
    }
}

fun Byte.reverseBits(): Int =
    (((this.toInt() and 0xFF) * 0x0202020202L and 0x010884422010L) % 1023).toInt() and 0xFF

fun ByteArray.foldToInt16LE(): IntArray = IntArray(size / 2) { i -> (this[i * 2 + 1].toInt() and 0xFF shl 8) or (this[i * 2].toInt() and 0xFF) }
fun ByteArray.foldToInt16BE(): IntArray = IntArray(size / 2) { i -> (this[i * 2].toInt() and 0xFF shl 8) or (this[i * 2 + 1].toInt() and 0xFF) }
fun ByteArray.foldToInt32LE(): IntArray = IntArray(size / 4) { i ->
    (this[i * 4 + 3].toInt() and 0xFF shl 8) or
            (this[i * 4 + 2].toInt() and 0xFF shl 8) or
            (this[i * 4 + 1].toInt() and 0xFF shl 8) or
            (this[i * 4].toInt() and 0xFF)
}

/** Puts [value] in this, and returns [value] back */
fun <K, V> MutableMap<K, V>.putBack(key: K, value: V): V {
    this[key] = value
    return value
}

inline fun String.trimNulls(): String = trimEnd(NULL_TERMINATOR)

@Suppress("unused")
inline fun Any?.unit(): Unit = Unit

@Suppress("unused")
inline fun <T> Any?.nulled(): T? = null
inline fun unitBlock(block: () -> Any?): Unit {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    block()

    return
}

inline fun <T> nullBlock(block: () -> Any?): T? {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    block()

    return null
}

inline fun <K, V> mutableMapOfAll(vararg maps: Pair<Array<K>, V>): MutableMap<K, V> =
    HashMap<K, V>().apply {
        maps.forEach { (keys, value) -> keys.forEach { k -> this[k] = value } }
    }

inline fun <K, V> mutableMapOfAll(vararg maps: Map<K, V>): MutableMap<K, V> =
    HashMap<K, V>().apply { maps.forEach { map -> putAll(map) } }

suspend inline fun <reified T> select(readline: () -> String?, prefix: String?, keys: Array<String>, values: Array<T>): KorneaResult<T> {
    val string = buildString {
        if (prefix?.isNotBlank() == true) append(prefix)

        keys.mapIndexed { index, name ->
            append("\n\t")
            append(index + 1)
            append(") ")
            append(name)
        }

        append("\n\t")
        append(keys.size + 1)
        append(") Exit")
    }

    while (true) {
        println(string)
        print(">>> ")

        val input = readline() ?: return korneaNotEnoughData("stdin broken")

        if (input.equals("exit", true) || input.equals("break", true) || input.equals("quit", true)) {
            println("(Exiting selection)")
            return KorneaResult.empty()
        }

        val inputAsNum = input.trim().toIntOrNullBaseN()?.minus(1)

        if (inputAsNum != null) {
            if (inputAsNum in keys.indices) {
                println("Selected: '${keys[inputAsNum]}'")

                return KorneaResult.success(values[inputAsNum], null)
            } else if (inputAsNum == keys.size) {
                println("(Exiting selection)")
                return KorneaResult.empty()
            } else {
                println("${inputAsNum + 1} outside of range!")
                continue
            }
        }

        val valueForKey = keys.withIndex().groupBy({ (_, k) -> k.commonPrefixWith(input, true).length }, IndexedValue<*>::index)
            .entries
            .maxByOrNull(Map.Entry<Int, *>::key)
            ?.let { (_, v) -> if (v.size == 1) v.first() else null }

        if (valueForKey == null) {
            println("Sorry, '$input' is a little ambiguous, try again maybe?")
        } else {
            return KorneaResult.success(values[valueForKey])
        }
    }
}

suspend inline fun <reified T> select(readline: () -> String?, prefix: String?, keys: Array<String>, values: Map<String, T>): KorneaResult<T> {
    val string = buildString {
        if (prefix?.isNotBlank() == true) append(prefix)

        keys.mapIndexed { index, name ->
            append("\n\t")
            append(index + 1)
            append(") ")
            append(name)
        }

        append("\n\t")
        append(keys.size + 1)
        append(") Exit")
    }

    while (true) {
        println(string)
        print(">>> ")

        val input = readline() ?: return korneaNotEnoughData("stdin broken")

        if (input.equals("exit", true) || input.equals("break", true) || input.equals("quit", true)) {
            println("(Exiting selection)")
            return KorneaResult.empty()
        }

        val inputAsNum = input.trim().toIntOrNullBaseN()?.minus(1)

        if (inputAsNum != null) {
            if (inputAsNum in keys.indices) {
                println("Selected: '${keys[inputAsNum]}'")

                return KorneaResult.successOrEmpty(values[keys[inputAsNum]], null)
            } else if (inputAsNum == keys.size) {
                println("(Exiting selection)")
                return KorneaResult.empty()
            } else {
                println("${inputAsNum + 1} outside of range!")
                continue
            }
        }

        val valueForKey = values.entries.groupBy { (k) -> k.commonPrefixWith(input, true).length }
            .entries
            .maxByOrNull(Map.Entry<Int, *>::key)
            ?.let { (_, v) -> v.firstOrNull { (k) -> k.length == input.length } }

        if (valueForKey == null) {
            println("Sorry, '$input' is a little ambiguous, try again maybe?")
        } else {
            return KorneaResult.success(valueForKey.value)
        }
    }
}