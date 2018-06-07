package org.abimon.spiral.core.utils

import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.*

object DataHandler {
    var byteArrayToMap: (ByteArray) -> Map<String, Any?>? = { streamToMap(ByteArrayInputStream(it)) }
    var stringToMap: (String) -> Map<String, Any?>? = { streamToMap(ByteArrayInputStream(it.toByteArray())) }
    var fileToMap: (File) -> Map<String, Any?>? = { streamToMap(FileInputStream(it)) }
    lateinit var streamToMap: (InputStream) -> Map<String, Any?>?

    fun readMapFromByteArray(byteArray: ByteArray): Map<String, Any?>? = byteArrayToMap(byteArray)
    fun readMapFromString(string: String): Map<String, Any?>? = stringToMap(string)
    fun readMapFromFile(file: File): Map<String, Any?>? = fileToMap(file)
    fun readMapFromStream(stream: InputStream): Map<String, Any?>? = streamToMap(stream)

    fun shouldReadMap(): Boolean = this::streamToMap.isInitialized

    var LOGGER = LoggerFactory.getLogger("SpiralFormats")

    var cacheFileInitialiser: (String?) -> File = func@{ name ->
        var cacheFile: File
        do {
            cacheFile = File("." + UUID.randomUUID().toString())
        } while (cacheFile.exists())

        cacheFile.createNewFile()
        cacheFile.deleteOnExit()

        return@func cacheFile
    }

    var cacheFileWithNameAndDataInitialiser: (String, (File) -> Unit) -> File = func@{ name, dataFunc ->
        val file = cacheFileInitialiser(name)

        if (!file.exists() || file.length() == 0L)
            dataFunc(file)

        return@func file
    }

    fun newCacheFile(name: String? = null): File = cacheFileInitialiser(name)
    fun cacheFileWithNameAndData(name: String, dataFunc: (File) -> Unit): File = cacheFileWithNameAndDataInitialiser(name, dataFunc)
}