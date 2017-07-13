package org.abimon.spiral.core.objects

import org.abimon.spiral.core.lin.LinScript
import org.abimon.spiral.core.lin.TextCountEntry
import org.abimon.spiral.core.lin.TextEntry
import org.abimon.spiral.core.toDRBytes
import org.abimon.spiral.core.writeNumber
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
        lin.writeNumber(type.toLong(), 4, true)
        lin.writeNumber((header.size + (if (type == 1) 12 else 16)).toLong(), 4, true)

        val entryData = ByteArrayOutputStream()
        val textData = ByteArrayOutputStream()
        val textText = ByteArrayOutputStream()

        textData.writeNumber(entries.count { entry -> entry is TextEntry }.toLong(), 4, true)

        var textID: Int = 0

        if (entries[0] !is TextCountEntry)
            entries.add(0, TextCountEntry(entries.count { entry -> entry is TextEntry }))
        val numText = (entries.count { entry -> entry is TextEntry }).toLong()

        entries.forEach { entry ->
            entryData.write(0x70)
            entryData.write(entry.getOpCode())

            if (entry is TextEntry) {
                val strData = entry.text.toDRBytes()
                textData.writeNumber((numText * 4L) + 4 + textText.size(), 4, true)
                textText.write(strData)

                entryData.write(textID / 256)
                entryData.write(textID % 256)

                textID++
            } else {
                entry.getRawArguments().forEach { arg -> entryData.write(arg) }
            }
        }

        if (type == 1)
            lin.writeNumber((12 + entryData.size() + textData.size() + textText.size()).toLong(), 4, true)
        else {
            lin.writeNumber((16 + entryData.size()).toLong(), 4, true)
            lin.writeNumber((16 + entryData.size() + textData.size() + textText.size()).toLong(), 4, true)
        }

        lin.write(entryData.toByteArray())
        lin.write(textData.toByteArray())
        lin.write(textText.toByteArray())
    }
}