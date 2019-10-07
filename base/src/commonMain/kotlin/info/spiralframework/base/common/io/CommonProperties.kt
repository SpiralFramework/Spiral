package info.spiralframework.base.common.io

import info.spiralframework.base.common.io.flow.InputFlow

@ExperimentalUnsignedTypes
internal class PropertiesFlow(val backing: InputFlow) {
    var inByteBuf: ByteArray = ByteArray(8192)
    var lineBuf = CharArray(1024)
    var inLimit = 0
    var inOff = 0

    suspend fun readLine(): Int {
        var len = 0
        var c: Char

        var skipWhiteSpace = true
        var isCommentLine = false
        var isNewLine = true
        var appendedLineBegin = false
        var precedingBackslash = false
        var skipLF = false

        while (true) {
            if (inOff >= inLimit) {
                inLimit = backing.read(inByteBuf) ?: -1
                inOff = 0
                if (inLimit <= 0) {
                    if (len == 0 || isCommentLine) {
                        return -1
                    }
                    if (precedingBackslash) {
                        len--
                    }
                    return len
                }
            }
            //The line below is equivalent to calling a
            //ISO8859-1 decoder.
            c = (0xff and inByteBuf[inOff++].toInt()).toChar()
            if (skipLF) {
                skipLF = false
                if (c == '\n') {
                    continue
                }
            }
            if (skipWhiteSpace) {
                if (c == ' ' || c == '\t' || c == '\u000C') {
                    continue
                }
                if (!appendedLineBegin && (c == '\r' || c == '\n')) {
                    continue
                }
                skipWhiteSpace = false
                appendedLineBegin = false
            }
            if (isNewLine) {
                isNewLine = false
                if (c == '#' || c == '!') {
                    isCommentLine = true
                    continue
                }
            }

            if (c != '\n' && c != '\r') {
                lineBuf[len++] = c
                if (len == lineBuf.size) {
                    var newLength = lineBuf.size * 2
                    if (newLength < 0) {
                        newLength = Int.MAX_VALUE
                    }
                    val buf = CharArray(newLength)
                    lineBuf.copyInto(buf, 0)
                    lineBuf = buf
                }
                //flip the preceding backslash flag
                if (c == '\\') {
                    precedingBackslash = !precedingBackslash
                } else {
                    precedingBackslash = false
                }
            } else {
                // reached EOL
                if (isCommentLine || len == 0) {
                    isCommentLine = false
                    isNewLine = true
                    skipWhiteSpace = true
                    len = 0
                    continue
                }
                if (inOff >= inLimit) {
                    inLimit = backing.read(inByteBuf) ?: return -1
                    inOff = 0
                    if (inLimit <= 0) {
                        if (precedingBackslash) {
                            len--
                        }

                        return len
                    }
                }
                if (precedingBackslash) {
                    len -= 1
                    //skip the leading whitespace characters in following line
                    skipWhiteSpace = true
                    appendedLineBegin = true
                    precedingBackslash = false
                    if (c == '\r') {
                        skipLF = true
                    }
                } else {
                    return len
                }
            }
        }
    }
}

@ExperimentalUnsignedTypes
suspend fun InputFlow.loadProperties(): Map<String, String> {
    val convtBuf = CharArray(1024)
    var limit: Int
    var keyLen: Int
    var valueStart: Int
    var c: Char
    var hasSep: Boolean
    var precedingBackslash: Boolean
    val map: MutableMap<String, String> = HashMap()
    val reader = PropertiesFlow(this)

    while (true) {
        limit = reader.readLine()
        if (limit <= 0) {
            break
        }
        c = 0.toChar()
        keyLen = 0
        valueStart = limit
        hasSep = false

        //System.out.println("line=<" + new String(lineBuf, 0, limit) + ">");
        precedingBackslash = false
        while (keyLen < limit) {
            c = reader.lineBuf[keyLen]
            //need check if escaped.
            if ((c == '=' || c == ':') && !precedingBackslash) {
                valueStart = keyLen + 1
                hasSep = true
                break
            } else if ((c == ' ' || c == '\t' || c == '\u000C') && !precedingBackslash) {
                valueStart = keyLen + 1
                break
            }
            if (c == '\\') {
                precedingBackslash = !precedingBackslash
            } else {
                precedingBackslash = false
            }
            keyLen++
        }
        while (valueStart < limit) {
            c = reader.lineBuf[valueStart]
            if (c != ' ' && c != '\t' && c != '\u000C') {
                if (!hasSep && (c == '=' || c == ':')) {
                    hasSep = true
                } else {
                    break
                }
            }
            valueStart++
        }
        val key = loadConvert(reader.lineBuf, 0, keyLen, convtBuf)
        val value = loadConvert(reader.lineBuf, valueStart, limit - valueStart, convtBuf)
        map[key] = value
    }

    return map
}

private fun loadConvert(`in`: CharArray, off: Int, len: Int, convtBuf: CharArray): String {
    var off = off
    var convtBuf = convtBuf
    if (convtBuf.size < len) {
        var newLen = len * 2
        if (newLen < 0) {
            newLen = Int.MAX_VALUE
        }
        convtBuf = CharArray(newLen)
    }
    var aChar: Char
    val out = convtBuf
    var outLen = 0
    val end = off + len

    while (off < end) {
        aChar = `in`[off++]
        if (aChar == '\\') {
            aChar = `in`[off++]
            if (aChar == 'u') {
                // Read the xxxx
                var value = 0
                for (i in 0..3) {
                    aChar = `in`[off++]
                    when (aChar) {
                        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> value = (value shl 4) + aChar.toInt() - '0'.toInt()
                        'a', 'b', 'c', 'd', 'e', 'f' -> value = (value shl 4) + 10 + aChar.toInt() - 'a'.toInt()
                        'A', 'B', 'C', 'D', 'E', 'F' -> value = (value shl 4) + 10 + aChar.toInt() - 'A'.toInt()
                        else -> throw IllegalArgumentException(
                                "Malformed \\uxxxx encoding.")
                    }
                }
                out[outLen++] = value.toChar()
            } else {
                if (aChar == 't')
                    aChar = '\t'
                else if (aChar == 'r')
                    aChar = '\r'
                else if (aChar == 'n')
                    aChar = '\n'
                else if (aChar == 'f') aChar = '\u000C'
                out[outLen++] = aChar
            }
        } else {
            out[outLen++] = aChar
        }
    }
    return String(out, 0, outLen)
}