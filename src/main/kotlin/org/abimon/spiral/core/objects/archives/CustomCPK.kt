package org.abimon.spiral.core.objects.archives

import org.abimon.visi.io.DataSource

class CustomCPK {
    companion object {
        val MAGIC = byteArrayOf(0x43, 0x50, 0x4B, 0x20)
        val HEADER_PADDING = byteArrayOf(0xFF.toByte(), 0x00, 0x00, 0x00, 0x50, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
    }

    private val files: MutableMap<String, DataSource> = HashMap()
}