package org.abimon.spiral.core.objects.scripting

import org.abimon.spiral.core.wrd.WRDScript
import org.abimon.spiral.core.writeInt
import org.abimon.spiral.core.writeShort
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class CustomWRD {
    val entries: ArrayList<WRDScript> = ArrayList()
    val cmds: Array<MutableList<String>> = Array(3, ::ArrayList)
    val strings: ArrayList<String> = arrayListOf("")

    fun entry(script: WRDScript) {
        entries.add(script)
    }

    fun command(type: Int, cmd: String): Int {
        if(cmd !in cmds[type])
            cmds[type].add(cmd)
        return cmds[type].indexOf(cmd)
    }

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

        wrd.writeShort(strings.size, true, true) //String Count
        wrd.writeShort(cmds[0].size, true, true) //cmd1Count
        wrd.writeShort(cmds[1].size, true, true) //cmd2Count
        wrd.writeShort(cmds[2].size, true, true) //cmd3Count

        wrd.writeInt(0, true, true) //unk
        wrd.writeInt(0x20 + entryData.size(), true, true) //unkOffset
        wrd.writeInt(0x20 + entryData.size(), true, true) //cmd3Offset
        wrd.writeInt(0x20 + entryData.size() + cmdData[2].size(), true, true) //cmd1Offset
        wrd.writeInt(0x20 + entryData.size() + cmdData[2].size() + cmdData[0].size(), true, true) //cmd2Offset
        wrd.writeInt(0x20 + entryData.size() + cmdData[2].size() + cmdData[0].size() + cmdData[1].size(), true, true) //strOffset

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