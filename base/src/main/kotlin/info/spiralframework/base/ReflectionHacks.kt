package info.spiralframework.base

import java.io.FileInputStream
import java.io.InputStream

val STRING_CLASS_NAME = String::class.java.name
val FILE_INPUT_STREAM_PATH_FIELD = FileInputStream::class.java.declaredFields.firstOrNull { field -> field.name == "path" && field.type.name == STRING_CLASS_NAME }?.apply {
    isAccessible = true
}

val FileInputStream.publicPath: String?
    get() = FILE_INPUT_STREAM_PATH_FIELD?.let { field -> field[this] as? String }

val InputStream.path: String?
    get() = when (this) {
        is FileInputStream -> publicPath
        else -> null
    }