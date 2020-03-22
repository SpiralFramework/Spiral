package info.spiralframework.base.common.text

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
fun Int.toHexString(): String = StringBuilder().also(this::toHexString).toString()
fun StringBuilder.appendHex(num: Number) = num.toInt().toHexString(this)
fun StringBuilder.appendlnHex(num: Number) {
    num.toInt().toHexString(this)
    appendln()
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

/** Appends a line separator to this Appendable. */
public fun Appendable.appendln(): Appendable = append('\n')

/** Appends value to the given Appendable and line separator after it. */
public inline fun Appendable.appendln(value: CharSequence?): Appendable = append(value).appendln()

/** Appends value to the given Appendable and line separator after it. */
public inline fun Appendable.appendln(value: Char): Appendable = append(value).appendln()

/** Appends a line separator to this StringBuilder. */
public fun StringBuilder.appendln(): StringBuilder = append('\n')

/** Appends [value] to this [StringBuilder], followed by a line separator. */
public inline fun StringBuilder.appendln(value: CharSequence?): StringBuilder = append(value).appendln()

/** Appends [value] to this [StringBuilder], followed by a line separator. */
public inline fun StringBuilder.appendln(value: String?): StringBuilder = append(value).appendln()

/** Appends [value] to this [StringBuilder], followed by a line separator. */
public inline fun StringBuilder.appendln(value: Any?): StringBuilder = append(value).appendln()

/** Appends [value] to this [StringBuilder], followed by a line separator. */
public inline fun StringBuilder.appendln(value: StringBuilder?): StringBuilder = append(value).appendln()

/** Appends [value] to this [StringBuilder], followed by a line separator. */
public inline fun StringBuilder.appendln(value: CharArray): StringBuilder = append(value).appendln()

/** Appends [value] to this [StringBuilder], followed by a line separator. */
public inline fun StringBuilder.appendln(value: Char): StringBuilder = append(value).appendln()

/** Appends [value] to this [StringBuilder], followed by a line separator. */
public inline fun StringBuilder.appendln(value: Boolean): StringBuilder = append(value).appendln()

/** Appends [value] to this [StringBuilder], followed by a line separator. */
public inline fun StringBuilder.appendln(value: Int): StringBuilder = append(value).appendln()

/** Appends [value] to this [StringBuilder], followed by a line separator. */
public inline fun StringBuilder.appendln(value: Short): StringBuilder = append(value.toInt()).appendln()

/** Appends [value] to this [StringBuilder], followed by a line separator. */
public inline fun StringBuilder.appendln(value: Byte): StringBuilder = append(value.toInt()).appendln()

/** Appends [value] to this [StringBuilder], followed by a line separator. */
public inline fun StringBuilder.appendln(value: Long): StringBuilder = append(value).appendln()

/** Appends [value] to this [StringBuilder], followed by a line separator. */
public inline fun StringBuilder.appendln(value: Float): StringBuilder = append(value).appendln()

/** Appends [value] to this [StringBuilder], followed by a line separator. */
public inline fun StringBuilder.appendln(value: Double): StringBuilder = append(value).appendln()
