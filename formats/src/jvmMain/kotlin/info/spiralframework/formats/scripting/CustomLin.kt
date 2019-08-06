package info.spiralframework.formats.scripting

import info.spiralframework.base.util.writeInt32LE
import info.spiralframework.formats.scripting.lin.LinScript
import info.spiralframework.formats.scripting.lin.LinTextScript
import info.spiralframework.formats.scripting.lin.TextCountEntry
import info.spiralframework.formats.utils.removeEscapes
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class CustomLin {
    var type = 2
    var header: ByteArray = ByteArray(0)
    val entries: MutableList<LinScript> = ArrayList()
    val linesOfText: MutableList<String> = ArrayList()

    var writeTextBOM: Boolean = true

    fun add(entry: LinScript) {
        if (entry is LinTextScript) addText(entry)
        entries.add(entry)
    }

    fun addDirectly(entry: LinScript) {
        entries.add(entry)
    }

    fun addAll(entries: Array<LinScript>) {
        entries.forEach { entry -> if (entry is LinTextScript) addText(entry) }
        this.entries.addAll(entries)
    }

    fun addAll(entries: List<LinScript>) {
        entries.forEach { entry -> if (entry is LinTextScript) addText(entry) }
        this.entries.addAll(entries)
    }

    fun addText(textEntry: LinTextScript) {
        val text = textEntry.text ?: return
        textEntry.textID = addText(text)
    }
    fun addText(text: String): Int {
        val index = linesOfText.indexOf(text)
        if (index == -1) {
            linesOfText.add(text)
            return linesOfText.size - 1
        }

        return linesOfText.indexOf(text)
    }

    fun compile(out: OutputStream) {
        out.writeInt32LE(type)
        out.writeInt32LE(header.size + (if (type == 1) 12 else 16))

        val entryData = ByteArrayOutputStream()
        val textData = ByteArrayOutputStream()
        val textText = ByteArrayOutputStream()

        textData.writeInt32LE(linesOfText.size)

        if (entries[0] !is TextCountEntry)
            entries.add(0, TextCountEntry(linesOfText.size))

        entries.forEach { entry ->
            entryData.write(0x70)
            entryData.write(entry.opCode)

//            if (entry is LinTextScript) {
//                val strData = (entry.text ?: "Hello, Null!").removeEscapes().toByteArray(Charsets.UTF_16LE)
//                textData.writeInt32LE((numText * 4L) + 8 + textText.size())
//                if (entry.writeBOM) {
//                    textText.write(0xFF)
//                    textText.write(0xFE)
//                }
//
//                textText.write(strData)
//                if (strData[strData.size - 1] != 0x00.toByte() || strData[strData.size - 2] != 0x00.toByte()) {
//                    textText.write(0x00)
//                    textText.write(0x00)
//                }
//
//                entryData.write(textID / 256)
//                entryData.write(textID % 256)
//
//                textID++
//            } else {
                entry.rawArguments.forEach { arg -> entryData.write(arg) }
//            }
        }

        linesOfText.forEach { line ->
            val strData = line.removeEscapes().toByteArray(Charsets.UTF_16LE)
            textData.writeInt32LE((linesOfText.size * 4L) + 8 + textText.size())
            if (writeTextBOM) {
                textText.write(0xFF)
                textText.write(0xFE)
            }

            textText.write(strData)
            if (strData[strData.size - 1] != 0x00.toByte() || strData[strData.size - 2] != 0x00.toByte()) {
                textText.write(0x00)
                textText.write(0x00)
            }
        }

        textData.writeInt32LE((linesOfText.size * 4L) + 8 + textText.size())

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