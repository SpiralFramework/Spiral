@file:Suppress("NOTHING_TO_INLINE")

package info.spiralframework.base.common

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.korneaNotEnoughData
import dev.brella.kornea.io.common.*
import info.spiralframework.base.common.text.toIntOrNullBaseN
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public infix fun UInt.alignmentNeededFor(alignment: Int): Long = (alignment - this % alignment) % alignment
public infix fun ULong.alignmentNeededFor(alignment: Int): Long = (alignment - this % alignment) % alignment
public infix fun Int.alignmentNeededFor(alignment: Int): Int = (alignment - this % alignment) % alignment
public infix fun Long.alignmentNeededFor(alignment: Int): Int = ((alignment - this % alignment) % alignment).toInt()

public infix fun UInt.alignedTo(alignment: Int): ULong = this + alignmentNeededFor(alignment)
public infix fun ULong.alignedTo(alignment: Int): ULong = this + alignmentNeededFor(alignment)
public infix fun Int.alignedTo(alignment: Int): Int = alignmentNeededFor(alignment) + this
public infix fun Long.alignedTo(alignment: Int): Long = alignmentNeededFor(alignment) + this

public fun ByteArray.toHexString(): String = buildString {
    this@toHexString.forEach { byte ->
        append(
            byte.toInt()
                .and(0xFF)
                .toString(16)
                .padStart(2, '0')
        )
    }
}

public fun Byte.reverseBits(): Int =
    (((this.toInt() and 0xFF) * 0x0202020202L and 0x010884422010L) % 1023).toInt() and 0xFF

public fun ByteArray.foldToInt16LE(): IntArray =
    IntArray(size / 2) { i -> (this[i * 2 + 1].toInt() and 0xFF shl 8) or (this[i * 2].toInt() and 0xFF) }

public fun ByteArray.foldToInt16BE(): IntArray =
    IntArray(size / 2) { i -> (this[i * 2].toInt() and 0xFF shl 8) or (this[i * 2 + 1].toInt() and 0xFF) }

public fun ByteArray.foldToInt32LE(): IntArray = IntArray(size / 4) { i ->
    (this[i * 4 + 3].toInt() and 0xFF shl 8) or
            (this[i * 4 + 2].toInt() and 0xFF shl 8) or
            (this[i * 4 + 1].toInt() and 0xFF shl 8) or
            (this[i * 4].toInt() and 0xFF)
}

/** Puts [value] in this, and returns [value] back */
public fun <K, V> MutableMap<K, V>.putBack(key: K, value: V): V {
    this[key] = value
    return value
}

public inline fun String.trimNulls(): String = trimEnd(NULL_TERMINATOR)

@Suppress("unused")
public inline fun Any?.unit(): Unit = Unit

@Suppress("unused")
public inline fun <T> Any?.nulled(): T? = null

@OptIn(ExperimentalContracts::class)
public inline fun unitBlock(block: () -> Any?) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    block()
}

@OptIn(ExperimentalContracts::class)
public inline fun <T> nullBlock(block: () -> Any?): T? {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    block()

    return null
}

public inline fun <K, V> mutableMapOfAll(vararg maps: Pair<Array<K>, V>): MutableMap<K, V> =
    HashMap<K, V>().apply {
        maps.forEach { (keys, value) -> keys.forEach { k -> this[k] = value } }
    }

public inline fun <K, V> mutableMapOfAll(vararg maps: Map<K, V>): MutableMap<K, V> =
    HashMap<K, V>().apply { maps.forEach { map -> putAll(map) } }

public inline fun <reified T> select(
    readline: () -> String?,
    prefix: String?,
    keys: Array<String>,
    values: Array<T>
): KorneaResult<T> =
    select(
        readline,
        prefix,
        keys,
        buildMap(values.size) {
            values.mapIndexed { index, value ->
                put(index.toString(), value)
            }
        })

public inline fun <reified T> select(
    readline: () -> String?,
    prefix: String?,
    keys: Array<String>,
    values: Map<String, T>
): KorneaResult<T> {
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
            return if (inputAsNum in keys.indices) {
                println("Selected: '${keys[inputAsNum]}'")

                KorneaResult.successOrEmpty(values[keys[inputAsNum]])
            } else if (inputAsNum == keys.size) {
                println("(Exiting selection)")
                KorneaResult.empty()
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