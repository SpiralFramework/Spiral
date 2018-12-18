package org.abimon.spiral.core.objects.scripting.osx

import org.abimon.spiral.core.objects.scripting.DRExecutable
import org.abimon.spiral.core.utils.readString
import org.abimon.spiral.core.utils.readZeroString
import org.abimon.spiral.core.utils.trimToBytes
import java.io.File
import java.util.*

class MacDR1Executable(file: File): DRExecutable(file) {
    companion object {
        val DATA_OFFSET = 0x35C901L
        val PATCH_OFFSET = DATA_OFFSET + 21
        val KEYBOARD_OFFSET = PATCH_OFFSET + 22

        val DATA_OFFSET_2 = 0x35CA26L

        val DATA_LOCALISATION_OFFSET = DATA_OFFSET_2 + 13
        val KEYBOARD_LOCALISATION_OFFSET = DATA_LOCALISATION_OFFSET + 9

        val LANGUAGE_ONE_DATA_OFFSET = KEYBOARD_LOCALISATION_OFFSET + 18
        val LANGUAGE_ONE_KEYBOARD_OFFSET = LANGUAGE_ONE_DATA_OFFSET + 8

        val DATA_LENGTH = 12
        val PATCH_LENGTH = 13
        val KEYBOARD_LENGTH = 21

        val DATA_LOCALISATION_LENGTH = 8
        val KEYBOARD_LOCALISATION_LENGTH = 17

        val LANGUAGE_ONE_DATA_LENGTH = 7
        val LANGUAGE_ONE_KEYBOARD_LENGTH = 7

        val MAXIMUM_CHAPTER = 8
        val MAXIMUM_CHARACTER = 33

        val VOICE_LINE_OFFSET = 2748610L
        val VOICE_LINE_ARRAY_LENGTH = ((MAXIMUM_CHARACTER + MAXIMUM_CHAPTER + (MAXIMUM_CHARACTER * MAXIMUM_CHAPTER)) * 2) + 1

        val PAK_MAPPING = mapOf(
                "bg_000" to (0x35F748L to 6),
                "bg_001" to (0x35F7F8L to 58),
                "bg_002" to (0x35FE38L to 17),
                "bg_003" to (0x35FFF0L to 32),
                "bg_004" to (0x360318L to 48),
                "bg_005" to (0x360808L to 33),
                "bg_006" to (0x360B80L to 65),
                "bg_007" to (0x3611E0L to 55),
                "bg_009" to (0x361790L to 44),
                "bg_010" to (0x361BE8L to 65),
                "bg_011" to (0x362330L to 53),
                "bg_014" to (0x3628C0L to 43),
                "bg_015" to (0x362D20L to 50),
                "bg_016" to (0x363238L to 42),
                "bg_017" to (0x3636E8L to 37),
                "bg_018" to (0x363B20L to 46),
                "bg_021" to (0x364018L to 52),
                "bg_022" to (0x3645A8L to 47),
                "bg_025" to (0x364A50L to 41),
                "bg_026" to (0x364E60L to 52),
                "bg_027" to (0x3653E8L to 44),
                "bg_028" to (0x365858L to 48),
                "bg_029" to (0x365D48L to 50),
                "bg_030" to (0x366258L to 56),
                "bg_031" to (0x3667B8L to 49),
                "bg_032" to (0x366CC0L to 49),
                "bg_033" to (0x3671C0L to 51),
                "bg_034" to (0x367760L to 37),
                "bg_035" to (0x367B90L to 28),
                "bg_036" to (0x367EC8L to 32),
                "bg_037" to (0x3681F8L to 39),
                "bg_041" to (0x3685E0L to 44),
                "bg_042" to (0x368A88L to 41),
                "bg_043" to (0x368EA0L to 49),
                "bg_044" to (0x369380L to 37),
                "bg_045" to (0x369750L to 14),
                "bg_046" to (0x3698A8L to 66),
                "bg_047" to (0x369F58L to 92),
                "bg_048" to (0x36A908L to 72),
                "bg_049" to (0x36B0A8L to 19),
                "bg_050" to (0x36B280L to 17),
                "bg_051" to (0x36B430L to 49),
                "bg_052" to (0x36B930L to 49),
                "bg_061" to (0x36BE30L to 45),
                "bg_062" to (0x36C300L to 12),
                "bg_063" to (0x36C438L to 53),
                "bg_064" to (0x36C960L to 23),
                "bg_065" to (0x36CBB0L to 42),
                "bg_066" to (0x36CFF8L to 68),
                "bg_067" to (0x36D708L to 45),
                "bg_068" to (0x36DBB0L to 40),
                "bg_069" to (0x36DFE8L to 30),
                "bg_070" to (0x36E318L to 49),
                "bg_071" to (0x36E820L to 49),
                "bg_072" to (0x36ED20L to 42),
                "bg_073" to (0x36F1C8L to 37),
                "bg_081" to (0x36F600L to 35),
                "bg_083" to (0x36F9C0L to 79),
                "bg_084" to (0x370270L to 16),
                "bg_085" to (0x370448L to 26),
                "bg_086" to (0x370758L to 24),
                "bg_087" to (0x3709F8L to 58),
                "bg_088" to (0x371018L to 15),
                "bg_089" to (0x3711C0L to 40),
                "bg_090" to (0x3715D8L to 49),
                "bg_091" to (0x371AE0L to 49),
                "bg_092" to (0x371FE0L to 44),
                "bg_101" to (0x372478L to 59),
                "bg_103" to (0x372AD0L to 51),
                "bg_104" to (0x373010L to 55),
                "bg_105" to (0x3735C0L to 28),
                "bg_106" to (0x3738A8L to 33),
                "bg_107" to (0x373C10L to 49),
                "bg_109" to (0x374110L to 52),
                "bg_111" to (0x374688L to 41),
                "bg_113" to (0x374AD0L to 46),
                "bg_115" to (0x374F88L to 59),
                "bg_117" to (0x3755B0L to 53),
                "bg_118" to (0x375B20L to 28),
                "bg_119" to (0x375E08L to 38),
                "bg_120" to (0x3761F8L to 28),
                "bg_121" to (0x3764D8L to 38),
                "bg_122" to (0x3768B8L to 28),
                "bg_123" to (0x376B98L to 54),
                "bg_125" to (0x377158L to 47),
                "bg_127" to (0x377630L to 48),
                "bg_129" to (0x377B38L to 63),
                "bg_131" to (0x3781E0L to 33),
                "bg_133" to (0x378550L to 49),
                "bg_135" to (0x378A70L to 62),
                "bg_136" to (0x3790B8L to 52),
                "bg_137" to (0x379628L to 50),
                "bg_138" to (0x379B48L to 47),
                "bg_139" to (0x37A020L to 45),
                "bg_140" to (0x37A4B0L to 44),
                "bg_141" to (0x37A938L to 50),
                "bg_142" to (0x37AE78L to 31),
                "bg_144" to (0x37B1C0L to 40),
                "bg_145" to (0x37B5B8L to 27),
                "bg_146" to (0x37B880L to 36),
                "bg_147" to (0x37BC18L to 32),
                "bg_148" to (0x37BF48L to 47),
                "bg_149" to (0x37C440L to 43),
                "bg_150" to (0x37C8C0L to 28),
                "bg_151" to (0x37CBA8L to 36),
                "bg_152" to (0x37CF28L to 41),
                "bg_153" to (0x37D350L to 36),
                "bg_154" to (0x37D6E8L to 32),
                "bg_156" to (0x37DA18L to 41),
                "bg_157" to (0x37DE70L to 21),
                "bg_158" to (0x37E0A0L to 14),
                "bg_159" to (0x37E218L to 30),
                "bg_160" to (0x37E528L to 53),
                "bg_201" to (0x37EAF0L to 38),
                "bg_203" to (0x37EEC8L to 20),
                "bg_204" to (0x37F0E8L to 24),
                "bg_205" to (0x37F368L to 20),
                "bg_206" to (0x37F588L to 25),
                "bg_207" to (0x37F830L to 23),
                "bg_208" to (0x37FAA0L to 24),
                "bg_216" to (0x37FD28L to 45),
                "bg_217" to (0x3801B0L to 29),
                "bg_218" to (0x3804C0L to 23),
                "bg_219" to (0x380720L to 23),
                "bg_220" to (0x380980L to 23),
                "bg_221" to (0x380BE0L to 22),
                "bg_222" to (0x380E28L to 22),
                "bg_244" to (0x381078L to 56),
                "bg_248" to (0x381648L to 58),
                "bg_900" to (0x381C88L to 21),
                "bg_901" to (0x381EC0L to 56),
                "bg_902" to (0x382448L to 40)
        )

        val ARCHIVE_LOCATIONS = arrayOf(
                0x2E2F65L to 16,
                0x3578B6L to 30,
                0x3578D5L to 25,
                0x3578EFL to 25,
                0x3579B7L to 31,
                0x3579D7L to 26,
                0x3579F2L to 26,
                0x357A0DL to 31,
                0x357A2DL to 26,
                0x357A48L to 26,
                0x359D9EL to 8,
                0x35C9CFL to 15,
                0x35C9EFL to 27,
                0x385C28L to 8,
                0x385C4BL to 8,
                0x386C31L to 17,
                0x386C43L to 12,
                0x386C50L to 12,
                0x38F248L to 8,
                0x38F26BL to 8
        )
    }

