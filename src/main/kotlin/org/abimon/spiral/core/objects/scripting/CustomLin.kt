package org.abimon.spiral.core.objects.scripting

import org.abimon.spiral.core.lin.LinScript
import org.abimon.spiral.core.lin.TextCountEntry
import org.abimon.spiral.core.lin.TextEntry
import org.abimon.spiral.core.toDRBytes
import org.abimon.spiral.core.writeInt
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class CustomLin {
    var type = 2
    var header: ByteArray = ByteArray(0)
    val entries: ArrayList<LinScript> = ArrayList()

    fun type(type: Int) {
        this.type = type
    }

    fun entry(script: LinScript) {
        entries.add(script)
    }

    fun entry(text: String) {
        entry(TextEntry(text.replace("\\n", "\n"), 0, 0, 0))
    }

    fun compile(lin: OutputStream) {
        lin.writeInt(type.toLong())
        lin.writeInt((header.size + (if (type == 1) 12 else 16)).toLong())

        val entryData = ByteArrayOutputStream()
        val textData = ByteArrayOutputStream()
        val textText = ByteArrayOutputStream()

        textData.writeInt(entries.count { entry -> entry is TextEntry }.toLong())

        var textID = 0

        if (entries[0] !is TextCountEntry)
            entries.add(0, TextCountEntry(entries.count { entry -> entry is TextEntry }))
        val numText = (entries.count { entry -> entry is TextEntry }).toLong()

        entries.forEach { entry ->
            entryData.write(0x70)
            entryData.write(entry.getOpCode())

            if (entry is TextEntry) {
                val strData = entry.text.toDRBytes()
                textData.writeInt((numText * 4L) + 4 + textText.size())
                textText.write(strData)

                entryData.write(textID / 256)
                entryData.write(textID % 256)

                textID++
            } else {
                entry.getRawArguments().forEach { arg -> entryData.write(arg) }
            }
        }

        if (type == 1)
            lin.writeInt((12 + entryData.size() + textData.size() + textText.size()).toLong())
        else {
            lin.writeInt((16 + entryData.size()).toLong())
            lin.writeInt((16 + entryData.size() + textData.size() + textText.size()).toLong())
        }

        lin.write(entryData.toByteArray())
        lin.write(textData.toByteArray())
        lin.write(textText.toByteArray())
    }
}