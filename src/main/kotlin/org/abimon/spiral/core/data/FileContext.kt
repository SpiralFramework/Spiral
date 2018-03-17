package org.abimon.spiral.core.data

import java.io.File
import java.io.InputStream

class FileContext(val dir: File) {
    fun provide(name: String): (() -> InputStream)? {
        val file = File(dir, name.replace("/", File.separator).replace("\\", File.separator))

        if (file.exists() && file.isFile)
            return file::inputStream

        return null
    }
}