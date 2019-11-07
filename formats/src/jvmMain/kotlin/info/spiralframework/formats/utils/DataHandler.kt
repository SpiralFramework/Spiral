package info.spiralframework.formats.utils

import java.io.File
import java.util.concurrent.CopyOnWriteArraySet

object DataHandler {
    val tmpFiles: MutableSet<File> = CopyOnWriteArraySet()

    //TODO: Figure out a better way to cache this data
    fun createTmpFile(hash: String): File {
        val tmp = File.createTempFile(hash, ".dat")
        tmp.deleteOnExit()
        tmpFiles.add(tmp)

        return tmp
    }
}