package info.spiralframework.formats.common.audio

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localisedNotEnoughData
import info.spiralframework.formats.common.withFormats
import org.abimon.kornea.errors.common.KorneaResult
import org.abimon.kornea.errors.common.cast
import org.abimon.kornea.errors.common.getOrBreak
import org.abimon.kornea.errors.common.flatMap
import org.abimon.kornea.io.common.*
import org.kornea.toolkit.common.SemanticVersion
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.or
import kotlin.math.min

@ExperimentalUnsignedTypes
data class HighCompressionAudio(val version: SemanticVersion, val audioChannels: Array<HcaAudioChannel>, val hfrGroupCount: Int, val headerSize: Int, val sampleRate: Int, val channelCount: Int, val blockSize: Int, val blockCount: Int, val encoderDelay: Int, val encoderPadding: Int, val loopEnabled: Boolean, val loopStartBlock: Int?, val loopEndBlock: Int?, val loopStartDelay: Int?, val loopEndPadding: Int?, val samplesPerBlock: Int, val audioInfo: HcaAudioInfo, val athInfo: HcaAbsoluteThresholdHearingInfo?, val cipherInfo: HcaCipherInfo?, val comment: String?, val encryptionEnabled: Boolean, val dataSource: DataSource<*>) {
    companion object {
        @ExperimentalUnsignedTypes
        public suspend inline fun <T : DataCloseable, R> use(t: T, block: suspend () -> R): R {
            contract {
                callsInPlace(block, InvocationKind.EXACTLY_ONCE)
            }

            var exception: Throwable? = null
            try {
                return block()
            } catch (e: Throwable) {
                exception = e
                throw e
            } finally {
//                println("Hewwo")
                t.close()
//                t.closeFinally(exception)
            }

            throw IllegalStateException()
        }

        const val INVALID_MAGIC = 0x0000
        const val INVALID_HEADER_SIZE = 0x0001
        const val INVALID_CHECKSUM = 0x0002

        const val INVALID_FORMAT_MAGIC = 0x0010
        const val INVALID_CHANNEL_COUNT = 0x0011
        const val INVALID_SAMPLE_RATE = 0x0012
        const val INVALID_FRAME_COUNT = 0x0013
        const val INVALID_POSITION = 0x0014

        const val INVALID_INFO_MAGIC = 0x0020

        const val INVALID_LOOP_FRAMES = 0x0030

        const val INVALID_CIPHER_TYPE = 0x0040

        const val NOT_ENOUGH_DATA_KEY = "formats.hca.not_enough_data"
        const val INVALID_MAGIC_KEY = "formats.hca.invalid_magic"
        const val INVALID_HEADER_SIZE_KEY = "formats.hca.invalid_header_size"
        const val INVALID_CHECKSUM_KEY = "formats.hca.invalid_checksum"

        const val INVALID_FORMAT_MAGIC_KEY = "formats.hca.invalid_format_magic"
        const val INVALID_CHANNEL_COUNT_KEY = "formats.hca.invalid_channel_count"
        const val INVALID_SAMPLE_RATE_KEY = "formats.hca.invalid_sample_rate"
        const val INVALID_FRAME_COUNT_KEY = "formats.hca.invalid_frame_count"
        const val INVALID_POSITION_KEY = "formats.hca.invalid_position"

        const val INVALID_INFO_MAGIC_KEY = "formats.hca.invalid_info_magic"

        const val INVALID_LOOP_FRAMES_KEY = "formats.hca.invalid_loop_frames"

        const val INVALID_CIPHER_TYPE_KEY = "formats.hca.invalid_cipher_type"

        /** HCA. */
        const val MAGIC_NUMBER_BE = 0x48434100

        /** 'fmt.' */
        const val FORMAT_MAGIC_NUMBER_BE = 0x666D7400

        /** 'comp' */
        const val COMPRESSION_MAGIC_NUMBER_BE = 0x636F6D70

        /** 'dec.' */
        const val DECODE_MAGIC_NUMBER_BE = 0x64656300

        const val VBR_MAGIC_NUMBER_BE = 0x76627200
        const val ATH_MAGIC_NUMBER_BE = 0x61746800
        const val RVA_MAGIC_NUMBER_BE = 0x72766100
        const val LOOP_MAGIC_NUMBER_BE = 0x6C6F6F70
        const val CIPHER_MAGIC_NUMBER_BE = 0x63697068
        const val COMMENT_MAGIC_NUMBER_BE = 0x636F6D6D
        const val PADDING_MAGIC_NUMBER_BE = 0x70616400

        const val HCA_MASK = 0x7F7F7F7F
        const val HCA_SUBFRAMES_PER_FRAME = 8
        const val HCA_SAMPLES_PER_SUBFRAME = 128
        const val HCA_SAMPLES_PER_FRAME = HCA_SUBFRAMES_PER_FRAME * HCA_SAMPLES_PER_SUBFRAME
        const val HCA_MDCT_BITS = 7 /* (1<<7) = 128 */

        const val CHANNEL_DISCRETE = 0
        const val CHANNEL_STEREO_PRIMARY = 1
        const val CHANNEL_STEREO_SECONDARY = 2

        val CRC16_LOOKUP_TABLE = intArrayOf(
                0x0000, 0x8005, 0x800F, 0x000A, 0x801B, 0x001E, 0x0014, 0x8011, 0x8033, 0x0036, 0x003C, 0x8039, 0x0028, 0x802D, 0x8027, 0x0022,
                0x8063, 0x0066, 0x006C, 0x8069, 0x0078, 0x807D, 0x8077, 0x0072, 0x0050, 0x8055, 0x805F, 0x005A, 0x804B, 0x004E, 0x0044, 0x8041,
                0x80C3, 0x00C6, 0x00CC, 0x80C9, 0x00D8, 0x80DD, 0x80D7, 0x00D2, 0x00F0, 0x80F5, 0x80FF, 0x00FA, 0x80EB, 0x00EE, 0x00E4, 0x80E1,
                0x00A0, 0x80A5, 0x80AF, 0x00AA, 0x80BB, 0x00BE, 0x00B4, 0x80B1, 0x8093, 0x0096, 0x009C, 0x8099, 0x0088, 0x808D, 0x8087, 0x0082,
                0x8183, 0x0186, 0x018C, 0x8189, 0x0198, 0x819D, 0x8197, 0x0192, 0x01B0, 0x81B5, 0x81BF, 0x01BA, 0x81AB, 0x01AE, 0x01A4, 0x81A1,
                0x01E0, 0x81E5, 0x81EF, 0x01EA, 0x81FB, 0x01FE, 0x01F4, 0x81F1, 0x81D3, 0x01D6, 0x01DC, 0x81D9, 0x01C8, 0x81CD, 0x81C7, 0x01C2,
                0x0140, 0x8145, 0x814F, 0x014A, 0x815B, 0x015E, 0x0154, 0x8151, 0x8173, 0x0176, 0x017C, 0x8179, 0x0168, 0x816D, 0x8167, 0x0162,
                0x8123, 0x0126, 0x012C, 0x8129, 0x0138, 0x813D, 0x8137, 0x0132, 0x0110, 0x8115, 0x811F, 0x011A, 0x810B, 0x010E, 0x0104, 0x8101,
                0x8303, 0x0306, 0x030C, 0x8309, 0x0318, 0x831D, 0x8317, 0x0312, 0x0330, 0x8335, 0x833F, 0x033A, 0x832B, 0x032E, 0x0324, 0x8321,
                0x0360, 0x8365, 0x836F, 0x036A, 0x837B, 0x037E, 0x0374, 0x8371, 0x8353, 0x0356, 0x035C, 0x8359, 0x0348, 0x834D, 0x8347, 0x0342,
                0x03C0, 0x83C5, 0x83CF, 0x03CA, 0x83DB, 0x03DE, 0x03D4, 0x83D1, 0x83F3, 0x03F6, 0x03FC, 0x83F9, 0x03E8, 0x83ED, 0x83E7, 0x03E2,
                0x83A3, 0x03A6, 0x03AC, 0x83A9, 0x03B8, 0x83BD, 0x83B7, 0x03B2, 0x0390, 0x8395, 0x839F, 0x039A, 0x838B, 0x038E, 0x0384, 0x8381,
                0x0280, 0x8285, 0x828F, 0x028A, 0x829B, 0x029E, 0x0294, 0x8291, 0x82B3, 0x02B6, 0x02BC, 0x82B9, 0x02A8, 0x82AD, 0x82A7, 0x02A2,
                0x82E3, 0x02E6, 0x02EC, 0x82E9, 0x02F8, 0x82FD, 0x82F7, 0x02F2, 0x02D0, 0x82D5, 0x82DF, 0x02DA, 0x82CB, 0x02CE, 0x02C4, 0x82C1,
                0x8243, 0x0246, 0x024C, 0x8249, 0x0258, 0x825D, 0x8257, 0x0252, 0x0270, 0x8275, 0x827F, 0x027A, 0x826B, 0x026E, 0x0264, 0x8261,
                0x0220, 0x8225, 0x822F, 0x022A, 0x823B, 0x023E, 0x0234, 0x8231, 0x8213, 0x0216, 0x021C, 0x8219, 0x0208, 0x820D, 0x8207, 0x0202
        )

        //--------------------------------------------------
        // ATH
        //--------------------------------------------------
        /* Base ATH (Absolute Threshold of Hearing) curve (for 41856hz).
         * May be a slight modification of the standard Painter & Spanias ATH curve formula.
         */
        val ATH_BASE_CURVE = intArrayOf(
                0x78, 0x5F, 0x56, 0x51, 0x4E, 0x4C, 0x4B, 0x49, 0x48, 0x48, 0x47, 0x46, 0x46, 0x45, 0x45, 0x45,
                0x44, 0x44, 0x44, 0x44, 0x43, 0x43, 0x43, 0x43, 0x43, 0x43, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42,
                0x42, 0x42, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x40, 0x40, 0x40, 0x40,
                0x40, 0x40, 0x40, 0x40, 0x40, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
                0x3F, 0x3F, 0x3F, 0x3E, 0x3E, 0x3E, 0x3E, 0x3E, 0x3E, 0x3D, 0x3D, 0x3D, 0x3D, 0x3D, 0x3D, 0x3D,
                0x3C, 0x3C, 0x3C, 0x3C, 0x3C, 0x3C, 0x3C, 0x3C, 0x3B, 0x3B, 0x3B, 0x3B, 0x3B, 0x3B, 0x3B, 0x3B,
                0x3B, 0x3B, 0x3B, 0x3B, 0x3B, 0x3B, 0x3B, 0x3B, 0x3B, 0x3B, 0x3B, 0x3B, 0x3B, 0x3B, 0x3B, 0x3B,
                0x3B, 0x3B, 0x3B, 0x3B, 0x3B, 0x3B, 0x3B, 0x3B, 0x3C, 0x3C, 0x3C, 0x3C, 0x3C, 0x3C, 0x3C, 0x3C,
                0x3D, 0x3D, 0x3D, 0x3D, 0x3D, 0x3D, 0x3D, 0x3D, 0x3E, 0x3E, 0x3E, 0x3E, 0x3E, 0x3E, 0x3E, 0x3F,
                0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F, 0x3F,
                0x3F, 0x3F, 0x3F, 0x3F, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40,
                0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41,
                0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41,
                0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x41, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42,
                0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x42, 0x43, 0x43, 0x43,
                0x43, 0x43, 0x43, 0x43, 0x43, 0x43, 0x43, 0x43, 0x43, 0x43, 0x43, 0x43, 0x43, 0x43, 0x44, 0x44,
                0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x45, 0x45, 0x45, 0x45,
                0x45, 0x45, 0x45, 0x45, 0x45, 0x45, 0x45, 0x45, 0x46, 0x46, 0x46, 0x46, 0x46, 0x46, 0x46, 0x46,
                0x46, 0x46, 0x47, 0x47, 0x47, 0x47, 0x47, 0x47, 0x47, 0x47, 0x47, 0x47, 0x48, 0x48, 0x48, 0x48,
                0x48, 0x48, 0x48, 0x48, 0x49, 0x49, 0x49, 0x49, 0x49, 0x49, 0x49, 0x49, 0x4A, 0x4A, 0x4A, 0x4A,
                0x4A, 0x4A, 0x4A, 0x4A, 0x4B, 0x4B, 0x4B, 0x4B, 0x4B, 0x4B, 0x4B, 0x4C, 0x4C, 0x4C, 0x4C, 0x4C,
                0x4C, 0x4D, 0x4D, 0x4D, 0x4D, 0x4D, 0x4D, 0x4E, 0x4E, 0x4E, 0x4E, 0x4E, 0x4E, 0x4F, 0x4F, 0x4F,
                0x4F, 0x4F, 0x4F, 0x50, 0x50, 0x50, 0x50, 0x50, 0x51, 0x51, 0x51, 0x51, 0x51, 0x52, 0x52, 0x52,
                0x52, 0x52, 0x53, 0x53, 0x53, 0x53, 0x54, 0x54, 0x54, 0x54, 0x54, 0x55, 0x55, 0x55, 0x55, 0x56,
                0x56, 0x56, 0x56, 0x57, 0x57, 0x57, 0x57, 0x57, 0x58, 0x58, 0x58, 0x59, 0x59, 0x59, 0x59, 0x5A,
                0x5A, 0x5A, 0x5A, 0x5B, 0x5B, 0x5B, 0x5B, 0x5C, 0x5C, 0x5C, 0x5D, 0x5D, 0x5D, 0x5D, 0x5E, 0x5E,
                0x5E, 0x5F, 0x5F, 0x5F, 0x60, 0x60, 0x60, 0x61, 0x61, 0x61, 0x61, 0x62, 0x62, 0x62, 0x63, 0x63,
                0x63, 0x64, 0x64, 0x64, 0x65, 0x65, 0x66, 0x66, 0x66, 0x67, 0x67, 0x67, 0x68, 0x68, 0x68, 0x69,
                0x69, 0x6A, 0x6A, 0x6A, 0x6B, 0x6B, 0x6B, 0x6C, 0x6C, 0x6D, 0x6D, 0x6D, 0x6E, 0x6E, 0x6F, 0x6F,
                0x70, 0x70, 0x70, 0x71, 0x71, 0x72, 0x72, 0x73, 0x73, 0x73, 0x74, 0x74, 0x75, 0x75, 0x76, 0x76,
                0x77, 0x77, 0x78, 0x78, 0x78, 0x79, 0x79, 0x7A, 0x7A, 0x7B, 0x7B, 0x7C, 0x7C, 0x7D, 0x7D, 0x7E,
                0x7E, 0x7F, 0x7F, 0x80, 0x80, 0x81, 0x81, 0x82, 0x83, 0x83, 0x84, 0x84, 0x85, 0x85, 0x86, 0x86,
                0x87, 0x88, 0x88, 0x89, 0x89, 0x8A, 0x8A, 0x8B, 0x8C, 0x8C, 0x8D, 0x8D, 0x8E, 0x8F, 0x8F, 0x90,
                0x90, 0x91, 0x92, 0x92, 0x93, 0x94, 0x94, 0x95, 0x95, 0x96, 0x97, 0x97, 0x98, 0x99, 0x99, 0x9A,
                0x9B, 0x9B, 0x9C, 0x9D, 0x9D, 0x9E, 0x9F, 0xA0, 0xA0, 0xA1, 0xA2, 0xA2, 0xA3, 0xA4, 0xA5, 0xA5,
                0xA6, 0xA7, 0xA7, 0xA8, 0xA9, 0xAA, 0xAA, 0xAB, 0xAC, 0xAD, 0xAE, 0xAE, 0xAF, 0xB0, 0xB1, 0xB1,
                0xB2, 0xB3, 0xB4, 0xB5, 0xB6, 0xB6, 0xB7, 0xB8, 0xB9, 0xBA, 0xBA, 0xBB, 0xBC, 0xBD, 0xBE, 0xBF,
                0xC0, 0xC1, 0xC1, 0xC2, 0xC3, 0xC4, 0xC5, 0xC6, 0xC7, 0xC8, 0xC9, 0xC9, 0xCA, 0xCB, 0xCC, 0xCD,
                0xCE, 0xCF, 0xD0, 0xD1, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6, 0xD7, 0xD8, 0xD9, 0xDA, 0xDB, 0xDC, 0xDD,
                0xDE, 0xDF, 0xE0, 0xE1, 0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7, 0xE8, 0xE9, 0xEA, 0xEB, 0xED, 0xEE,
                0xEF, 0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF7, 0xF8, 0xF9, 0xFA, 0xFB, 0xFC, 0xFD, 0xFF, 0xFF
        ).map(Int::toByte).toByteArray()

        val SCALE_TO_RESOLUTION_CURVE = intArrayOf(
                0x0E, 0x0E, 0x0E, 0x0E, 0x0E, 0x0E, 0x0D, 0x0D,
                0x0D, 0x0D, 0x0D, 0x0D, 0x0C, 0x0C, 0x0C, 0x0C,
                0x0C, 0x0C, 0x0B, 0x0B, 0x0B, 0x0B, 0x0B, 0x0B,
                0x0A, 0x0A, 0x0A, 0x0A, 0x0A, 0x0A, 0x0A, 0x09,
                0x09, 0x09, 0x09, 0x09, 0x09, 0x08, 0x08, 0x08,
                0x08, 0x08, 0x08, 0x07, 0x06, 0x06, 0x05, 0x04,
                0x04, 0x04, 0x03, 0x03, 0x03, 0x02, 0x02, 0x02,
                0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        )

        val DEQUANTIZER_SCALING_TABLE = intArrayOf(
                0x342A8D26, 0x34633F89, 0x3497657D, 0x34C9B9BE, 0x35066491, 0x353311C4, 0x356E9910, 0x359EF532,
                0x35D3CCF1, 0x360D1ADF, 0x363C034A, 0x367A83B3, 0x36A6E595, 0x36DE60F5, 0x371426FF, 0x3745672A,
                0x37838359, 0x37AF3B79, 0x37E97C38, 0x381B8D3A, 0x384F4319, 0x388A14D5, 0x38B7FBF0, 0x38F5257D,
                0x3923520F, 0x39599D16, 0x3990FA4D, 0x39C12C4D, 0x3A00B1ED, 0x3A2B7A3A, 0x3A647B6D, 0x3A9837F0,
                0x3ACAD226, 0x3B071F62, 0x3B340AAF, 0x3B6FE4BA, 0x3B9FD228, 0x3BD4F35B, 0x3C0DDF04, 0x3C3D08A4,
                0x3C7BDFED, 0x3CA7CD94, 0x3CDF9613, 0x3D14F4F0, 0x3D467991, 0x3D843A29, 0x3DB02F0E, 0x3DEAC0C7,
                0x3E1C6573, 0x3E506334, 0x3E8AD4C6, 0x3EB8FBAF, 0x3EF67A41, 0x3F243516, 0x3F5ACB94, 0x3F91C3D3,
                0x3FC238D2, 0x400164D2, 0x402C6897, 0x4065B907, 0x40990B88, 0x40CBEC15, 0x4107DB35, 0x413504F3
        ).map(Float.Companion::fromBits).toFloatArray()

        val QUANTIZER_STEP_SIZE = intArrayOf(
                0x00000000, 0x3F2AAAAB, 0x3ECCCCCD, 0x3E924925, 0x3E638E39, 0x3E3A2E8C, 0x3E1D89D9, 0x3E088889,
                0x3D842108, 0x3D020821, 0x3C810204, 0x3C008081, 0x3B804020, 0x3B002008, 0x3A801002, 0x3A000801
        ).map(Float.Companion::fromBits).toFloatArray()

        val QUANTIZED_SPECTRUM_MAX_BITS = intArrayOf(
                0, 2, 3, 3, 4, 4, 4, 4, 5, 6, 7, 8, 9, 10, 11, 12
        )

        val QUANTIZED_SPECTRUM_BITS = intArrayOf(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                1, 1, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                2, 2, 2, 2, 2, 2, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0,
                2, 2, 3, 3, 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0,
                3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4,
                3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4,
                3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
                3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4
        )

        val QUANTIZED_SPECTRUM_VALUES = intArrayOf(
                +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0,
                +0, +0, +1, -1, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0,
                +0, +0, +1, +1, -1, -1, +2, -2, +0, +0, +0, +0, +0, +0, +0, +0,
                +0, +0, +1, -1, +2, -2, +3, -3, +0, +0, +0, +0, +0, +0, +0, +0,
                +0, +0, +1, +1, -1, -1, +2, +2, -2, -2, +3, +3, -3, -3, +4, -4,
                +0, +0, +1, +1, -1, -1, +2, +2, -2, -2, +3, -3, +4, -4, +5, -5,
                +0, +0, +1, +1, -1, -1, +2, -2, +3, -3, +4, -4, +5, -5, +6, -6,
                +0, +0, +1, -1, +2, -2, +3, -3, +4, -4, +5, -5, +6, -6, +7, -7
        ).map(Int::toFloat).toFloatArray()

        val SCALE_CONVERSION_TABLE = intArrayOf(
                0x00000000, 0x00000000, 0x32A0B051, 0x32D61B5E, 0x330EA43A, 0x333E0F68, 0x337D3E0C, 0x33A8B6D5,
                0x33E0CCDF, 0x3415C3FF, 0x34478D75, 0x3484F1F6, 0x34B123F6, 0x34EC0719, 0x351D3EDA, 0x355184DF,
                0x358B95C2, 0x35B9FCD2, 0x35F7D0DF, 0x36251958, 0x365BFBB8, 0x36928E72, 0x36C346CD, 0x370218AF,
                0x372D583F, 0x3766F85B, 0x3799E046, 0x37CD078C, 0x3808980F, 0x38360094, 0x38728177, 0x38A18FAF,
                0x38D744FD, 0x390F6A81, 0x393F179A, 0x397E9E11, 0x39A9A15B, 0x39E2055B, 0x3A16942D, 0x3A48A2D8,
                0x3A85AAC3, 0x3AB21A32, 0x3AED4F30, 0x3B1E196E, 0x3B52A81E, 0x3B8C57CA, 0x3BBAFF5B, 0x3BF9295A,
                0x3C25FED7, 0x3C5D2D82, 0x3C935A2B, 0x3CC4563F, 0x3D02CD87, 0x3D2E4934, 0x3D68396A, 0x3D9AB62B,
                0x3DCE248C, 0x3E0955EE, 0x3E36FD92, 0x3E73D290, 0x3EA27043, 0x3ED87039, 0x3F1031DC, 0x3F40213B,

                0x3F800000, 0x3FAA8D26, 0x3FE33F89, 0x4017657D, 0x4049B9BE, 0x40866491, 0x40B311C4, 0x40EE9910,
                0x411EF532, 0x4153CCF1, 0x418D1ADF, 0x41BC034A, 0x41FA83B3, 0x4226E595, 0x425E60F5, 0x429426FF,
                0x42C5672A, 0x43038359, 0x432F3B79, 0x43697C38, 0x439B8D3A, 0x43CF4319, 0x440A14D5, 0x4437FBF0,
                0x4475257D, 0x44A3520F, 0x44D99D16, 0x4510FA4D, 0x45412C4D, 0x4580B1ED, 0x45AB7A3A, 0x45E47B6D,
                0x461837F0, 0x464AD226, 0x46871F62, 0x46B40AAF, 0x46EFE4BA, 0x471FD228, 0x4754F35B, 0x478DDF04,
                0x47BD08A4, 0x47FBDFED, 0x4827CD94, 0x485F9613, 0x4894F4F0, 0x48C67991, 0x49043A29, 0x49302F0E,
                0x496AC0C7, 0x499C6573, 0x49D06334, 0x4A0AD4C6, 0x4A38FBAF, 0x4A767A41, 0x4AA43516, 0x4ADACB94,
                0x4B11C3D3, 0x4B4238D2, 0x4B8164D2, 0x4BAC6897, 0x4BE5B907, 0x4C190B88, 0x4C4BEC15, 0x00000000
        ).map(Float.Companion::fromBits).toFloatArray()

        val INTENSITY_RATIO_TABLE = intArrayOf(
                0x40000000, 0x3FEDB6DB, 0x3FDB6DB7, 0x3FC92492, 0x3FB6DB6E, 0x3FA49249, 0x3F924925, 0x3F800000,
                0x3F5B6DB7, 0x3F36DB6E, 0x3F124925, 0x3EDB6DB7, 0x3E924925, 0x3E124925, 0x00000000, 0x00000000,
                /* v2.0 seems to define indexes over 15, but intensity is packed in 4b thus unused */
                0x00000000, 0x32A0B051, 0x32D61B5E, 0x330EA43A, 0x333E0F68, 0x337D3E0C, 0x33A8B6D5, 0x33E0CCDF,
                0x3415C3FF, 0x34478D75, 0x3484F1F6, 0x34B123F6, 0x34EC0719, 0x351D3EDA, 0x355184DF, 0x358B95C2,
                0x35B9FCD2, 0x35F7D0DF, 0x36251958, 0x365BFBB8, 0x36928E72, 0x36C346CD, 0x370218AF, 0x372D583F,
                0x3766F85B, 0x3799E046, 0x37CD078C, 0x3808980F, 0x38360094, 0x38728177, 0x38A18FAF, 0x38D744FD,
                0x390F6A81, 0x393F179A, 0x397E9E11, 0x39A9A15B, 0x39E2055B, 0x3A16942D, 0x3A48A2D8, 0x3A85AAC3,
                0x3AB21A32, 0x3AED4F30, 0x3B1E196E, 0x3B52A81E, 0x3B8C57CA, 0x3BBAFF5B, 0x3BF9295A, 0x3C25FED7,
                0x3C5D2D82, 0x3C935A2B, 0x3CC4563F, 0x3D02CD87, 0x3D2E4934, 0x3D68396A, 0x3D9AB62B, 0x3DCE248C,
                0x3E0955EE, 0x3E36FD92, 0x3E73D290, 0x3EA27043, 0x3ED87039, 0x3F1031DC, 0x3F40213B, 0x00000000
        ).map(Float.Companion::fromBits).toFloatArray()

        val SIN_TABLES = arrayOf(
                intArrayOf(
                        0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75,
                        0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75,
                        0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75,
                        0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75,
                        0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75,
                        0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75,
                        0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75,
                        0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75, 0x3DA73D75
                ),
                intArrayOf(
                        0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31,
                        0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31,
                        0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31,
                        0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31,
                        0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31,
                        0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31,
                        0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31,
                        0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31, 0x3F7B14BE, 0x3F54DB31
                ),
                intArrayOf(
                        0x3F7EC46D, 0x3F74FA0B, 0x3F61C598, 0x3F45E403, 0x3F7EC46D, 0x3F74FA0B, 0x3F61C598, 0x3F45E403,
                        0x3F7EC46D, 0x3F74FA0B, 0x3F61C598, 0x3F45E403, 0x3F7EC46D, 0x3F74FA0B, 0x3F61C598, 0x3F45E403,
                        0x3F7EC46D, 0x3F74FA0B, 0x3F61C598, 0x3F45E403, 0x3F7EC46D, 0x3F74FA0B, 0x3F61C598, 0x3F45E403,
                        0x3F7EC46D, 0x3F74FA0B, 0x3F61C598, 0x3F45E403, 0x3F7EC46D, 0x3F74FA0B, 0x3F61C598, 0x3F45E403,
                        0x3F7EC46D, 0x3F74FA0B, 0x3F61C598, 0x3F45E403, 0x3F7EC46D, 0x3F74FA0B, 0x3F61C598, 0x3F45E403,
                        0x3F7EC46D, 0x3F74FA0B, 0x3F61C598, 0x3F45E403, 0x3F7EC46D, 0x3F74FA0B, 0x3F61C598, 0x3F45E403,
                        0x3F7EC46D, 0x3F74FA0B, 0x3F61C598, 0x3F45E403, 0x3F7EC46D, 0x3F74FA0B, 0x3F61C598, 0x3F45E403,
                        0x3F7EC46D, 0x3F74FA0B, 0x3F61C598, 0x3F45E403, 0x3F7EC46D, 0x3F74FA0B, 0x3F61C598, 0x3F45E403
                ),
                intArrayOf(
                        0x3F7FB10F, 0x3F7D3AAC, 0x3F7853F8, 0x3F710908, 0x3F676BD8, 0x3F5B941A, 0x3F4D9F02, 0x3F3DAEF9,
                        0x3F7FB10F, 0x3F7D3AAC, 0x3F7853F8, 0x3F710908, 0x3F676BD8, 0x3F5B941A, 0x3F4D9F02, 0x3F3DAEF9,
                        0x3F7FB10F, 0x3F7D3AAC, 0x3F7853F8, 0x3F710908, 0x3F676BD8, 0x3F5B941A, 0x3F4D9F02, 0x3F3DAEF9,
                        0x3F7FB10F, 0x3F7D3AAC, 0x3F7853F8, 0x3F710908, 0x3F676BD8, 0x3F5B941A, 0x3F4D9F02, 0x3F3DAEF9,
                        0x3F7FB10F, 0x3F7D3AAC, 0x3F7853F8, 0x3F710908, 0x3F676BD8, 0x3F5B941A, 0x3F4D9F02, 0x3F3DAEF9,
                        0x3F7FB10F, 0x3F7D3AAC, 0x3F7853F8, 0x3F710908, 0x3F676BD8, 0x3F5B941A, 0x3F4D9F02, 0x3F3DAEF9,
                        0x3F7FB10F, 0x3F7D3AAC, 0x3F7853F8, 0x3F710908, 0x3F676BD8, 0x3F5B941A, 0x3F4D9F02, 0x3F3DAEF9,
                        0x3F7FB10F, 0x3F7D3AAC, 0x3F7853F8, 0x3F710908, 0x3F676BD8, 0x3F5B941A, 0x3F4D9F02, 0x3F3DAEF9
                ),
                intArrayOf(
                        0x3F7FEC43, 0x3F7F4E6D, 0x3F7E1324, 0x3F7C3B28, 0x3F79C79D, 0x3F76BA07, 0x3F731447, 0x3F6ED89E,
                        0x3F6A09A7, 0x3F64AA59, 0x3F5EBE05, 0x3F584853, 0x3F514D3D, 0x3F49D112, 0x3F41D870, 0x3F396842,
                        0x3F7FEC43, 0x3F7F4E6D, 0x3F7E1324, 0x3F7C3B28, 0x3F79C79D, 0x3F76BA07, 0x3F731447, 0x3F6ED89E,
                        0x3F6A09A7, 0x3F64AA59, 0x3F5EBE05, 0x3F584853, 0x3F514D3D, 0x3F49D112, 0x3F41D870, 0x3F396842,
                        0x3F7FEC43, 0x3F7F4E6D, 0x3F7E1324, 0x3F7C3B28, 0x3F79C79D, 0x3F76BA07, 0x3F731447, 0x3F6ED89E,
                        0x3F6A09A7, 0x3F64AA59, 0x3F5EBE05, 0x3F584853, 0x3F514D3D, 0x3F49D112, 0x3F41D870, 0x3F396842,
                        0x3F7FEC43, 0x3F7F4E6D, 0x3F7E1324, 0x3F7C3B28, 0x3F79C79D, 0x3F76BA07, 0x3F731447, 0x3F6ED89E,
                        0x3F6A09A7, 0x3F64AA59, 0x3F5EBE05, 0x3F584853, 0x3F514D3D, 0x3F49D112, 0x3F41D870, 0x3F396842
                ),
                intArrayOf(
                        0x3F7FFB11, 0x3F7FD397, 0x3F7F84AB, 0x3F7F0E58, 0x3F7E70B0, 0x3F7DABCC, 0x3F7CBFC9, 0x3F7BACCD,
                        0x3F7A7302, 0x3F791298, 0x3F778BC5, 0x3F75DEC6, 0x3F740BDD, 0x3F721352, 0x3F6FF573, 0x3F6DB293,
                        0x3F6B4B0C, 0x3F68BF3C, 0x3F660F88, 0x3F633C5A, 0x3F604621, 0x3F5D2D53, 0x3F59F26A, 0x3F5695E5,
                        0x3F531849, 0x3F4F7A1F, 0x3F4BBBF8, 0x3F47DE65, 0x3F43E200, 0x3F3FC767, 0x3F3B8F3B, 0x3F373A23,
                        0x3F7FFB11, 0x3F7FD397, 0x3F7F84AB, 0x3F7F0E58, 0x3F7E70B0, 0x3F7DABCC, 0x3F7CBFC9, 0x3F7BACCD,
                        0x3F7A7302, 0x3F791298, 0x3F778BC5, 0x3F75DEC6, 0x3F740BDD, 0x3F721352, 0x3F6FF573, 0x3F6DB293,
                        0x3F6B4B0C, 0x3F68BF3C, 0x3F660F88, 0x3F633C5A, 0x3F604621, 0x3F5D2D53, 0x3F59F26A, 0x3F5695E5,
                        0x3F531849, 0x3F4F7A1F, 0x3F4BBBF8, 0x3F47DE65, 0x3F43E200, 0x3F3FC767, 0x3F3B8F3B, 0x3F373A23
                ),
                intArrayOf(
                        0x3F7FFEC4, 0x3F7FF4E6, 0x3F7FE129, 0x3F7FC38F, 0x3F7F9C18, 0x3F7F6AC7, 0x3F7F2F9D, 0x3F7EEA9D,
                        0x3F7E9BC9, 0x3F7E4323, 0x3F7DE0B1, 0x3F7D7474, 0x3F7CFE73, 0x3F7C7EB0, 0x3F7BF531, 0x3F7B61FC,
                        0x3F7AC516, 0x3F7A1E84, 0x3F796E4E, 0x3F78B47B, 0x3F77F110, 0x3F772417, 0x3F764D97, 0x3F756D97,
                        0x3F748422, 0x3F73913F, 0x3F7294F8, 0x3F718F57, 0x3F708066, 0x3F6F6830, 0x3F6E46BE, 0x3F6D1C1D,
                        0x3F6BE858, 0x3F6AAB7B, 0x3F696591, 0x3F6816A8, 0x3F66BECC, 0x3F655E0B, 0x3F63F473, 0x3F628210,
                        0x3F6106F2, 0x3F5F8327, 0x3F5DF6BE, 0x3F5C61C7, 0x3F5AC450, 0x3F591E6A, 0x3F577026, 0x3F55B993,
                        0x3F53FAC3, 0x3F5233C6, 0x3F5064AF, 0x3F4E8D90, 0x3F4CAE79, 0x3F4AC77F, 0x3F48D8B3, 0x3F46E22A,
                        0x3F44E3F5, 0x3F42DE29, 0x3F40D0DA, 0x3F3EBC1B, 0x3F3CA003, 0x3F3A7CA4, 0x3F385216, 0x3F36206C
                )
        ).map { ints -> ints.map(Float.Companion::fromBits).toFloatArray() }

        val COS_TABLES = arrayOf(
                longArrayOf(
                        0xBD0A8BD4, 0x3D0A8BD4, 0x3D0A8BD4, 0xBD0A8BD4, 0x3D0A8BD4, 0xBD0A8BD4, 0xBD0A8BD4, 0x3D0A8BD4,
                        0x3D0A8BD4, 0xBD0A8BD4, 0xBD0A8BD4, 0x3D0A8BD4, 0xBD0A8BD4, 0x3D0A8BD4, 0x3D0A8BD4, 0xBD0A8BD4,
                        0x3D0A8BD4, 0xBD0A8BD4, 0xBD0A8BD4, 0x3D0A8BD4, 0xBD0A8BD4, 0x3D0A8BD4, 0x3D0A8BD4, 0xBD0A8BD4,
                        0xBD0A8BD4, 0x3D0A8BD4, 0x3D0A8BD4, 0xBD0A8BD4, 0x3D0A8BD4, 0xBD0A8BD4, 0xBD0A8BD4, 0x3D0A8BD4,
                        0x3D0A8BD4, 0xBD0A8BD4, 0xBD0A8BD4, 0x3D0A8BD4, 0xBD0A8BD4, 0x3D0A8BD4, 0x3D0A8BD4, 0xBD0A8BD4,
                        0xBD0A8BD4, 0x3D0A8BD4, 0x3D0A8BD4, 0xBD0A8BD4, 0x3D0A8BD4, 0xBD0A8BD4, 0xBD0A8BD4, 0x3D0A8BD4,
                        0xBD0A8BD4, 0x3D0A8BD4, 0x3D0A8BD4, 0xBD0A8BD4, 0x3D0A8BD4, 0xBD0A8BD4, 0xBD0A8BD4, 0x3D0A8BD4,
                        0x3D0A8BD4, 0xBD0A8BD4, 0xBD0A8BD4, 0x3D0A8BD4, 0xBD0A8BD4, 0x3D0A8BD4, 0x3D0A8BD4, 0xBD0A8BD4
                ),
                longArrayOf(
                        0xBE47C5C2, 0xBF0E39DA, 0x3E47C5C2, 0x3F0E39DA, 0x3E47C5C2, 0x3F0E39DA, 0xBE47C5C2, 0xBF0E39DA,
                        0x3E47C5C2, 0x3F0E39DA, 0xBE47C5C2, 0xBF0E39DA, 0xBE47C5C2, 0xBF0E39DA, 0x3E47C5C2, 0x3F0E39DA,
                        0x3E47C5C2, 0x3F0E39DA, 0xBE47C5C2, 0xBF0E39DA, 0xBE47C5C2, 0xBF0E39DA, 0x3E47C5C2, 0x3F0E39DA,
                        0xBE47C5C2, 0xBF0E39DA, 0x3E47C5C2, 0x3F0E39DA, 0x3E47C5C2, 0x3F0E39DA, 0xBE47C5C2, 0xBF0E39DA,
                        0x3E47C5C2, 0x3F0E39DA, 0xBE47C5C2, 0xBF0E39DA, 0xBE47C5C2, 0xBF0E39DA, 0x3E47C5C2, 0x3F0E39DA,
                        0xBE47C5C2, 0xBF0E39DA, 0x3E47C5C2, 0x3F0E39DA, 0x3E47C5C2, 0x3F0E39DA, 0xBE47C5C2, 0xBF0E39DA,
                        0xBE47C5C2, 0xBF0E39DA, 0x3E47C5C2, 0x3F0E39DA, 0x3E47C5C2, 0x3F0E39DA, 0xBE47C5C2, 0xBF0E39DA,
                        0x3E47C5C2, 0x3F0E39DA, 0xBE47C5C2, 0xBF0E39DA, 0xBE47C5C2, 0xBF0E39DA, 0x3E47C5C2, 0x3F0E39DA
                ),
                longArrayOf(
                        0xBDC8BD36, 0xBE94A031, 0xBEF15AEA, 0xBF226799, 0x3DC8BD36, 0x3E94A031, 0x3EF15AEA, 0x3F226799,
                        0x3DC8BD36, 0x3E94A031, 0x3EF15AEA, 0x3F226799, 0xBDC8BD36, 0xBE94A031, 0xBEF15AEA, 0xBF226799,
                        0x3DC8BD36, 0x3E94A031, 0x3EF15AEA, 0x3F226799, 0xBDC8BD36, 0xBE94A031, 0xBEF15AEA, 0xBF226799,
                        0xBDC8BD36, 0xBE94A031, 0xBEF15AEA, 0xBF226799, 0x3DC8BD36, 0x3E94A031, 0x3EF15AEA, 0x3F226799,
                        0x3DC8BD36, 0x3E94A031, 0x3EF15AEA, 0x3F226799, 0xBDC8BD36, 0xBE94A031, 0xBEF15AEA, 0xBF226799,
                        0xBDC8BD36, 0xBE94A031, 0xBEF15AEA, 0xBF226799, 0x3DC8BD36, 0x3E94A031, 0x3EF15AEA, 0x3F226799,
                        0xBDC8BD36, 0xBE94A031, 0xBEF15AEA, 0xBF226799, 0x3DC8BD36, 0x3E94A031, 0x3EF15AEA, 0x3F226799,
                        0x3DC8BD36, 0x3E94A031, 0x3EF15AEA, 0x3F226799, 0xBDC8BD36, 0xBE94A031, 0xBEF15AEA, 0xBF226799
                ),
                longArrayOf(
                        0xBD48FB30, 0xBE164083, 0xBE78CFCC, 0xBEAC7CD4, 0xBEDAE880, 0xBF039C3D, 0xBF187FC0, 0xBF2BEB4A,
                        0x3D48FB30, 0x3E164083, 0x3E78CFCC, 0x3EAC7CD4, 0x3EDAE880, 0x3F039C3D, 0x3F187FC0, 0x3F2BEB4A,
                        0x3D48FB30, 0x3E164083, 0x3E78CFCC, 0x3EAC7CD4, 0x3EDAE880, 0x3F039C3D, 0x3F187FC0, 0x3F2BEB4A,
                        0xBD48FB30, 0xBE164083, 0xBE78CFCC, 0xBEAC7CD4, 0xBEDAE880, 0xBF039C3D, 0xBF187FC0, 0xBF2BEB4A,
                        0x3D48FB30, 0x3E164083, 0x3E78CFCC, 0x3EAC7CD4, 0x3EDAE880, 0x3F039C3D, 0x3F187FC0, 0x3F2BEB4A,
                        0xBD48FB30, 0xBE164083, 0xBE78CFCC, 0xBEAC7CD4, 0xBEDAE880, 0xBF039C3D, 0xBF187FC0, 0xBF2BEB4A,
                        0xBD48FB30, 0xBE164083, 0xBE78CFCC, 0xBEAC7CD4, 0xBEDAE880, 0xBF039C3D, 0xBF187FC0, 0xBF2BEB4A,
                        0x3D48FB30, 0x3E164083, 0x3E78CFCC, 0x3EAC7CD4, 0x3EDAE880, 0x3F039C3D, 0x3F187FC0, 0x3F2BEB4A
                ),
                longArrayOf(
                        0xBCC90AB0, 0xBD96A905, 0xBDFAB273, 0xBE2F10A2, 0xBE605C13, 0xBE888E93, 0xBEA09AE5, 0xBEB8442A,
                        0xBECF7BCA, 0xBEE63375, 0xBEFC5D27, 0xBF08F59B, 0xBF13682A, 0xBF1D7FD1, 0xBF273656, 0xBF3085BB,
                        0x3CC90AB0, 0x3D96A905, 0x3DFAB273, 0x3E2F10A2, 0x3E605C13, 0x3E888E93, 0x3EA09AE5, 0x3EB8442A,
                        0x3ECF7BCA, 0x3EE63375, 0x3EFC5D27, 0x3F08F59B, 0x3F13682A, 0x3F1D7FD1, 0x3F273656, 0x3F3085BB,
                        0x3CC90AB0, 0x3D96A905, 0x3DFAB273, 0x3E2F10A2, 0x3E605C13, 0x3E888E93, 0x3EA09AE5, 0x3EB8442A,
                        0x3ECF7BCA, 0x3EE63375, 0x3EFC5D27, 0x3F08F59B, 0x3F13682A, 0x3F1D7FD1, 0x3F273656, 0x3F3085BB,
                        0xBCC90AB0, 0xBD96A905, 0xBDFAB273, 0xBE2F10A2, 0xBE605C13, 0xBE888E93, 0xBEA09AE5, 0xBEB8442A,
                        0xBECF7BCA, 0xBEE63375, 0xBEFC5D27, 0xBF08F59B, 0xBF13682A, 0xBF1D7FD1, 0xBF273656, 0xBF3085BB
                ),
                longArrayOf(
                        0xBC490E90, 0xBD16C32C, 0xBD7B2B74, 0xBDAFB680, 0xBDE1BC2E, 0xBE09CF86, 0xBE22ABB6, 0xBE3B6ECF,
                        0xBE541501, 0xBE6C9A7F, 0xBE827DC0, 0xBE8E9A22, 0xBE9AA086, 0xBEA68F12, 0xBEB263EF, 0xBEBE1D4A,
                        0xBEC9B953, 0xBED53641, 0xBEE0924F, 0xBEEBCBBB, 0xBEF6E0CB, 0xBF00E7E4, 0xBF064B82, 0xBF0B9A6B,
                        0xBF10D3CD, 0xBF15F6D9, 0xBF1B02C6, 0xBF1FF6CB, 0xBF24D225, 0xBF299415, 0xBF2E3BDE, 0xBF32C8C9,
                        0x3C490E90, 0x3D16C32C, 0x3D7B2B74, 0x3DAFB680, 0x3DE1BC2E, 0x3E09CF86, 0x3E22ABB6, 0x3E3B6ECF,
                        0x3E541501, 0x3E6C9A7F, 0x3E827DC0, 0x3E8E9A22, 0x3E9AA086, 0x3EA68F12, 0x3EB263EF, 0x3EBE1D4A,
                        0x3EC9B953, 0x3ED53641, 0x3EE0924F, 0x3EEBCBBB, 0x3EF6E0CB, 0x3F00E7E4, 0x3F064B82, 0x3F0B9A6B,
                        0x3F10D3CD, 0x3F15F6D9, 0x3F1B02C6, 0x3F1FF6CB, 0x3F24D225, 0x3F299415, 0x3F2E3BDE, 0x3F32C8C9
                ),
                longArrayOf(
                        0xBBC90F88, 0xBC96C9B6, 0xBCFB49BA, 0xBD2FE007, 0xBD621469, 0xBD8A200A, 0xBDA3308C, 0xBDBC3AC3,
                        0xBDD53DB9, 0xBDEE3876, 0xBE039502, 0xBE1008B7, 0xBE1C76DE, 0xBE28DEFC, 0xBE354098, 0xBE419B37,
                        0xBE4DEE60, 0xBE5A3997, 0xBE667C66, 0xBE72B651, 0xBE7EE6E1, 0xBE8586CE, 0xBE8B9507, 0xBE919DDD,
                        0xBE97A117, 0xBE9D9E78, 0xBEA395C5, 0xBEA986C4, 0xBEAF713A, 0xBEB554EC, 0xBEBB31A0, 0xBEC1071E,
                        0xBEC6D529, 0xBECC9B8B, 0xBED25A09, 0xBED8106B, 0xBEDDBE79, 0xBEE363FA, 0xBEE900B7, 0xBEEE9479,
                        0xBEF41F07, 0xBEF9A02D, 0xBEFF17B2, 0xBF0242B1, 0xBF04F484, 0xBF07A136, 0xBF0A48AD, 0xBF0CEAD0,
                        0xBF0F8784, 0xBF121EB0, 0xBF14B039, 0xBF173C07, 0xBF19C200, 0xBF1C420C, 0xBF1EBC12, 0xBF212FF9,
                        0xBF239DA9, 0xBF26050A, 0xBF286605, 0xBF2AC082, 0xBF2D1469, 0xBF2F61A5, 0xBF31A81D, 0xBF33E7BC
                )
        ).map { longs -> longs.map(Long::toInt).map(Float.Companion::fromBits).toFloatArray() }

        val IMDCT_WINDOW = longArrayOf(
                0x3A3504F0, 0x3B0183B8, 0x3B70C538, 0x3BBB9268, 0x3C04A809, 0x3C308200, 0x3C61284C, 0x3C8B3F17,
                0x3CA83992, 0x3CC77FBD, 0x3CE91110, 0x3D0677CD, 0x3D198FC4, 0x3D2DD35C, 0x3D434643, 0x3D59ECC1,
                0x3D71CBA8, 0x3D85741E, 0x3D92A413, 0x3DA078B4, 0x3DAEF522, 0x3DBE1C9E, 0x3DCDF27B, 0x3DDE7A1D,
                0x3DEFB6ED, 0x3E00D62B, 0x3E0A2EDA, 0x3E13E72A, 0x3E1E00B1, 0x3E287CF2, 0x3E335D55, 0x3E3EA321,
                0x3E4A4F75, 0x3E56633F, 0x3E62DF37, 0x3E6FC3D1, 0x3E7D1138, 0x3E8563A2, 0x3E8C72B7, 0x3E93B561,
                0x3E9B2AEF, 0x3EA2D26F, 0x3EAAAAAB, 0x3EB2B222, 0x3EBAE706, 0x3EC34737, 0x3ECBD03D, 0x3ED47F46,
                0x3EDD5128, 0x3EE6425C, 0x3EEF4EFF, 0x3EF872D7, 0x3F00D4A9, 0x3F0576CA, 0x3F0A1D3B, 0x3F0EC548,
                0x3F136C25, 0x3F180EF2, 0x3F1CAAC2, 0x3F213CA2, 0x3F25C1A5, 0x3F2A36E7, 0x3F2E9998, 0x3F32E705,

                0xBF371C9E, 0xBF3B37FE, 0xBF3F36F2, 0xBF431780, 0xBF46D7E6, 0xBF4A76A4, 0xBF4DF27C, 0xBF514A6F,
                0xBF547DC5, 0xBF578C03, 0xBF5A74EE, 0xBF5D3887, 0xBF5FD707, 0xBF6250DA, 0xBF64A699, 0xBF66D908,
                0xBF68E90E, 0xBF6AD7B1, 0xBF6CA611, 0xBF6E5562, 0xBF6FE6E7, 0xBF715BEF, 0xBF72B5D1, 0xBF73F5E6,
                0xBF751D89, 0xBF762E13, 0xBF7728D7, 0xBF780F20, 0xBF78E234, 0xBF79A34C, 0xBF7A5397, 0xBF7AF439,
                0xBF7B8648, 0xBF7C0ACE, 0xBF7C82C8, 0xBF7CEF26, 0xBF7D50CB, 0xBF7DA88E, 0xBF7DF737, 0xBF7E3D86,
                0xBF7E7C2A, 0xBF7EB3CC, 0xBF7EE507, 0xBF7F106C, 0xBF7F3683, 0xBF7F57CA, 0xBF7F74B6, 0xBF7F8DB6,
                0xBF7FA32E, 0xBF7FB57B, 0xBF7FC4F6, 0xBF7FD1ED, 0xBF7FDCAD, 0xBF7FE579, 0xBF7FEC90, 0xBF7FF22E,
                0xBF7FF688, 0xBF7FF9D0, 0xBF7FFC32, 0xBF7FFDDA, 0xBF7FFEED, 0xBF7FFF8F, 0xBF7FFFDF, 0xBF7FFFFC
        ).map(Long::toInt).map(Float.Companion::fromBits).toFloatArray()

        @ExperimentalStdlibApi
        suspend operator fun invoke(context: SpiralContext, dataSource: DataSource<*>): KorneaResult<HighCompressionAudio> =
            withFormats(context) {
                val flow = dataSource.openInputFlow().getOrBreak { return@withFormats it.cast() }

                closeAfter(flow) {
                    val minHeader = ByteArray(8)
                    if (flow.read(minHeader) != 8) return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    val magic = minHeader.readInt32BE(0)?.and(HCA_MASK)
                            ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (magic != MAGIC_NUMBER_BE) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_MAGIC, localise(INVALID_MAGIC_KEY, "0x${magic.toString(16)}", "0x${MAGIC_NUMBER_BE.toString(16)}"))
                    }

                    val major = minHeader[4].toInt() and 0xFF
                    val minor = minHeader[5].toInt() and 0xFF
                    val headerSize = minHeader.readInt16BE(6) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (headerSize <= 0) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_HEADER_SIZE, localise(INVALID_HEADER_SIZE_KEY, headerSize))
                    }

                    val header = ByteArray(headerSize)
                    minHeader.copyInto(header, 0, 0, 8)
                    if (flow.read(header, 8, headerSize - 8) != (headerSize - 8))
                        return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                    trace("HCA v$major.$minor / $headerSize")

                    //Calculate Checksum
                    val checksum = calculateCRC16(header)
                    if (checksum != 0) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_CHECKSUM, localise(INVALID_CHECKSUM_KEY, checksum))
                    }

                    var pos = 8
                    val formatMagic = header.readInt32BE(pos)?.and(HCA_MASK)
                            ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    if (formatMagic != FORMAT_MAGIC_NUMBER_BE) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_FORMAT_MAGIC, localise(INVALID_FORMAT_MAGIC_KEY, "0x${formatMagic.toString(16)}", "0x${FORMAT_MAGIC_NUMBER_BE.toString(16)}"))
                    }
                    pos += 4

                    val channels = header.getOrNull(pos)?.toInt()?.and(0xFF)
                            ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    pos += 1
                    if (channels !in 1 .. 16) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_CHANNEL_COUNT, localise(INVALID_CHANNEL_COUNT_KEY, channels))
                    }

                    /* encoder max seems 48000 */
                    val sampleRate = header.readInt24BE(pos) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    pos += 3
                    if (sampleRate !in 1 .. 0x7FFFFF) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_SAMPLE_RATE, localise(INVALID_SAMPLE_RATE_KEY, sampleRate))
                    }

                    val frameCount = header.readInt32BE(pos) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    pos += 4
                    if (frameCount <= 0) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_FRAME_COUNT, localise(INVALID_FRAME_COUNT_KEY, frameCount))
                    }

                    val encoderDelay = header.readInt16BE(pos) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    pos += 2

                    val encoderPadding = header.readInt16BE(pos) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    pos += 2


