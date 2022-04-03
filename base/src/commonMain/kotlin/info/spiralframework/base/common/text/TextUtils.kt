@file:Suppress("NOTHING_TO_INLINE")

package info.spiralframework.base.common.text

import kotlin.jvm.JvmInline

@JvmInline
public value class LazyString(private val init: () -> Any?) {
    override fun toString(): String = init().toString()
}

public inline fun lazyString(noinline init: () -> Any?): LazyString =
    LazyString(init)

public fun String.toIntBaseN(): Int = when {
    startsWith("0b") -> substring(2).toInt(2)
    startsWith("0o") -> substring(2).toInt(8)
    startsWith("0x") -> substring(2).toInt(16)
    startsWith("0d") -> substring(2).toInt()
    else -> toInt()
}

public fun String.toIntOrNullBaseN(): Int? = when {
    startsWith("0b") -> substring(2).toIntOrNull(2)
    startsWith("0o") -> substring(2).toIntOrNull(8)
    startsWith("0x") -> substring(2).toIntOrNull(16)
    startsWith("0d") -> substring(2).toIntOrNull()
    else -> toIntOrNull()
}

public fun Byte.toHexString(): String = toInt().and(0xFF).toHexString()
public fun Short.toHexString(): String = toInt().and(0xFFFF).toHexString()
public fun Int.toHexString(): String = StringBuilder().also(this::toHexString).toString()
public fun Long.toHexString(): String = StringBuilder().also(this::toHexString).toString()
public fun StringBuilder.appendHex(num: Number): Unit = num.toInt().toHexString(this)
public fun StringBuilder.appendLineHex(num: Number) {
    num.toInt().toHexString(this)
    appendLine()
}

public fun Int.toHexString(builder: StringBuilder) {
    with(builder) {
        append("0x")
        val hex = toString(16).uppercase()
        for (i in 0 until hex.length % 2)
            append('0')
        append(hex)
    }
}

public fun Long.toHexString(builder: StringBuilder) {
    with(builder) {
        append("0x")
        val hex = toString(16).uppercase()
        for (i in 0 until hex.length % 2)
            append('0')
        append(hex)
    }
}

public fun String.removeEscapes(): String =
        buildString {
            var i = 0
            while (i in this@removeEscapes.indices) {
                val c = this@removeEscapes[i++]
                if (c == '\\' && (i++) in this@removeEscapes.indices) {
                    when (this@removeEscapes[i]) {
                        'n' -> append('\n')
                        't' -> append('\t')
                        'b' -> append('\b')
                        'r' -> append('\r')
                        '0' -> append(0x00.toChar())
                        'u' -> {
                            val hex = this@removeEscapes.substring(i, i + 4)
                            i += 4
                            hex.toIntOrNull(16)?.toChar()?.let(this::append)
                        }
                        else -> {
                            append('\\')
                            append(c)
                        }
                    }
                } else {
                    append(c)
                }
            }
        }

public fun String.doublePadWindowsPaths(): String =
        replace("\"([a-zA-Z]):((?:\\\\([^\\\\/:*\"?<>|]{1,254}))+)\"".toRegex()) { result ->
            val drive = result.groupValues[1]
            val components = result.groupValues[2].replace("\\", "\\\\")

            "\"$drive:$components\""
        }