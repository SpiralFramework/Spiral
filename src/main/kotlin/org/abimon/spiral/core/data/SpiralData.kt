package org.abimon.spiral.core.data

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.core.TripleHashMap
import org.abimon.spiral.core.put
import org.abimon.visi.io.DataSource
import org.abimon.visi.lang.make
import org.abimon.visi.security.sha512Hash
import java.io.*
import java.util.*

object SpiralData {
    val pakNames = HashMap<String, Pair<String, String>>()
    val formats = HashMap<String, Pair<String, SpiralFormat>>()
    val config = File(".spiral_names")
    val opCodes = make<TripleHashMap<Int, Int, String>> {
        put(0x00, 2, "Text Count")
        put(0x01, 3, "0x01")
        put(0x02, 2, "Text")
        put(0x03, 1, "Format")
        put(0x04, 4, "Filter")
        put(0x05, 2, "Movie")
        put(0x06, 8, "Animation")
        put(0x07, -1, "0x07")
        put(0x08, 5, "Voice Line")
        put(0x09, 3, "Music")
        put(0x0A, 3, "SFX A")
        put(0x0B, 2, "SFX B")
        put(0x0C, 2, "Truth Bullet")
        put(0x0D, 3, "0x0D")
        put(0x0E, 2, "0x0E")
        put(0x0F, 3, "Set Title")
        put(0x10, 3, "Set Report Info")
        put(0x11, 4, "0x11")
        put(0x12, -1, "0x12")
        put(0x13, -1, "0x13")
        put(0x14, 3, "Trial Camera")
        put(0x15, 3, "Load Map")
        put(0x16, -1, "0x16")
        put(0x17, -1, "0x17")
        put(0x18, -1, "0x18")
        put(0x19, 3, "Script")
        put(0x1A, 0, "Stop Script")
        put(0x1B, 3, "Run Script")
        put(0x1C, 0, "0x1C")
        put(0x1D, -1, "0x1D")
        put(0x1E, 5, "Sprite")
        put(0x1F, 7, "0x1F")
        put(0x20, 5, "0x20")
        put(0x21, 1, "Speaker")
        put(0x22, 3, "0x22")
        put(0x23, 5, "0x23")
        put(0x24, -1, "0x24")
        put(0x25, 2, "Change UI")
        put(0x26, 3, "Set Flag")
        put(0x27, 1, "Check Character")
        put(0x28, -1, "0x28")
        put(0x29, 1, "Check Object")
        put(0x2A, 2, "Set Label")
        put(0x2B, 1, "Choice")
        put(0x2C, 2, "0x2C")
        put(0x2D, -1, "0x2D")
        put(0x2E, 2, "0x2E")
        put(0x2F, 10, "0x2F")
        put(0x30, 3, "Show Background")
        put(0x31, -1, "0x31")
        put(0x32, 1, "0x32")
        put(0x33, 4, "0x33")
        put(0x34, 2, "Goto Label")
        put(0x35, -1, "Check Flag A")
        put(0x36, -1, "Check Flag B")
        put(0x37, -1, "0x37")
        put(0x38, -1, "0x38")
        put(0x39, 5, "0x39")
        put(0x3A, 0, "Wait For Input")
        put(0x3B, 0, "Wait Frame")
        put(0x3C, 0, "End Flag Check")
    }

    fun getPakName(pathName: String, data: ByteArray): String? {
        if (pakNames.containsKey(pathName)) {
            val (sha512, name) = pakNames[pathName]!!
            if (sha512 == data.sha512Hash())
                return name
        }

        return null
    }

    fun getFormat(pathName: String, data: ByteArray): SpiralFormat? {
        if (formats.containsKey(pathName)) {
            val (sha512, format) = formats[pathName]!!
            if (sha512 == data.sha512Hash())
                return format
        }

        return null
    }

    fun getFormat(pathName: String, data: DataSource): SpiralFormat? {
        if (formats.containsKey(pathName)) {
            val (sha512, format) = formats[pathName]!!
            if (sha512 == data.data.sha512Hash())
                return format
        }

        return null
    }

    fun registerFormat(pathName: String, data: ByteArray, format: SpiralFormat) {
        formats[pathName] = Pair(data.sha512Hash(), format)
        save()
    }

    fun save() {
        val writer = PrintWriter(BufferedWriter(FileWriter(config)))
        writer.use {
            pakNames.forEach { path, pair -> it.println("name|$path|${pair.first}|${pair.second}") }
            formats.forEach { path, pair -> it.println("format|$path|${pair.first}|${pair.second.name}") }
        }
    }

    init {
        if (!config.exists())
            config.createNewFile()

        val reader = BufferedReader(FileReader(config))
        reader.use {
            it.forEachLine {
                val components = it.split("|")
                when (components[0]) {
                    "name" -> pakNames.put(components[1], Pair(components[2], components[3]))
                    "format" -> {
                        val format = SpiralFormats.formatForName(components[3])
                        if (format != null)
                            formats.put(components[1], Pair(components[2], format))
                    }
                }
            }
        }
    }
}