//                    println("$channels / $sampleRate / $frameCount / $encoderDelay / $encoderPadding")

                    val versionMagic = header.readInt32BE(pos)?.and(HCA_MASK)
                            ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                    pos += 4

                    val audioInfo: HcaAudioInfo
                    if (versionMagic == COMPRESSION_MAGIC_NUMBER_BE) {
                        /** compression (v2.0) */
                        val frameSize = header.readInt16BE(pos) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 2

                        val minResolution = header.getOrNull(pos)?.toInt()?.and(0xFF)
                                ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 1

                        val maxResolution = header.getOrNull(pos)?.toInt()?.and(0xFF)
                                ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 1

                        val trackCount = header.getOrNull(pos)?.toInt()?.and(0xFF)
                                ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 1

                        val channelConfig = header.getOrNull(pos)?.toInt()?.and(0xFF)
                                ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 1

                        val totalBandCount = header.getOrNull(pos)?.toInt()?.and(0xFF)
                                ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 1

                        val baseBandCount = header.getOrNull(pos)?.toInt()?.and(0xFF)
                                ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 1

                        val stereoBandCount = header.getOrNull(pos)?.toInt()?.and(0xFF)
                                ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 1

                        val bandsPerHfrGroup = header.getOrNull(pos)?.toInt()?.and(0xFF)
                                ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 1

                        val reserved1 = header.getOrNull(pos)?.toInt()?.and(0xFF)
                                ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 1

                        val reserved2 = header.getOrNull(pos)?.toInt()?.and(0xFF)
                                ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 1

                        audioInfo = HcaAudioInfo(frameSize, minResolution, maxResolution, trackCount, channelConfig, totalBandCount, baseBandCount, stereoBandCount, bandsPerHfrGroup, reserved1, reserved2)
                    } else if (versionMagic == DECODE_MAGIC_NUMBER_BE) {
                        /** decode (v1.x) */

                        val frameSize = header.readInt16BE(pos) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 2

                        val minResolution = header.getOrNull(pos)?.toInt()?.and(0xFF)
                                ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 1

                        val maxResolution = header.getOrNull(pos)?.toInt()?.and(0xFF)
                                ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 1

                        val totalBandCount = header.getOrNull(pos)?.toInt()?.and(0xFF)?.plus(1)
                                ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 1

                        val baseBandCountRead = header.getOrNull(pos)?.toInt()?.and(0xFF)?.plus(1)
                                ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 1

                        val trackCountAndChannelConfig = header.getOrNull(pos)?.toInt()?.and(0xFF)
                                ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

                        val trackCount = (trackCountAndChannelConfig shl 4) and 0xF
                        val channelConfig = trackCountAndChannelConfig and 0xF
                        pos += 1

                        val stereoType = header.getOrNull(pos)?.toInt()?.and(0xFF)
                                ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 1

                        val baseBandCount = if (stereoType == 0) totalBandCount else baseBandCountRead

                        val stereoBandCount = totalBandCount - baseBandCount
                        val bandsPerHfrGroup = 0

                        audioInfo = HcaAudioInfo(frameSize, minResolution, maxResolution, trackCount, channelConfig, totalBandCount, baseBandCount, stereoBandCount, bandsPerHfrGroup)
                    } else {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_INFO_MAGIC, localise(INVALID_INFO_MAGIC_KEY, "0x${formatMagic.toString(16)}", "0x${COMPRESSION_MAGIC_NUMBER_BE.toString(16)}", "0x${DECODE_MAGIC_NUMBER_BE.toString(16)}"))
                    }

                    val vbrInfo: HcaVariableRateInfo?
                    if (header.readInt32BE(pos)?.and(HCA_MASK) == VBR_MAGIC_NUMBER_BE) {
                        pos += 4

                        val maxFrameSize = header.readInt16BE(pos) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 2

                        val noiseLevel = header.readInt16BE(pos) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 2

                        require(audioInfo.frameSize == 0 && maxFrameSize in 9 .. 0x1FF)
                        vbrInfo = HcaVariableRateInfo(maxFrameSize, noiseLevel)
                    } else {
                        vbrInfo = null
                    }

                    val athInfo: HcaAbsoluteThresholdHearingInfo?
                    if (header.readInt32BE(pos)?.and(HCA_MASK) == ATH_MAGIC_NUMBER_BE) {
                        pos += 4

                        athInfo = HcaAbsoluteThresholdHearingInfo(header.readInt16BE(pos)
                                ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY))
                        pos += 2
                    } else if (major == 2) {
                        athInfo = null
                    } else {
                        athInfo = HcaAbsoluteThresholdHearingInfo(1)
                    }

                    val loopInfo: HcaLoopInfo?
                    if (header.readInt32BE(pos)?.and(HCA_MASK) == LOOP_MAGIC_NUMBER_BE) {
                        pos += 4

                        val startFrame = header.readInt32BE(pos) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 4

                        val endFrame = header.readInt32BE(pos) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 4

                        val startDelay = header.readInt16BE(pos) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 2

                        val endPadding = header.readInt16BE(pos) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 2

                        if (startFrame !in 0 .. endFrame || endFrame > frameCount) {
                            return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_LOOP_FRAMES, localise(INVALID_LOOP_FRAMES_KEY, startFrame, endFrame, frameCount))
                        }

                        loopInfo = HcaLoopInfo(startFrame, endFrame, startDelay, endPadding)
                    } else {
                        loopInfo = null
                    }

                    val cipherInfo: HcaCipherInfo?
                    if (header.readInt32BE(pos)?.and(HCA_MASK) == CIPHER_MAGIC_NUMBER_BE) {
                        pos += 4

                        val type = header.readInt16BE(pos) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 2

                        if (type != 0 && type != 1 && type != 56) {
                            return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_CIPHER_TYPE, localise(INVALID_CIPHER_TYPE_KEY, type))
                        }

                        cipherInfo = HcaCipherInfo(type)
                    } else {
                        cipherInfo = null
                    }

                    val rvaInfo: HcaRelativeVolumeAdjustmentInfo?
                    if (header.readInt32BE(pos)?.and(HCA_MASK) == RVA_MAGIC_NUMBER_BE) {
                        pos += 4

                        rvaInfo = HcaRelativeVolumeAdjustmentInfo(header.readFloat32BE(pos)
                                ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY))
                        pos += 4
                    } else {
                        rvaInfo = null
                    }

                    val comment: HcaCommentInfo?
                    if (header.readInt32BE(pos)?.and(HCA_MASK) == COMMENT_MAGIC_NUMBER_BE) {
                        pos += 4

                        val length = header.getOrNull(pos) ?: return@closeAfter localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
                        pos += 1

                        comment = HcaCommentInfo(header.sliceArray(pos until pos + length).decodeToString())
                    } else {
                        comment = null
                    }

                    if (header.readInt32BE(pos)?.and(HCA_MASK) == PADDING_MAGIC_NUMBER_BE) {
                        pos = headerSize - 2
                    }

                    if (pos + 2 != headerSize) {
                        return@closeAfter KorneaResult.errorAsIllegalArgument(INVALID_POSITION, localise(INVALID_POSITION_KEY, pos, headerSize))
                    }
                    /* actual max seems 0x155*channels */
                    require(audioInfo.frameSize in 0x08 .. 0xFFFF)
                    require(audioInfo.minResolution == 1 && audioInfo.maxResolution == 15)

                    val hfrGroupCountA = audioInfo.totalBandCount - audioInfo.baseBandCount - audioInfo.stereoBandCount
                    val hfrGroupCount = if (audioInfo.bandsPerHfrGroup > 0)
                        hfrGroupCountA / audioInfo.bandsPerHfrGroup + (if (hfrGroupCountA % audioInfo.bandsPerHfrGroup == 0) 0 else 1)
                    else
                        0

