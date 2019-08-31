package info.spiralframework.base.binding

import java.nio.charset.Charset

val TextCharsets.java: Charset
    get() = when (this) {
        TextCharsets.UTF_8 -> Charsets.UTF_8
        TextCharsets.UTF_16 -> Charsets.UTF_16
        TextCharsets.UTF_16BE -> Charsets.UTF_16BE
        TextCharsets.UTF_16LE -> Charsets.UTF_16LE
    }

actual fun ByteArray.decodeToString(charset: TextCharsets): String =
        String(this, charset.java)