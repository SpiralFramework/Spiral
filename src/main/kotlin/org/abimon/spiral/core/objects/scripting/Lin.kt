package org.abimon.spiral.core.objects.scripting

import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.lin.*
import org.abimon.spiral.core.lin.dr1.LoadMapEntry
import org.abimon.spiral.core.lin.dr1.LoadScriptEntry
import org.abimon.spiral.core.lin.dr1.RunScriptEntry
import org.abimon.spiral.core.lin.dr1.TrialCameraEntry
import org.abimon.spiral.core.lin.dr2.*
import org.abimon.spiral.core.readDRString
import org.abimon.spiral.core.readNumber
import org.abimon.spiral.core.toIntArray
import org.abimon.spiral.util.CountingInputStream
import org.abimon.spiral.util.trace
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.read
import java.io.ByteArrayInputStream
import java.io.DataInputStream

/**
 * *Sigh*
 * Here we are, the script files.
 * Massive writeup for the lin format itself, check Formats.md
 */
class Lin(val dataSource: DataSource, var dr1: Boolean = true) {
    val linType: Long
    val headerSpace: Long
    val size: Long
    val textBlock: Long
    val header: ByteArray
    val entries: ArrayList<LinScript>

    init {
        val lin = CountingInputStream(DataInputStream(dataSource.inputStream))
        try {
            linType = lin.readNumber(4, true)
            headerSpace = lin.readNumber(4, true)

            if (linType == 1L) {
                size = lin.readNumber(4, true)
                textBlock = size
            } else if (linType == 2L) {
                textBlock = lin.readNumber(4, true)
                size = lin.readNumber(4, true)
            } else
                throw IllegalArgumentException("Unknown LIN type $linType")

            header = lin.read((headerSpace - (if (linType == 1L) 12 else 16)).toInt())
            entries = ArrayList<LinScript>()
            val data = lin.read((textBlock - headerSpace).toInt()).toIntArray()

            val textStream = ByteArrayInputStream(lin.read((size - textBlock).toInt()))
            val numTextEntries = textStream.readNumber(4, true)

            var i = 0

            for (index in 0 until data.size) {
                if (i >= data.size)
                    break

                if (data[i] == 0x0) {
                    trace("$i is 0x0")
                    i++
                } else if (data[i] != 0x70) {
                    while (i < data.size) {
                        trace("$i expected to be 0x70, was ${data[i]}")
                        if (data[i] == 0x00 || data[i] == 0x70)
                            break
                        i++
                    }
                } else {
                    i++
                    val opCode = data[i++]
                    val (argumentCount) = (if(dr1) SpiralData.dr1OpCodes else SpiralData.dr2OpCodes).getOrDefault(opCode, Pair(-1, opCode.toString(16)))
                    val params: IntArray

                    if (argumentCount == -1) {
                        val args = ArrayList<Int>()
                        while (i < data.size && data[i] != 0x70) {
                            args.add(data[i++])
                        }
                        params = args.toIntArray()
                    } else {
                        params = IntArray(argumentCount)

                        for (argumentIndex in 0 until argumentCount) {
                            params[argumentIndex] = data[i++]
                        }
                    }

                    if(dr1) {
                        when (opCode) {
                            0x00 -> { ensure(0x00, 2, params); entries.add(TextCountEntry(params[0], params[1])) }
                            0x01 -> { ensure(0x01, 3, params); entries.add(UnknownEntry(0x01, params)) }
                            0x02 -> {
                                ensure(0x02, 2, params)
                                val textID = ((params[0] shl 8) or params[1])
                                textStream.reset()
                                textStream.skip((textID + 1) * 4L)
                                val textPos = textStream.readNumber(4, true)

                                val nextTextPos: Long
                                if (textID.toLong() == (numTextEntries - 1))
                                    nextTextPos = size - textBlock
                                else {
                                    textStream.reset()
                                    textStream.skip((textID + 2) * 4L)
                                    nextTextPos = textStream.readNumber(4, true)
                                }

                                textStream.reset()
                                textStream.skip(textPos)
                                entries.add(TextEntry(textStream.readDRString((nextTextPos - textPos).toInt(), "UTF-16"), textID, (textBlock + textPos).toInt(), (textBlock + nextTextPos).toInt()))
                            }
                            0x03 -> { ensure(0x03, 1, params); entries.add(FormatEntry(params[0])) }
                            0x04 -> { ensure(0x04, 4, params); entries.add(FilterEntry(params[0], params[1], params[2], params[3])) }
                            0x05 -> { ensure(0x05, 2, params); entries.add(MovieEntry(params[0], params[1])) }
                            0x06 -> { ensure(0x06, 8, params); entries.add(AnimationEntry(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7])) }
                            0x08 -> { ensure(0x08, 5, params); entries.add(VoiceLineEntry(params[0], params[1], params[2], params[3], params[4])) }
                            0x09 -> { ensure(0x09, 3, params); entries.add(MusicEntry(params[0], params[1], params[2])) }
                            0x0A -> { ensure(0x0A, 3, params); entries.add(SoundEffectEntryA(params[0], params[1], params[2])) }
                            0x0B -> { ensure(0x0B, 2, params); entries.add(SoundEffectEntryB(params[0], params[1])) }
                            0x0C -> { ensure(0x0C, 2, params); entries.add(TruthBulletEntry(params[0], params[1])) }
                            0x0D -> { ensure(0x0D, 3, params); entries.add(UnknownEntry(0x0D, params)) }
                            0x0E -> { ensure(0x0E, 2, params); entries.add(UnknownEntry(0x0E, params)) }
                            0x0F -> { ensure(0x0F, 3, params); entries.add(SetStudentTitleEntry(params[0], params[1], params[2])) }
                            0x10 -> { ensure(0x10, 3, params); entries.add(SetStudentReportEntry(params[0], params[1], params[2])) }
                            0x11 -> { ensure(0x11, 4, params); entries.add(UnknownEntry(0x11, params)) }
                            0x14 -> { ensure(0x14, 3, params); entries.add(TrialCameraEntry(params[0], params[1], params[2])) }
                            0x15 -> { ensure(0x15, 3, params); entries.add(LoadMapEntry(params[0], params[1], params[2])) }
                            0x19 -> { ensure(0x19, 3, params); entries.add(LoadScriptEntry(params[0], params[1], params[2])) }
                            0x1A -> { ensure(0x1A, 0, params); entries.add(StopScriptEntry()) }
                            0x1B -> { ensure(0x1B, 3, params); entries.add(RunScriptEntry(params[0], params[1], params[2])) }
                            0x1C -> { ensure(0x1C, 0, params); entries.add(UnknownEntry(0x1C, IntArray(0))) }
                            0x1E -> { ensure(0x1E, 5, params); entries.add(SpriteEntry(params[0], params[1], params[2], params[3], params[4])) }
                            0x1F -> { ensure(0x1F, 7, params); entries.add(UnknownEntry(0x1F, params)) }
                            0x20 -> { ensure(0x20, 5, params); entries.add(UnknownEntry(0x20, params)) }
                            0x21 -> { ensure(0x21, 1, params); entries.add(SpeakerEntry(params[0])) }
                            0x22 -> { ensure(0x22, 3, params); entries.add(UnknownEntry(0x22, params)) }
                            0x23 -> { ensure(0x23, 5, params); entries.add(UnknownEntry(0x23, params)) }
                            0x25 -> { ensure(0x25, 2, params); entries.add(ChangeUIEntry(params[0], params[1])) }
                            0x26 -> { ensure(0x26, 3, params); entries.add(SetFlagEntry(params[0], params[1], params[2])) }
                            0x27 -> { ensure(0x27, 1, params); entries.add(CheckCharacterEntry(params[0])) }
                            0x29 -> { ensure(0x29, 1, params); entries.add(CheckObjectEntry(params[0])) }
                            0x2A -> { ensure(0x2A, 2, params); entries.add(SetLabelEntry(params[0], params[1])) }
                            0x2B -> { ensure(0x2B, 1, params); entries.add(ChoiceEntry(params[0])) }
                            0x2C -> { ensure(0x2C, 2, params); entries.add(UnknownEntry(0x2C, params)) }
                            0x2E -> { ensure(0x2E, 2, params); entries.add(UnknownEntry(0x2E, params)) }
                            0x2F -> { ensure(0x2F, 10, params); entries.add(UnknownEntry(0x2F, params)) }
                            0x30 -> { ensure(0x30, 3, params); entries.add(ShowBackgroundEntry(params[0], params[1], params[2])) }
                            0x32 -> { ensure(0x32, 1, params); entries.add(UnknownEntry(0x32, params)) }
                            0x33 -> { ensure(0x33, 4, params); entries.add(UnknownEntry(0x33, params)) }
                            0x34 -> { ensure(0x34, 2, params); entries.add(GoToLabelEntry(params[0], params[1])) }
                            0x35 -> entries.add(CheckFlagEntryA(params))
                            0x36 -> entries.add(CheckFlagEntryB(params))
                            0x39 -> { ensure(0x39, 5, params); entries.add(UnknownEntry(0x39, params)) }
                            0x3A -> { ensure(0x3A, 0, params); entries.add(WaitForInputEntry()) }
                            0x3B -> { ensure(0x3B, 0, params); entries.add(WaitFrameEntry()) }
                            0x3C -> { ensure(0x3C, 0, params); entries.add(EndFlagCheckEntry()) }

                            else -> entries.add(UnknownEntry(opCode, params))
                        }
                    } else {
                        when (opCode) {
                            0x00 -> { ensure(0x00, 2, params); entries.add(TextCountEntry(params[0], params[1])) }
                            0x01 -> { ensure(0x01, 4, params); entries.add(UnknownEntry(0x01, params)) }
                            0x02 -> {
                                ensure(0x02, 2, params)
                                val textID = ((params[0] shl 8) or params[1])
                                textStream.reset()
                                textStream.skip((textID + 1) * 4L)
                                val textPos = textStream.readNumber(4, true)

                                val nextTextPos: Long
                                if (textID.toLong() == (numTextEntries - 1))
                                    nextTextPos = size - textBlock
                                else {
                                    textStream.reset()
                                    textStream.skip((textID + 2) * 4L)
                                    nextTextPos = textStream.readNumber(4, true)
                                }

                                textStream.reset()
                                textStream.skip(textPos)
                                entries.add(TextEntry(textStream.readDRString((nextTextPos - textPos).toInt(), "UTF-16"), textID, (textBlock + textPos).toInt(), (textBlock + nextTextPos).toInt()))
                            }
                            0x03 -> { ensure(0x03, 1, params); entries.add(FormatEntry(params[0])) }
                            0x04 -> { ensure(0x04, 4, params); entries.add(FilterEntry(params[0], params[1], params[2], params[3])) }
                            0x05 -> { ensure(0x05, 2, params); entries.add(MovieEntry(params[0], params[1])) }
                            0x06 -> { ensure(0x06, 8, params); entries.add(AnimationEntry(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7])) }
                            0x08 -> { ensure(0x08, 5, params); entries.add(VoiceLineEntry(params[0], params[1], params[2], params[3], params[4])) }
                            0x09 -> { ensure(0x09, 3, params); entries.add(MusicEntry(params[0], params[1], params[2])) }
                            0x0A -> { ensure(0x0A, 3, params); entries.add(SoundEffectEntryA(params[0], params[1], params[2])) }
                            0x0B -> { ensure(0x0B, 2, params); entries.add(SoundEffectEntryB(params[0], params[1])) }
                            0x0C -> { ensure(0x0C, 2, params); entries.add(TruthBulletEntry(params[0], params[1])) }
                            0x0D -> { ensure(0x0D, 3, params); entries.add(UnknownEntry(0x0D, params)) }
                            0x0E -> { ensure(0x0E, 2, params); entries.add(UnknownEntry(0x0E, params)) }
                            0x0F -> { ensure(0x0F, 3, params); entries.add(SetStudentTitleEntry(params[0], params[1], params[2])) }
                            0x10 -> { ensure(0x10, 3, params); entries.add(SetStudentReportEntry(params[0], params[1], params[2])) }
                            0x11 -> { ensure(0x11, 4, params); entries.add(UnknownEntry(0x11, params)) }
                            0x14 -> { ensure(0x14, 6, params); entries.add(TrialCameraDR2Entry(params[0], params[1], params[2], params[3], params[4], params[5])) }
                            0x15 -> { ensure(0x15, 4, params); entries.add(LoadMapDR2Entry(params[0], params[1], params[2], params[3])) }
                            0x19 -> { ensure(0x19, 5, params); entries.add(LoadScriptDR2Entry(params[0], params[1], params[2], params[3], params[4])) }
                            0x1A -> { ensure(0x1A, 0, params); entries.add(StopScriptEntry()) }
                            0x1B -> { ensure(0x1B, 5, params); entries.add(RunScriptDR2Entry(params[0], params[1], params[2], params[3], params[4])) }
                            0x1C -> { ensure(0x1C, 0, params); entries.add(UnknownEntry(0x1C, IntArray(0))) }
                            0x1E -> { ensure(0x1E, 5, params); entries.add(SpriteEntry(params[0], params[1], params[2], params[3], params[4])) }
                            0x1F -> { ensure(0x1F, 7, params); entries.add(UnknownEntry(0x1F, params)) }
                            0x20 -> { ensure(0x20, 5, params); entries.add(UnknownEntry(0x20, params)) }
                            0x21 -> { ensure(0x21, 1, params); entries.add(SpeakerEntry(params[0])) }
                            0x22 -> { ensure(0x22, 3, params); entries.add(UnknownEntry(0x22, params)) }
                            0x23 -> { ensure(0x23, 5, params); entries.add(UnknownEntry(0x23, params)) }
                            0x25 -> { ensure(0x25, 2, params); entries.add(ChangeUIEntry(params[0], params[1])) }
                            0x26 -> { ensure(0x26, 3, params); entries.add(SetFlagEntry(params[0], params[1], params[2])) }
                            0x27 -> { ensure(0x27, 1, params); entries.add(CheckCharacterEntry(params[0])) }
                            0x29 -> { ensure(0x29, 1, params); entries.add(CheckObjectEntry(params[0])) }
                            0x2A -> { ensure(0x2A, 2, params); entries.add(SetLabelEntry(params[0], params[1])) }
                            0x2B -> { ensure(0x2B, 1, params); entries.add(ChoiceEntry(params[0])) }
                            0x2C -> { ensure(0x2C, 2, params); entries.add(UnknownEntry(0x2C, params)) }
                            0x2E -> { ensure(0x2E, 5, params); entries.add(UnknownEntry(0x2E, params)) }
                            0x2F -> { ensure(0x2F, 10, params); entries.add(UnknownEntry(0x2F, params)) }
                            0x30 -> { ensure(0x30, 3, params); entries.add(ShowBackgroundEntry(params[0], params[1], params[2])) }
                            0x32 -> { ensure(0x32, 1, params); entries.add(UnknownEntry(0x32, params)) }
                            0x33 -> { ensure(0x33, 4, params); entries.add(UnknownEntry(0x33, params)) }
                            0x34 -> { ensure(0x34, 2, params); entries.add(GoToLabelEntry(params[0], params[1])) }
                            0x35 -> entries.add(CheckFlagEntryA(params))
                            0x36 -> entries.add(CheckFlagEntryB(params))
                            0x39 -> { ensure(0x39, 5, params); entries.add(UnknownEntry(0x39, params)) }
                            0x3A -> { ensure(0x3A, 4, params); entries.add(WaitForInputDR1Entry(params[0], params[1], params[2], params[3])) }
                            0x3B -> { ensure(0x3B, 2, params); entries.add(WaitFrameDR1Entry(params[0], params[1])) }
                            0x3C -> { ensure(0x3C, 0, params); entries.add(EndFlagCheckEntry()) }
                            0x4B -> { ensure(0x4B, 0, params); entries.add(WaitForInputEntry()) }
                            0x4C -> { ensure(0x4C, 0, params); entries.add(WaitFrameEntry()) }

                            else -> entries.add(UnknownEntry(opCode, params))
                        }
                    }
                }
            }
            lin.close()
        } catch(illegal: IllegalArgumentException) {
            lin.close()
            throw illegal
        }
    }

    fun ensure(opCode: Int, required: Int, params: IntArray): Unit = if(params.size != required) throw IllegalArgumentException("Malformed LIN entry - 0x${opCode.toString(16)} (Expected $required, got ${params.size})") else Unit
}