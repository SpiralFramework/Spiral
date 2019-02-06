package info.spiralframework.base.util

import java.io.File

/**
 * Get the relative path to this file, from parent directory [to]
 * Path will start with the name of [to]
 * @see [relativePathFrom]
 */
infix fun File.relativePathTo(to: File): String = to.name + absolutePath.replace(to.absolutePath, "")
/**
 * Get the relative path from parent directory [to] to this file
 * Path will ***not*** start with the name of [to]
 * @see [relativePathTo]
 */
infix fun File.relativePathFrom(to: File): String = absolutePath.replace(to.absolutePath + File.separator, "")

fun File.existingDirectory(): Boolean = isDirectory && exists()

fun File.ensureExists(): File {
    if (!exists()) {
        if (isDirectory) mkdirs()
        if (isFile) createNewFile()
    }

    return this
}