package org.abimon.spiral.core.objects.scripting

import org.abimon.spiral.core.objects.scripting.wrd.WrdScript
import org.abimon.spiral.core.utils.writeInt16LE
import org.abimon.spiral.core.utils.writeInt32LE
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class CustomWordScript {
    val entries: ArrayList<WrdScript> = ArrayList()
    val cmds: Array<MutableList<String>> = Array(3, ::ArrayList)
    val strings: ArrayList<String> = arrayListOf("")

    fun add(entry: WrdScript) {
        entries.add(entry)
    }

    fun addAll(entries: Array<WrdScript>) {
        this.entries.addAll(entries)
    }

    fun addAll(entries: List<WrdScript>) {
        this.entries.addAll(entries)
    }

    fun command(type: Int, cmd: String): Int {
        if(cmd !in cmds[type])
            cmds[type].add(cmd)
        return cmds[type].indexOf(cmd)
    }

    fun commandOne(cmd: String): Int = command(0, cmd)
    fun commandTwo(cmd: String): Int = command(1, cmd)
    fun commandThree(cmd: String): Int = command(2, cmd)

    fun string(str: String): Int {
        val index = strings.size
        strings.add(str)
        return index
    }

    fun compile(wrd: OutputStream) {
        val entryData = ByteArrayOutputStream()
        val cmdData: Array<ByteArrayOutputStream> = Array(3, ::ByteArrayOutputStream)

        cmds.forEachIndexed { index, commands -> commands.forEach { cmd ->
            val str = cmd.toByteArray(Charsets.UTF_8)
            cmdData[index].write(str.size)
            cmdData[index].write(str)
            cmdData[index].write(0x00)
        } }

        entries.forEach { entry ->
            entryData.write(0x70)
            entryData.write(entry.opCode)
            entry.rawArguments.forEach { arg -> entryData.write(arg) }
        }

        wrd.writeInt16LE(strings.size) //String Count
        wrd.writeInt16LE(cmds[0].size) //cmd1Count
        wrd.writeInt16LE(cmds[1].size) //cmd2Count
        wrd.writeInt16LE(cmds[2].size) //cmd3Count

        wrd.writeInt32LE(0) //unk
        wrd.writeInt32LE(0x20 + entryData.size()) //unkOffset
        wrd.writeInt32LE(0x20 + entryData.size()) //cmd3Offset
        wrd.writeInt32LE(0x20 + entryData.size() + cmdData[2].size()) //cmd1Offset
        wrd.writeInt32LE(0x20 + entryData.size() + cmdData[2].size() + cmdData[0].size()) //cmd2Offset
        wrd.writeInt32LE(0x20 + entryData.size() + cmdData[2].size() + cmdData[0].size() + cmdData[1].size()) //strOffset

        entryData.writeTo(wrd)
        cmdData[2].writeTo(wrd)
        cmdData[0].writeTo(wrd)
        cmdData[1].writeTo(wrd)

        strings.forEach { str ->
            val bytes = str.toByteArray(Charsets.UTF_16LE)

            if(bytes.size >= 0x80) {
                val copy = bytes.copyOfRange(0, 0x80)
                wrd.write(copy.size)
                wrd.write(copy)
                wrd.write(0x00)
                wrd.write(0x00)
            } else {
                wrd.write(bytes.size)
                wrd.write(bytes)
                wrd.write(0x00)
                wrd.write(0x00)
            }
        }
    }
}