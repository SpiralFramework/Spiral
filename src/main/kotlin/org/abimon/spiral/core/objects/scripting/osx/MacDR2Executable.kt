package org.abimon.spiral.core.objects.scripting.osx

import org.abimon.spiral.core.objects.scripting.DRExecutable
import org.abimon.spiral.core.utils.readString
import org.abimon.spiral.core.utils.readZeroString
import org.abimon.spiral.core.utils.trimToBytes
import java.io.File

class MacDR2Executable(file: File): DRExecutable(file) {
    companion object {
        val DATA_OFFSET = 0x3C6280L
        val PATCH_OFFSET = DATA_OFFSET + 21
        val KEYBOARD_OFFSET = PATCH_OFFSET + 22

        val DATA_OFFSET_2 = 0x3C63A5L

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

        val PAK_MAPPING = mapOf(
                "bg_000" to (0x3D2930L to 7),
                "bg_001" to (0x3D2A00L to 62),
                "bg_002" to (0x3D30B8L to 55),
                "bg_003" to (0x3D3660L to 48),
                "bg_004" to (0x3D3B98L to 35),
                "bg_005" to (0x3D3F30L to 50),
                "bg_006" to (0x3D4438L to 93),
                "bg_007" to (0x3D4D70L to 86),
                "bg_008" to (0x3D5618L to 46),
                "bg_009" to (0x3D5B38L to 51),
                "bg_011" to (0x3D6110L to 44),
                "bg_012" to (0x3D65E8L to 24),
                "bg_019" to (0x3D6898L to 82),
                "bg_020" to (0x3D7128L to 79),
                "bg_021" to (0x3D79E0L to 49),
                "bg_022" to (0x3D7F00L to 85),
                "bg_023" to (0x3D88A0L to 63),
                "bg_024" to (0x3D8FC0L to 43),
                "bg_025" to (0x3D93E0L to 81),
                "bg_026" to (0x3D9C60L to 73),
                "bg_028" to (0x3DA430L to 42),
                "bg_029" to (0x3DA838L to 74),
                "bg_030" to (0x3DAEB8L to 56),
                "bg_032" to (0x3DB4E8L to 86),
                "bg_040" to (0x3DBE58L to 66),
                "bg_041" to (0x3DC538L to 80),
                "bg_042" to (0x3DCD88L to 51),
                "bg_043" to (0x3DD2D0L to 23),
                "bg_044" to (0x3DD560L to 47),
                "bg_045" to (0x3DDA30L to 43),
                "bg_046" to (0x3DDF00L to 52),
                "bg_047" to (0x3DE448L to 105),
                "bg_048" to (0x3DEE50L to 63),
                "bg_053" to (0x3DF470L to 108),
                "bg_054" to (0x3DFE08L to 108),
                "bg_055" to (0x3E0798L to 56),
                "bg_056" to (0x3E0DC8L to 55),
                "bg_059" to (0x3E1370L to 38),
                "bg_060" to (0x3E17A8L to 31),
                "bg_061" to (0x3E1B20L to 33),
                "bg_062" to (0x3E1ED0L to 30),
                "bg_063" to (0x3E2228L to 34),
                "bg_064" to (0x3E25F8L to 38),
                "bg_070" to (0x3E2A48L to 51),
                "bg_071" to (0x3E2FC0L to 80),
                "bg_072" to (0x3E37F8L to 33),
                "bg_073" to (0x3E3BC0L to 33),
                "bg_074" to (0x3E3F90L to 34),
                "bg_075" to (0x3E4368L to 27),
                "bg_076" to (0x3E4680L to 27),
                "bg_079" to (0x3E4990L to 74),
                "bg_080" to (0x3E5108L to 32),
                "bg_081" to (0x3E54B8L to 32),
                "bg_082" to (0x3E5868L to 29),
                "bg_083" to (0x3E5BB0L to 27),
                "bg_084" to (0x3E5EC0L to 27),
                "bg_086" to (0x3E61D0L to 35),
                "bg_087" to (0x3E65C0L to 29),
                "bg_089" to (0x3E68C0L to 18),
                "bg_090" to (0x3E6AA8L to 15),
                "bg_091" to (0x3E6C40L to 31),
                "bg_093" to (0x3E6F80L to 22),
                "bg_096" to (0x3E71D8L to 80),
                "bg_097" to (0x3E79A8L to 44),
                "bg_100" to (0x3E7E58L to 72),
                "bg_101" to (0x3E85C8L to 41),
                "bg_102" to (0x3E8A50L to 66),
                "bg_104" to (0x3E9138L to 90),
                "bg_105" to (0x3E9A38L to 76),
                "bg_106" to (0x3EA188L to 62),
                "bg_107" to (0x3EA778L to 61),
                "bg_108" to (0x3EAD40L to 70),
                "bg_109" to (0x3EB3D8L to 78),
                "bg_110" to (0x3EBAE8L to 49),
                "bg_111" to (0x3EBFB0L to 57),
                "bg_112" to (0x3EC510L to 61),
                "bg_113" to (0x3ECAB0L to 68),
                "bg_114" to (0x3ED128L to 61),
                "bg_115" to (0x3ED6F0L to 51),
                "bg_116" to (0x3EDBE0L to 66),
                "bg_117" to (0x3EE238L to 59),
                "bg_118" to (0x3EE7D0L to 63),
                "bg_119" to (0x3EEDD0L to 63),
                "bg_120" to (0x3EF3C0L to 51),
                "bg_130" to (0x3EF970L to 56),
                "bg_131" to (0x3EFF88L to 30),
                "bg_132" to (0x3F02C8L to 58),
                "bg_133" to (0x3F0918L to 63),
                "bg_134" to (0x3F0FD0L to 30),
                "bg_135" to (0x3F1338L to 53),
                "bg_140" to (0x3F18D0L to 78),
                "bg_141" to (0x3F2158L to 80),
                "bg_142" to (0x3F2A18L to 50),
                "bg_143" to (0x3F2F68L to 69),
                "bg_144" to (0x3F36C0L to 43),
                "bg_145" to (0x3F3B30L to 55),
                "bg_150" to (0x3F40E0L to 41),
                "bg_160" to (0x3F4520L to 71),
                "bg_161" to (0x3F4CE0L to 57),
                "bg_162" to (0x3F52C0L to 52),
                "bg_163" to (0x3F57F8L to 58),
                "bg_164" to (0x3F5DE8L to 65),
                "bg_165" to (0x3F64A0L to 25),
                "bg_166" to (0x3F6780L to 44),
                "bg_167" to (0x3F6C48L to 64),
                "bg_168" to (0x3F7378L to 57),
                "bg_169" to (0x3F79C0L to 25),
                "bg_170" to (0x3F7C90L to 43),
                "bg_171" to (0x3F8130L to 59),
                "bg_172" to (0x3F8780L to 33),
                "bg_180" to (0x3F8B40L to 33),
                "bg_200" to (0x3F8EE0L to 29),
                "bg_201" to (0x3F9210L to 42),
                "bg_202" to (0x3F96A8L to 26),
                "bg_203" to (0x3F9988L to 29),
                "bg_204" to (0x3F9CB0L to 34),
                "bg_205" to (0x3FA058L to 16),
                "bg_206" to (0x3FA228L to 25),
                "bg_207" to (0x3FA4F0L to 39),
                "bg_208" to (0x3FA940L to 22),
                "bg_209" to (0x3FABB8L to 25),
                "bg_210" to (0x3FAE80L to 32),
                "bg_211" to (0x3FB1F8L to 14),
                "bg_220" to (0x3FB388L to 21),
                "bg_221" to (0x3FB5D0L to 23),
                "bg_222" to (0x3FB850L to 35),
                "bg_223" to (0x3FBC00L to 25),
                "bg_250" to (0x3FBED0L to 19),
                "bg_251" to (0x3FC080L to 20),
                "bg_252" to (0x3FC248L to 25),
                "bg_253" to (0x3FC470L to 25),
                "bg_254" to (0x3FC6A0L to 23),
                "bg_255" to (0x3FC8B0L to 13),
                "bg_256" to (0x3FC9F0L to 18),
                "bg_259" to (0x3FCB98L to 46),
                "bg_260" to (0x3FD098L to 49),
                "bg_261" to (0x3FD5D0L to 42),
                "bg_262" to (0x3FDA48L to 40),
                "bg_263" to (0x3FDE88L to 45),
                "bg_264" to (0x3FE350L to 40),
                "bg_265" to (0x3FE788L to 33),
                "bg_266" to (0x3FEB20L to 48),
                "bg_267" to (0x3FF018L to 42),
                "bg_268" to (0x3FF488L to 44),
                "bg_269" to (0x3FF918L to 41),
                "bg_270" to (0x3FFD50L to 39),
                "bg_271" to (0x400140L to 47),
                "bg_272" to (0x400630L to 48),
                "bg_273" to (0x400B38L to 49),
                "bg_274" to (0x401090L to 35),
                "bg_275" to (0x401440L to 28),
                "bg_276" to (0x401748L to 27),
                "bg_277" to (0x401A30L to 17),
                "bg_900" to (0x401C10L to 23),
                "bg_901" to (0x401E50L to 23),
                "bg_902" to (0x402090L to 23),
                "bg_903" to (0x4022F0L to 16),
                "bg_904" to (0x4024A8L to 43),
                "bg_905" to (0x402930L to 7),
                "bg_906" to (0x402A00L to 21)
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
    }

    fun save() {
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
    }

    fun reset() {
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