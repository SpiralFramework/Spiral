package info.spiralframework.base.common.text

inline class LazyString(val init: () -> Any?) {
    override fun toString(): String = init().toString()
}

inline fun lazyString(noinline init: () -> Any?) =
    LazyString(init)

fun String.toIntBaseN(): Int = when {
    startsWith("0b") -> substring(2).toInt(2)
    startsWith("0o") -> substring(2).toInt(8)
    startsWith("0x") -> substring(2).toInt(16)
    startsWith("0d") -> substring(2).toInt()
    else -> toInt()
}

fun String.toIntOrNullBaseN(): Int? = when {
    startsWith("0b") -> substring(2).toIntOrNull(2)
    startsWith("0o") -> substring(2).toIntOrNull(8)
    startsWith("0x") -> substring(2).toIntOrNull(16)
    startsWith("0d") -> substring(2).toIntOrNull()
    else -> toIntOrNull()
}

fun Byte.toHexString(): String = toInt().and(0xFF).toHexString()
fun Short.toHexString(): String = toInt().and(0xFF).toHexString()
fun Int.toHexString(): String = StringBuilder().also(this::toHexString).toString()
fun Long.toHexString(): String = StringBuilder().also(this::toHexString).toString()
fun StringBuilder.appendHex(num: Number) = num.toInt().toHexString(this)
fun StringBuilder.appendLineHex(num: Number) {
    num.toInt().toHexString(this)
    appendLine()
}

fun Int.toHexString(builder: StringBuilder) {
    with(builder) {
        append("0x")
        val hex = toString(16).toUpperCase()
        for (i in 0 until hex.length % 2)
            append('0')
        append(hex)
    }
}

fun Long.toHexString(builder: StringBuilder) {
    with(builder) {
        append("0x")
        val hex = toString(16).toUpperCase()
        for (i in 0 until hex.length % 2)
            append('0')
        append(hex)
    }
}

fun String.removeEscapes(): String =
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

fun String.doublePadWindowsPaths(): String =
        replace("\"([a-zA-Z]):((?:\\\\(?:[^\\\\/:*\"?<>|]{1,254}))+)\"".toRegex()) { result ->
            val drive = result.groupValues[1]
            val components = result.groupValues[2].replace("\\", "\\\\")

            "\"$drive:$components\""
        }