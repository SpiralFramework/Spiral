package info.spiralframework.formats.utils

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.concurrent.CopyOnWriteArraySet

object DataHandler {
    var byteArrayToMap: (ByteArray) -> Map<String, Any?>? = { streamToMap(ByteArrayInputStream(it)) }
    var stringToMap: (String) -> Map<String, Any?>? = { streamToMap(ByteArrayInputStream(it.toByteArray())) }
    var fileToMap: (File) -> Map<String, Any?>? = { streamToMap(FileInputStream(it)) }
    lateinit var streamToMap: (InputStream) -> Map<String, Any?>?

    fun readMapFromByteArray(byteArray: ByteArray): Map<String, Any?>? = byteArrayToMap(byteArray)
    fun readMapFromString(string: String): Map<String, Any?>? = stringToMap(string)
    fun readMapFromFile(file: File): Map<String, Any?>? = fileToMap(file)
    fun readMapFromStream(stream: InputStream): Map<String, Any?>? = streamToMap(stream)

    val tmpFiles: MutableSet<File> = CopyOnWriteArraySet()

    //TODO: Figure out a better way to cache this data
    fun createTmpFile(hash: String): File {
        val tmp = File.createTempFile(hash, ".dat")
        tmp.deleteOnExit()
        tmpFiles.add(tmp)

        return tmp
    }

    fun shouldReadMap(): Boolean = this::streamToMap.isInitialized
}