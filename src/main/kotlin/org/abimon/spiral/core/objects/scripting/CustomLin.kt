package org.abimon.spiral.core.objects.scripting

import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.lin.TextCountEntry
import org.abimon.spiral.core.objects.scripting.lin.TextEntry
import org.abimon.spiral.core.utils.writeInt32LE
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class CustomLin {
    var type = 2
    var header: ByteArray = ByteArray(0)
    val entries: ArrayList<LinScript> = ArrayList()

    fun add(entry: LinScript) {
        entries.add(entry)
    }

    fun addAll(entries: Array<LinScript>) {
        this.entries.addAll(entries)
    }

    fun addAll(entries: List<LinScript>) {
        this.entries.addAll(entries)
    }

    /**
     * WARNING: This will not work with UDG!!
     */
    fun add(text: String) {
        entries.add(TextEntry(text, 0))
    }

    fun compile(out: OutputStream) {
        out.writeInt32LE(type)
        out.writeInt32LE(header.size + (if (type == 1) 12 else 16))

        val entryData = ByteArrayOutputStream()
        val textData = ByteArrayOutputStream()
        val textText = ByteArrayOutputStream()

        textData.writeInt32LE(entries.count { entry -> entry is TextEntry })

        var textID = 0

        if (entries[0] !is TextCountEntry)
            entries.add(0, TextCountEntry(entries.count { entry -> entry is TextEntry }))
        val numText = entries.count { entry -> entry is TextEntry }

        entries.forEach { entry ->
            entryData.write(0x70)
            entryData.write(entry.opCode)

            if (entry is TextEntry) {
                val text = buildString {
                    val raw = (entry.text ?: "Hello, Null!")
                    if (!raw.startsWith("\uFFFE"))
                        append("\uFFFE")
                    append(raw)
                }

                val strData = text.toByteArray(Charsets.UTF_16LE)
                textData.writeInt32LE((numText * 4L) + 4 + textText.size())
                textText.write(strData)

                entryData.write(textID / 256)
                entryData.write(textID % 256)

                textID++
            } else {
                entry.rawArguments.forEach { arg -> entryData.write(arg) }
            }
        }

        if (type == 1)
            out.writeInt32LE(12 + entryData.size() + textData.size() + textText.size())
        else {
            out.writeInt32LE(16 + entryData.size())
            out.writeInt32LE(16 + entryData.size() + textData.size() + textText.size())
        }

        out.write(entryData.toByteArray())
        out.write(textData.toByteArray())
        out.write(textText.toByteArray())
    }
}