    var dataWadName: String
    var patchWadName: String
    var keyboardWadName: String

    var dataWadName2: String

    var localisedWadBase: String
    var localisedWadKeyboard: String

    var languageOneDataExtension: String
    var languageOneKeyboardExtension: String

    override val pakNames: Map<String, Array<String>>
    override val maximumChapter = MAXIMUM_CHAPTER

    val voiceLineOffset: Long
    override val voiceLineArray: IntArray

    val archiveNames: Array<String>

    init {
        raf.seek(DATA_OFFSET)
        dataWadName = raf.readString(DATA_LENGTH)

        raf.seek(PATCH_OFFSET)
        patchWadName = raf.readString(PATCH_LENGTH)

        raf.seek(KEYBOARD_OFFSET)
        keyboardWadName = raf.readString(KEYBOARD_LENGTH)

        raf.seek(DATA_OFFSET_2)
        dataWadName2 = raf.readString(DATA_LENGTH)

        raf.seek(DATA_LOCALISATION_OFFSET)
        localisedWadBase = raf.readString(DATA_LOCALISATION_LENGTH)

        raf.seek(KEYBOARD_LOCALISATION_OFFSET)
        localisedWadKeyboard = raf.readString(KEYBOARD_LOCALISATION_LENGTH)

        raf.seek(LANGUAGE_ONE_DATA_OFFSET)
        languageOneDataExtension = raf.readString(LANGUAGE_ONE_DATA_LENGTH)

        raf.seek(LANGUAGE_ONE_KEYBOARD_OFFSET)
        languageOneKeyboardExtension = raf.readString(LANGUAGE_ONE_KEYBOARD_LENGTH)

        val mappings = HashMap<String, Array<String>>()

        PAK_MAPPING.forEach { filename, (offset, numFiles) ->
            raf.seek(offset)
            mappings[filename] = Array(numFiles) { raf.readZeroString() }
        }

        val initialOffset = PAK_MAPPING["bg_000"]!!.first
        raf.seek(initialOffset)

        pakNames = mappings

        raf.seek(VOICE_LINE_OFFSET)
        val originalOffsetArray = IntArray(VOICE_LINE_ARRAY_LENGTH) { (raf.read() or (raf.read() shl 8)) }

        if (originalOffsetArray[0] != 0xFFFF) {
            System.err.println("Error: Reading from $file at $VOICE_LINE_OFFSET for ${VOICE_LINE_ARRAY_LENGTH * 2} resulted in the first number being ${originalOffsetArray[0]}. This may be the result of an earlier game version, or a different compiler option.\nSearching for the correct offset now...")

            voiceLineOffset = -1
            voiceLineArray = intArrayOf()
        } else {
            if (originalOffsetArray[1] != 0x01)
                System.err.println("Warning: Reading from $file at $VOICE_LINE_OFFSET for ${VOICE_LINE_ARRAY_LENGTH * 2} resulted in the first two numbers being ${originalOffsetArray[0]} and ${originalOffsetArray[1]}\nThis is likely an issue.")

            voiceLineOffset = VOICE_LINE_OFFSET
            voiceLineArray = originalOffsetArray
        }

        archiveNames = ARCHIVE_LOCATIONS.map { (offset, size) ->
            raf.seek(offset)
            raf.readZeroString(size)
        }.toTypedArray()
    }

