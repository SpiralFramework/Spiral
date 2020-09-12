package info.spiralframework.bst.common

import dev.brella.kornea.io.common.flow.BinaryOutputFlow
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.extensions.writeInt16LE
import dev.brella.kornea.io.common.flow.extensions.writeInt32LE
import dev.brella.kornea.io.common.flow.extensions.writeInt64LE

@ExperimentalUnsignedTypes
@ExperimentalStdlibApi
class BstBuilder(val out: OutputFlow) {
    class ParseDataAsBuilder(val out: OutputFlow) {
        suspend fun pak() {
            out.write(BstProcessor.OPCODE_PARSE_DATA)
            out.write(BstProcessor.FILE_TYPE_PAK)
        }

        suspend fun spc() {
            out.write(BstProcessor.OPCODE_PARSE_DATA)
            out.write(BstProcessor.FILE_TYPE_SPC)
        }
    }

    class AddMagicNumberBuilder(val out: OutputFlow) {
        suspend fun pak() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_PAK)
        }

        suspend fun lin() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_UNK_LIN)
        }

        suspend fun wrd() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_WRD)
        }

        suspend fun srd() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_SRD)
        }

        suspend fun srdi() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_SRDI)
        }

        suspend fun srdv() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_SRDV)
        }

        suspend fun tga() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_TGA)
        }

        suspend fun dr1Loop() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_DR1_LOOP)
        }

        suspend fun dr1ClimaxEp() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_DR1_CLIMAX_EP)
        }

        suspend fun dr1Anagram() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_DR1_ANAGRAM)
        }

        suspend fun dr1Nonstop() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_DR1_NONSTOP)
        }

        suspend fun dr1RoomObject() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_DR1_ROOMOBJECT)
        }

        suspend fun v3DataTable() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_V3_DATA_TABLE)
        }

        suspend fun utf8() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_UTF8)
        }

        suspend fun rawInt8(int8: Number) {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_RAW_INT8)
            out.write(int8.toInt() and 0xFF)
        }

        suspend fun rawInt16(int16: Number) {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_RAW_INT16)
            out.writeInt16LE(int16)
        }

        suspend fun rawInt32(int32: Number) {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_RAW_INT32)
            out.writeInt32LE(int32)
        }

        suspend fun rawInt64(int64: Number) {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_RAW_INT64)
            out.writeInt64LE(int64)
        }
    }

    /** 0x00 */
    suspend fun parseDataAs(init: suspend ParseDataAsBuilder.() -> Unit) = ParseDataAsBuilder(out).init()
    /** 0x01 */
    suspend fun addMagicNumber(init: suspend AddMagicNumberBuilder.() -> Unit) = AddMagicNumberBuilder(out).init()
    /* 0x02 */
    suspend fun iterateSubfiles(init: suspend BstBuilder.() -> Unit) {
        out.write(BstProcessor.OPCODE_ITERATE_SUBFILES)
        BstBuilder(out).init()
    }
    /* 0x03 */
    suspend fun done() {
        out.write(BstProcessor.OPCODE_DONE)
    }
    /* 0x04 */
    suspend fun breakOut() {
        out.write(BstProcessor.OPCODE_BREAK)
    }
    /* 0x05 */
    suspend fun skip() {
        out.write(BstProcessor.OPCODE_SKIP)
    }
    /* 0x06 */
    suspend fun flush() {
        out.write(BstProcessor.OPCODE_FLUSH)
    }
}

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
suspend fun buildBinaryScriptTemplate(init: suspend BstBuilder.() -> Unit): ByteArray {
    val out = BinaryOutputFlow()
    val builder = BstBuilder(out)
    builder.init()
    return out.getData()
}