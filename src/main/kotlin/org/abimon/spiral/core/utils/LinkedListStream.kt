package org.abimon.spiral.core.utils

import java.io.InputStream

class LinkedListStream(val stream: InputStream) {
    private var buffer: Int? = null

    fun read(): Int {
        val r = buffer ?: stream.read()
        buffer = null

        return r
    }

    fun peek(): Int {
        if(buffer == null)
            buffer = stream.read()

        return buffer!!
    }
}