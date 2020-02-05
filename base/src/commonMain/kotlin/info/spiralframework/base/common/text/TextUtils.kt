package info.spiralframework.base.common.text

fun String.toIntBaseN(): Int = when {
    startsWith("0b") -> substring(2).toInt(2)
    startsWith("0o") -> substring(2).toInt(8)
    startsWith("0x") -> substring(2).toInt(16)
    startsWith("0d") -> substring(2).toInt()
    else -> toInt()
}

fun Int.toHexString(): String = buildString {
    append("0x")
    val hex = toString(16)
    for (i in 0 until hex.length % 2)
        append('0')
    append(hex)
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