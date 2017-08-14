package org.abimon.spiral.core

import org.abimon.spiral.core.formats.*
import org.abimon.spiral.core.objects.Pak
import org.abimon.spiral.core.objects.WAD
import org.abimon.visi.io.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object SpiralActions {

}

fun WAD.extractToDirectory(directory: File) {
    if (!directory.isDirectory)
        throw IllegalArgumentException("$directory is not a directory!")

    directories.forEach {
        File(directory, it.name).mkdirs()
    }

    files.forEach {
        try {
            val file = File(directory, it.name)

            val start = System.currentTimeMillis()
            FileOutputStream(file).use { fos -> it.use { stream -> stream.writeTo(fos, closeAfter = true) } }
            println("Finished $file, took ${System.currentTimeMillis() - start} ms")
        } catch(th: Throwable) {
            th.printStackTrace()
            return
        }
    }
}

fun Collection<File>.convertTgaToPng() {
    filter { it.name.endsWith(".tga") && SpiralFormats.TGA.isFormat(FileDataSource(it)) }.forEach {
        val data = it.readBytes()
        val out = FileOutputStream(File(it.absolutePath.replace(".tga", ".png")))
        SpiralFormats.TGA.convert(SpiralFormats.PNG, FunctionDataSource { data }, out, emptyMap())
        it.delete()
    }
}

fun Collection<File>.convertPakToZip() {
    filter {file -> file.name.endsWith(".pak") && SpiralFormats.PAK.isFormat(FileDataSource(file)) }.forEach { file ->
        //SpiralData.registerFormat(file.name, file.readBytes(), SpiralFormats.PAK)

        FileOutputStream(file.absolutePath.replace(".pak", ".zip")).use { Pak(FileDataSource(file)).convertToZip(file.name, it) }
    }
}

fun Pak.convertToZip(name: String, outputStream: OutputStream) {
    val zipOut = ZipOutputStream(outputStream)

    files.forEach {
        val format = SpiralFormats.formatForData(it, SpiralFormats.drWadFormats)

        if(format != null) {
            when(format) {
                is TGAFormat -> {
                    zipOut.putNextEntry(ZipEntry("${it.name}.${PNGFormat.extension}"))
                    format.convert(SpiralFormats.PNG, it, zipOut, emptyMap())
                }
                is PAKFormat -> {
                    zipOut.putNextEntry(ZipEntry("${it.name}.${ZIPFormat.extension}"))
                    format.convert(SpiralFormats.ZIP, it, zipOut, emptyMap())
                }
                is WADFormat -> println("Oh no. $name#${it.name} is a WAD file. Panic. Now.")
                else -> {
                    zipOut.putNextEntry(ZipEntry("${it.name}.${format.extension}"))
                    it.use { stream -> stream.writeTo(zipOut) }
                }
            }
        }
        else {
            zipOut.putNextEntry(ZipEntry(it.name))
            it.use { stream -> stream.writeTo(zipOut, closeAfter = true) }
        }
    }

    zipOut.closeEntry()
}

fun ZipInputStream.convertToMap(): HashMap<String, DataSource> {
    val map = HashMap<String, DataSource>()

    while (true) {
        val entry = nextEntry ?: break
        val data = readAllBytes()
        map.put(entry.name, FunctionDataSource { data })
    }

    return map
}