package org.abimon.spiral.core.objects.scripting

import java.io.File
import java.io.RandomAccessFile

abstract class DRExecutable(val file: File) {
    companion object {

    }

    val raf = RandomAccessFile(file, "rw")

    abstract val maximumChapter: Int

    abstract val pakNames: Map<String, Array<String>>
    abstract val voiceLineArray: IntArray

    abstract fun save()
    abstract fun reset()

    fun getVoiceFileID(character: Int, originalChapter: Int, voiceID: Int): Int {
        val chapter: Int

        when (originalChapter) {
            10 -> chapter = 7
            99 -> chapter = 8
            else -> chapter = originalChapter
        }

        if (chapter > maximumChapter)
            return -1
        if (character > 33)
            return -1

        val characterIndex = (character + chapter + (maximumChapter * character)) * 2
        val minID = voiceLineArray[characterIndex - 1]

        if (minID == 0xFFFF)
            return -1

        if (voiceID < minID || voiceID > voiceLineArray[characterIndex])
            return -1

        var baseIndex = 0

        for (charID in 0 until character) {
            for (chapID in 1 .. maximumChapter) {
                val min = voiceLineArray[((charID + chapID + (maximumChapter * charID)) * 2) - 1]
                val max = voiceLineArray[(charID + chapID + (maximumChapter * charID)) * 2]
                if (min != 0xFFFF)
                    baseIndex += max - min + 1
            }
        }

        for (chapID in 1 until chapter) {
            val min = voiceLineArray[((character + chapID + (maximumChapter * character)) * 2) - 1]
            val max = voiceLineArray[(character + chapID + (maximumChapter * character)) * 2]
            if (min != 0xFFFF)
                baseIndex += max - min + 1
        }

        return baseIndex - minID + voiceID
    }
}