    override fun save() {
        raf.seek(DATA_OFFSET)
        raf.write(dataWadName.trimToBytes(DATA_LENGTH))

        raf.seek(PATCH_OFFSET)
        raf.write(patchWadName.trimToBytes(PATCH_LENGTH))

        raf.seek(KEYBOARD_OFFSET)
        raf.write(keyboardWadName.trimToBytes(KEYBOARD_LENGTH))

        raf.seek(DATA_OFFSET_2)
        raf.write(dataWadName.trimToBytes(DATA_LENGTH))

        raf.seek(DATA_LOCALISATION_OFFSET)
        raf.write(localisedWadBase.trimToBytes(DATA_LOCALISATION_LENGTH))

        raf.seek(KEYBOARD_LOCALISATION_OFFSET)
        raf.write(localisedWadKeyboard.trimToBytes(KEYBOARD_LOCALISATION_LENGTH))

        raf.seek(LANGUAGE_ONE_DATA_OFFSET)
        raf.write(languageOneDataExtension.trimToBytes(LANGUAGE_ONE_DATA_LENGTH))

        raf.seek(LANGUAGE_ONE_KEYBOARD_OFFSET)
        raf.write(languageOneKeyboardExtension.trimToBytes(LANGUAGE_ONE_KEYBOARD_LENGTH))

        ARCHIVE_LOCATIONS.forEachIndexed { i, (offset, size) ->

            val array = ByteArray(size)
            val src = archiveNames[i].toByteArray()
            System.arraycopy(src, 0, array, 0, src.size.coerceAtMost(size))

            raf.seek(offset)
            raf.write(array)
        }
    }

    override fun reset() {
        dataWadName = "dr1_data.wad"
        patchWadName = "dr1_patch.wad"
        keyboardWadName = "dr1_data_keyboard.wad"
        dataWadName2 = "dr1_data.wad"
        localisedWadBase = "dr1_data"
        localisedWadKeyboard = "dr1_data_keyboard"
        languageOneDataExtension = "_us.wad"
        languageOneKeyboardExtension = "_us.wad"
    }
}