package org.abimon.spiral.core.data

import org.abimon.spiral.mvc.SpiralModel
import org.abimon.visi.io.ByteArrayDataSource
import org.abimon.visi.io.ByteArrayIOStream
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.FileDataSource
import org.abimon.visi.lang.and
import java.io.*
import java.nio.file.Files
import java.nio.file.attribute.DosFileAttributeView
import java.util.*

object CacheHandler {
    private val cacheFiles: MutableSet<File> = HashSet()
    private val cacheDir: File = File(".spiral_cache").apply {
        if(!exists())
            mkdir()
        if(Files.getFileAttributeView(toPath(), DosFileAttributeView::class.java) != null)
            Files.setAttribute(toPath(), "dos:hidden", true)

        listFiles().forEach { it.delete() }
    }

    fun cacheStream(name: String? = null): Pair<OutputStream, () -> InputStream> {
        if(SpiralModel.cacheEnabled) {
            val cacheFile = if(name == null) newCacheFile() else File(cacheDir, name)
            cacheFiles.add(cacheFile)

            return FileOutputStream(cacheFile) to { FileInputStream(cacheFile) }
        } else {
            val bios = ByteArrayIOStream()

            return bios.outputStream to { bios.inputStream }
        }
    }

    fun cacheRandomAccessStream(name: String? = null): Triple<DataSource, RandomAccessFile, Boolean> {
        val initialised: Boolean
        val cacheFile: File

        if (name == null) {
            initialised = false
            cacheFile = newCacheFile()
        } else {
            cacheFile = File(cacheDir, name)
            initialised = cacheFile.exists()
        }

        cacheFiles.add(cacheFile)

        return FileDataSource(cacheFile) to RandomAccessFile(cacheFile, "rw") and initialised
    }

    fun cacheRandomAccessOutput(name: String? = null): Triple<OutputStream, RandomAccessFile, Boolean> {
        val initialised: Boolean
        val cacheFile: File

        if (name == null) {
            initialised = false
            cacheFile = newCacheFile()
        } else {
            cacheFile = File(cacheDir, name)
            initialised = cacheFile.exists()
        }

        cacheFiles.add(cacheFile)

        return FileOutputStream(cacheFile) to RandomAccessFile(cacheFile, "rw") and initialised
    }

    fun cache(data: ByteArray): DataSource {
        if(SpiralModel.cacheEnabled) {
            val cacheFile = newCacheFile()
            cacheFiles.add(cacheFile)

            cacheFile.writeBytes(data)

            return FileDataSource(cacheFile)
        } else
            return ByteArrayDataSource(data)
    }

    fun newCacheFile(): File {
        var cacheFile: File
        do {
            cacheFile = File(cacheDir, UUID.randomUUID().toString())
        } while (cacheFile.exists())

        cacheFile.deleteOnExit()

        return cacheFile
    }

    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            if(SpiralModel.purgeCache)
                cacheDir.listFiles().forEach { it.delete() }
        })
    }

    fun purge() {
        cacheDir.listFiles().forEach { it.delete() }
    }
}