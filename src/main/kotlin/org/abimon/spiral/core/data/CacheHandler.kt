package org.abimon.spiral.core.data

import org.abimon.spiral.mvc.SpiralModel
import org.abimon.visi.io.ByteArrayDataSource
import org.abimon.visi.io.ByteArrayIOStream
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.FileDataSource
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
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

    fun cacheStream(): Pair<OutputStream, DataSource> {
        if(SpiralModel.cacheEnabled) {
            val cacheFile = newCacheFile()
            cacheFiles.add(cacheFile)

            return FileOutputStream(cacheFile) to FileDataSource(cacheFile)
        } else {
            val bios = ByteArrayIOStream()

            return bios.outputStream to bios.dataSource
        }
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
            cacheDir.listFiles().forEach { it.delete() }
        })
    }
}