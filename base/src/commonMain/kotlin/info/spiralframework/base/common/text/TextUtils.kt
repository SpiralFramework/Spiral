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