//                    println(audioInfo)
//                    println(vbrInfo)
//                    println(athInfo)
//                    println(loopInfo)
//                    println(cipherInfo)
//                    println(rvaInfo)
//                    println(comment)

                    val channelTypes = IntArray(16)

                    val channelsPerTrack = channels / min(audioInfo.trackCount, 1)
                    if (audioInfo.stereoBandCount > 0 && channelsPerTrack > 1) {
                        var channelPos = 0
                        for (i in 0 until min(audioInfo.trackCount, 1)) {
                            when (channelsPerTrack) {
                                2 -> {
                                    channelTypes[channelPos] = CHANNEL_STEREO_PRIMARY
                                    channelTypes[channelPos + 1] = CHANNEL_STEREO_SECONDARY
                                }
                                3 -> {
                                    channelTypes[channelPos] = CHANNEL_STEREO_PRIMARY
                                    channelTypes[channelPos + 1] = CHANNEL_STEREO_SECONDARY
                                    channelTypes[channelPos + 2] = CHANNEL_DISCRETE
                                }
                                4 -> {
                                    channelTypes[channelPos] = CHANNEL_STEREO_PRIMARY
                                    channelTypes[channelPos + 1] = CHANNEL_STEREO_SECONDARY

                                    if (audioInfo.channelConfig == 0) {
                                        channelTypes[channelPos + 2] = CHANNEL_STEREO_PRIMARY
                                        channelTypes[channelPos + 3] = CHANNEL_STEREO_SECONDARY
                                    } else {
                                        channelTypes[channelPos + 2] = CHANNEL_DISCRETE
                                        channelTypes[channelPos + 3] = CHANNEL_DISCRETE
                                    }
                                }
                                5 -> {
                                    channelTypes[channelPos] = CHANNEL_STEREO_PRIMARY
                                    channelTypes[channelPos + 1] = CHANNEL_STEREO_SECONDARY
                                    channelTypes[channelPos + 2] = CHANNEL_DISCRETE

                                    if (audioInfo.channelConfig <= 2) {
                                        channelTypes[channelPos + 3] = CHANNEL_STEREO_PRIMARY
                                        channelTypes[channelPos + 4] = CHANNEL_STEREO_SECONDARY
                                    } else {
                                        channelTypes[channelPos + 3] = CHANNEL_DISCRETE
                                        channelTypes[channelPos + 4] = CHANNEL_DISCRETE
                                    }
                                }
                                6 -> {
                                    channelTypes[channelPos] = CHANNEL_STEREO_PRIMARY
                                    channelTypes[channelPos + 1] = CHANNEL_STEREO_SECONDARY
                                    channelTypes[channelPos + 2] = CHANNEL_DISCRETE
                                    channelTypes[channelPos + 3] = CHANNEL_DISCRETE
                                    channelTypes[channelPos + 4] = CHANNEL_STEREO_PRIMARY
                                    channelTypes[channelPos + 5] = CHANNEL_STEREO_SECONDARY
                                }
                                7 -> {
                                    channelTypes[channelPos] = CHANNEL_STEREO_PRIMARY
                                    channelTypes[channelPos + 1] = CHANNEL_STEREO_SECONDARY
                                    channelTypes[channelPos + 2] = CHANNEL_DISCRETE
                                    channelTypes[channelPos + 3] = CHANNEL_DISCRETE
                                    channelTypes[channelPos + 4] = CHANNEL_STEREO_PRIMARY
                                    channelTypes[channelPos + 5] = CHANNEL_STEREO_SECONDARY
                                    channelTypes[channelPos + 6] = CHANNEL_DISCRETE
                                }
                                8 -> {
                                    channelTypes[channelPos] = CHANNEL_STEREO_PRIMARY
                                    channelTypes[channelPos + 1] = CHANNEL_STEREO_SECONDARY
                                    channelTypes[channelPos + 2] = CHANNEL_DISCRETE
                                    channelTypes[channelPos + 3] = CHANNEL_DISCRETE
                                    channelTypes[channelPos + 4] = CHANNEL_STEREO_PRIMARY
                                    channelTypes[channelPos + 5] = CHANNEL_STEREO_SECONDARY
                                    channelTypes[channelPos + 6] = CHANNEL_STEREO_PRIMARY
                                    channelTypes[channelPos + 7] = CHANNEL_STEREO_SECONDARY
                                }
                            }
                            channelPos += channelsPerTrack
                        }
                    }

                    val audioChannels = Array(channels) { index ->
                        HcaAudioChannel(
                                channelTypes[index],
                                if (channelTypes[index] != 2) audioInfo.baseBandCount + audioInfo.stereoBandCount else audioInfo.baseBandCount,
                                audioInfo.baseBandCount + audioInfo.stereoBandCount
                        )
                    }

                    return@closeAfter KorneaResult.success(HighCompressionAudio(
                            SemanticVersion(major, minor),
                            audioChannels,
                            hfrGroupCount,
                            headerSize,
                            sampleRate,
                            channels,
                            audioInfo.frameSize,
                            frameCount,
                            encoderDelay,
                            encoderPadding,
                            loopInfo != null,
                            loopInfo?.startFrame,
                            loopInfo?.endFrame,
                            loopInfo?.startDelay,
                            loopInfo?.endPadding,
                            HCA_SAMPLES_PER_FRAME,
                            audioInfo,
                            athInfo,
                            cipherInfo,
                            comment?.comment,
                            cipherInfo?.type == 56,
                            dataSource
                    ))
                }
            }

        suspend fun athInit(type: Int, sampleRate: Int): ByteArray? {
            val athCurve = ByteArray(HCA_SAMPLES_PER_SUBFRAME)
            when (type) {
                0 -> return athCurve
                1 -> {
                    var acc: Int = 0
                    var index: Int
                    for (i in 0 until HCA_SAMPLES_PER_SUBFRAME) {
                        acc += sampleRate
                        index = acc shr 13

                        if (index >= 654) {
                            athCurve.fill(0xFF.toByte(), i, HCA_SAMPLES_PER_SUBFRAME)
                            break
                        }
                        athCurve[i] = ATH_BASE_CURVE[index]
                    }

                    return athCurve
                }
                else -> return null
            }
        }

        suspend fun SpiralContext.cipherInit(type: Int, keycode: Long? = null): ByteArray? {
            if (type == 56 && keycode == null)
                warn("formats.hca.no_key_for_cipher")

            when (if (type == 56 && keycode == null) 0 else type) {
                //No Encryption
                0 -> return ByteArray(256) { it.toByte() }

                //Keyless encryption (rare)
                1 -> {
                    val mul: Int = 13
                    val add: Int = 11
                    var v = 0

                    val cipherTable = ByteArray(256)

                    for (i in 1 until 255) {
                        v = (v * mul + add) and 0xFF
                        if (v == 0 || v == 0xFF)
                            v = (v * mul + add) and 0xFF
                        cipherTable[i] = v.toByte()
                    }

                    cipherTable[0] = 0
                    cipherTable[0xFF] = 0xFF.toByte()

                    return cipherTable
                }

                //56bit keycode encryption (given as a uint64_t number, but upper 8b aren't used)
                56 -> {
                    @Suppress("NAME_SHADOWING")
                    var keycode: Long = keycode!! - 1
                    val kc = ByteArray(8)
                    val seed = ByteArray(16)
                    val base = ByteArray(256)
                    val baseR = ByteArray(16)
                    val baseC = ByteArray(16)

                    /* init keycode table */
                    for (r in 0 until 7) {
                        kc[r] = (keycode and 0xFF).toByte()
                        keycode = keycode shr 8
                    }

                    seed[0x00] = kc[1]
                    seed[0x01] = kc[1] or kc[6]
                    seed[0x02] = kc[2] or kc[3]
                    seed[0x03] = kc[2]
                    seed[0x04] = kc[2] or kc[1]
                    seed[0x05] = kc[3] or kc[4]
                    seed[0x06] = kc[3]
                    seed[0x07] = kc[3] or kc[2]
                    seed[0x08] = kc[4] or kc[5]
                    seed[0x09] = kc[4]
                    seed[0x0A] = kc[4] or kc[3]
                    seed[0x0B] = kc[5] or kc[6]
                    seed[0x0C] = kc[5]
                    seed[0x0D] = kc[5] or kc[4]
                    seed[0x0E] = kc[6] or kc[1]
                    seed[0x0F] = kc[6]

                    cipherInit56CreateTable(baseR, kc[0].toInt() and 0xFF)

                    for (r in 0 until 16) {
                        cipherInit56CreateTable(baseC, seed[r].toInt() and 0xFF)
                        val nb = baseR[r].toInt().and(0xFF) shl 4

                        for (c in 0 until 16) {
                            base[r * 16 + c] = (nb or baseC[c].toInt().and(0xFF)).toByte()
                        }
                    }

                    var x = 0
                    var pos = 1
                    val cipherTable = ByteArray(256)

                    val lower = 0.toByte()
                    val upper = 0xFF.toByte()

                    for (i in 0 until 256) {
                        x = (x + 17) and 0xFF
                        if (base[x] != lower && base[x] != upper)
                            cipherTable[pos++] = base[x]
                    }

                    cipherTable[0] = lower
                    cipherTable[0xFF] = upper

                    return cipherTable
                }

                else -> return null
            }
        }

        fun cipherInit56CreateTable(table: ByteArray, key: Int) {
            val mul = ((key and 1) shl 3) or 5
            val add = (key and 0xE) or 1

            @Suppress("NAME_SHADOWING")
            var key = key shr 4
            for (i in 0 until 16) {
                key = (key * mul + add) and 0xF
                table[i] = key.toByte()
            }
        }

        fun calculateCRC16(data: ByteArray): Int {
            var sum: Int = 0
            for (i in data.indices) {
                val byte = data[i].toInt().and(0xFF)
                sum = (((sum shl 8) xor CRC16_LOOKUP_TABLE[(sum shr 8) xor byte])) and 0xFFFF
            }

            return sum
        }
    }

    suspend fun SpiralContext.readFrame(index: Int): KorneaResult<List<Short>> {
        val offset = headerSize + (blockSize * index)
        if (index >= blockCount) {
            return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        }

        return dataSource.openInputFlow().flatMap { flow ->
            closeAfter(flow) {
                flow.skip(offset.toULong())
                val rawData = ByteArray(blockSize)
                require(flow.read(rawData) == blockSize)
                decodeBlock(rawData)
            }
        }
    }

    suspend fun SpiralContext.readAudioSamples(): KorneaResult<List<Short>> =
        dataSource.openInputFlow().flatMap { flow ->
            closeAfter(flow) {
                flow.skip(headerSize.toULong())
                val rawData = ByteArray(blockSize)
                val samples: MutableList<Short> = ArrayList(blockCount * samplesPerBlock)
                for (i in 0 until blockCount) {
                    if (flow.read(rawData) != blockSize) return@closeAfter localisedNotEnoughData<List<Short>>(NOT_ENOUGH_DATA_KEY)
                    samples.addAll(decodeBlock(rawData).getOrBreak { return@closeAfter it })
                }

                return@closeAfter KorneaResult.success(samples as List<Short>)
            }
        }

    suspend fun SpiralContext.decodeBlock(block: ByteArray): KorneaResult<List<Short>> {
        val checksum = calculateCRC16(block)
        require(checksum == 0)
        require(block.readInt16BE(0) == 0xFFFF)
//        require(!encryptionEnabled)

        val athBaseCurve = athInit(athInfo?.type ?: 0, sampleRate) ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)
        val cipherTable = cipherInit(cipherInfo?.type ?: 0) ?: return localisedNotEnoughData(NOT_ENOUGH_DATA_KEY)

        for (i in block.indices) {
            block[i] = cipherTable[block[i].toInt() and 0xFF]
        }

        val bitReader = BitPoolInput(block)
        bitReader.read(16)

        val frameAcceptableNoiseLevel = bitReader.read(9)
        val frameEvaluationBoundary = bitReader.read(7)
        val packedNoiseLevel = (frameAcceptableNoiseLevel shl 8) - frameEvaluationBoundary

        for (i in 0 until channelCount) {
            val ch = audioChannels[i]
            val csfCount = ch.codedScalefactorCount

            val scalefactorDeltaBits = bitReader.read(3)
            if (scalefactorDeltaBits >= 6) {
                for (j in 0 until csfCount) {
                    ch.scaleFactors[j] = bitReader.read(6).toByte()
                }
            } else if (scalefactorDeltaBits > 0) {
                val expectedDelta = (1 shl scalefactorDeltaBits) - 1
                val extraDelta = expectedDelta shr 1
                var scalefactorPrev = bitReader.read(6)

                ch.scaleFactors[0] = scalefactorPrev.toByte()

                for (j in 1 until csfCount) {
                    val delta = bitReader.read(scalefactorDeltaBits)

                    if (delta != expectedDelta) {
                        /* may happen with bad keycodes, scalefactors must be 6b indexes */
                        val scalefactorTest = scalefactorPrev + delta - extraDelta
                        require(scalefactorTest in 0 .. 63)
                        scalefactorPrev = (scalefactorPrev + delta - extraDelta) and 0xFF
                    } else {
                        scalefactorPrev = bitReader.read(6)
                    }

                    ch.scaleFactors[j] = (scalefactorPrev and 0xFF).toByte()
                }
            } else {
                ch.scaleFactors.fill(0)
            }

            if (ch.type == CHANNEL_STEREO_SECONDARY) {
                val intensityValue = bitReader.peek(4)
                ch.intensity[0] = intensityValue.toByte()

                if (intensityValue < 15) {
                    for (j in 0 until HCA_SUBFRAMES_PER_FRAME) {
                        ch.intensity[j] = bitReader.read(4).toByte()
                    }
                }
            } else {
                for (j in 0 until hfrGroupCount) {
                    ch.scaleFactors[j + ch.hfrScaleIndex] = bitReader.read(6).toByte()
                }
            }

            /* calculate resolutions */

            for (j in 0 until csfCount) {
                var newResolution = 0
                val scalefactor = ch.scaleFactors[j]

                if (scalefactor > 0) {
                    val noiseLevel = athBaseCurve[j].toInt().and(0xFF) + ((packedNoiseLevel + j) shr 8)
                    val curvePosition = noiseLevel - ((5 * scalefactor) shr 1) + 1

                    if (curvePosition < 0) {
                        newResolution = 15
                    } else if (curvePosition >= 57) {
                        newResolution = 1
                    } else {
                        newResolution = SCALE_TO_RESOLUTION_CURVE[curvePosition]
                    }
                }

                ch.resolution[j] = newResolution.toByte()
            }

            ch.resolution.fill(0, csfCount)

            for (j in 0 until csfCount) {
                val scalefactorScale = DEQUANTIZER_SCALING_TABLE[ch.scaleFactors[j].toInt() and 0xFF]
                val resolutionScale = QUANTIZER_STEP_SIZE[ch.resolution[j].toInt() and 0xFF]
                ch.gain[j] = scalefactorScale * resolutionScale
            }
        }

        for (subframe in 0 until HCA_SUBFRAMES_PER_FRAME) {
            for (chi in 0 until channelCount) {
                val ch = audioChannels[chi]
                val csfCount = ch.codedScalefactorCount

                for (i in 0 until csfCount) {
                    val qc: Float
                    val resolution = ch.resolution[i].toInt() and 0xFF
                    val bits = QUANTIZED_SPECTRUM_MAX_BITS[resolution]
                    var code = bitReader.peek(bits)

                    if (resolution < 8) {
                        bitReader.read(bits)
                        code += resolution shl 4
                        bitReader.read(QUANTIZED_SPECTRUM_BITS[code] - bits)
                        qc = QUANTIZED_SPECTRUM_VALUES[code]
                    } else {
                        val signedCode = (1 - ((code and 1) shl 1)) * (code shr 1)
                        if (signedCode == 0) {
                            bitReader.read(bits - 1)
                        } else {
                            bitReader.read(bits)
                        }
                        qc = signedCode.toFloat()
                    }

                    ch.spectra[i] = ch.gain[i] * qc
                }

                ch.spectra.fill(0f, csfCount)
            }
            for (chi in 0 until channelCount) {
                val ch = audioChannels[chi]

                if (ch.type == CHANNEL_STEREO_SECONDARY)
                    continue

                if (audioInfo.bandsPerHfrGroup == 0)
                    continue

                val startBand = audioInfo.stereoBandCount + audioInfo.baseBandCount
                var highband = startBand
                var lowband = startBand - 1

                for (group in 0 until hfrGroupCount) {
                    for (i in 0 until audioInfo.bandsPerHfrGroup) {
                        if (highband >= audioInfo.totalBandCount)
                            break

                        val scIndex = ch.scaleFactors[ch.hfrScaleIndex + group] - ch.scaleFactors[lowband] + 64
                        ch.spectra[highband] = SCALE_CONVERSION_TABLE[scIndex] * ch.spectra[lowband]
                        highband++
                        lowband--
                    }
                }

                ch.spectra[HCA_SAMPLES_PER_SUBFRAME - 1] = 0f /* last spectral coefficient should be 0 */
            }

            for (chi in 0 until channelCount - 1) {
                val chL = audioChannels[chi]
                val chR = audioChannels[chi + 1]

                if (chL.type != CHANNEL_STEREO_PRIMARY)
                    continue

                if (audioInfo.stereoBandCount == 0)
                    continue

                val ratioL = INTENSITY_RATIO_TABLE[chR.intensity[subframe].toInt() and 0xFF]
                val ratioR = ratioL - 2.0f
                val spL = chL.spectra
                val spR = chR.spectra

                for (band in audioInfo.baseBandCount until audioInfo.totalBandCount) {
                    spR[band] = spL[band] * ratioR
                    spL[band] = spL[band] * ratioL
                }
            }

            for (chi in 0 until channelCount) {
                val ch = audioChannels[chi]

                val size = HCA_SAMPLES_PER_SUBFRAME
                val half = size / 2
                val mdctBits = HCA_MDCT_BITS

                /* apply DCT-IV to dequantized spectra */

                var temp1a = ch.spectra
                var temp2a = ch.temp

                var temp1aPos = 0

                var count1a = 1
                var count2a = half


                for (i in 0 until mdctBits) {
                    var d1 = 0
                    var d2 = count2a

                    for (j in 0 until count1a) {
                        for (k in 0 until count2a) {
                            val a = temp1a[temp1aPos++]
                            val b = temp1a[temp1aPos++]

                            temp2a[d1++] = b + a
                            temp2a[d2++] = a - b
                        }

                        d1 += count2a
                        d2 += count2a
                    }

                    val swap = temp1a
                    temp1a = temp2a
                    temp2a = swap

                    temp1aPos -= HCA_SAMPLES_PER_SUBFRAME

                    count1a = count1a shl 1
                    count2a = count2a shr 1
                }

                var temp1b = ch.temp
                var temp2b = ch.spectra

                var count1b = half
                var count2b = 1

                for (i in 0 until mdctBits) {
                    var sinTablePos = 0
                    var cosTablePos = 0
                    var d1 = 0
                    var d2 = count2b * 2 - 1
                    var s1 = 0
                    var s2 = count2b

                    for (j in 0 until count1b) {
                        for (k in 0 until count2b) {
                            val a = temp1b[s1++]
                            val b = temp1b[s2++]

                            val sin = SIN_TABLES[i][sinTablePos++]
                            val cos = COS_TABLES[i][cosTablePos++]

                            temp2b[d1++] = a * sin - b * cos
                            temp2b[d2--] = a * cos + b * sin
                        }

                        s1 += count2b
                        s2 += count2b
                        d1 += count2b
                        d2 += count2b * 3
                    }

                    val swap = temp1b
                    temp1b = temp2b
                    temp2b = swap

                    count1b = count1b shr 1
                    count2b = count2b shl 1
                }

                ch.spectra.copyInto(ch.dct)

                for (i in 0 until half) {
                    ch.wave[subframe][i] = IMDCT_WINDOW[i] * ch.dct[i + half] + ch.imdct_previous[i]
                    ch.wave[subframe][i + half] = IMDCT_WINDOW[i + half] * ch.dct[size - 1 - i] - ch.imdct_previous[i + half]
                    ch.imdct_previous[i] = IMDCT_WINDOW[size - 1 - i] * ch.dct[half - i - 1]
                    ch.imdct_previous[i + half] = IMDCT_WINDOW[half - i - 1] * ch.dct[i]
                }
            }
        }

        /* should read all frame sans checksum (16b) at most
         * one frame was found to read up to 14b left (cross referenced with CRI's tools),
         * perhaps some encoding hiccup [World of Final Fantasy Maxima (Switch) am_ev21_0170 video],
         * though this validation makes more sense when testing keys and isn't normally done on decode
         */

        require(bitReader.index + 2 > bitReader.maxIndex)

        val samples: MutableList<Short> = ArrayList(HCA_SUBFRAMES_PER_FRAME * HCA_SAMPLES_PER_SUBFRAME * channelCount)

        val scale = 32768.0f
        var f: Float
        var s: Int

        for (i in 0 until HCA_SUBFRAMES_PER_FRAME) {
            for (j in 0 until HCA_SAMPLES_PER_SUBFRAME) {
                for (k in 0 until channelCount) {
                    f = audioChannels[k].wave[i][j]
                    if (f > 1.0f) {
                        f = 1.0f
                    } else if (f < -1.0f) {
                        f = -1.0f
                    }
                    //f = f * hca->rva_volume; /* rare, won't apply for now */
                    s = (f * scale).toInt()
                    if ((s.toLong() + 0x8000) and 0xFFFF0000 > 0) {
                        s = (s shr 31) xor 0x7FFF
                    }

                    samples.add(s.toShort())
                }
            }
        }

        return KorneaResult.success(samples)
    }
}

@ExperimentalUnsignedTypes
suspend fun HighCompressionAudio.readFrame(context: SpiralContext, index: Int) = context.readFrame(index)

@ExperimentalUnsignedTypes
suspend fun HighCompressionAudio.readAudioSamples(context: SpiralContext) = context.readAudioSamples()

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun SpiralContext.HighCompressionAudio(dataSource: DataSource<*>) = HighCompressionAudio(this, dataSource)

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
suspend fun SpiralContext.UnsafeHighCompressionAudio(dataSource: DataSource<*>) = HighCompressionAudio(this, dataSource).get()