package info.spiralframework.bst.common

import dev.brella.kornea.io.common.flow.BinaryOutputFlow
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.extensions.writeInt16LE
import dev.brella.kornea.io.common.flow.extensions.writeInt32LE
import dev.brella.kornea.io.common.flow.extensions.writeInt64LE

public class BstBuilder(public val out: OutputFlow) {
    public class ParseDataAsBuilder(public val out: OutputFlow) {
        public suspend fun pak() {
            out.write(BstProcessor.OPCODE_PARSE_DATA)
            out.write(BstProcessor.FILE_TYPE_PAK)
        }

        public suspend fun spc() {
            out.write(BstProcessor.OPCODE_PARSE_DATA)
            out.write(BstProcessor.FILE_TYPE_SPC)
        }
    }

    public class AddMagicNumberBuilder(public val out: OutputFlow) {
        public suspend fun pak() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_PAK)
        }

        public suspend fun lin() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_UNK_LIN)
        }

        public suspend fun wrd() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_WRD)
        }

        public suspend fun srd() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_SRD)
        }

        public suspend fun srdi() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_SRDI)
        }

        public suspend fun srdv() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_SRDV)
        }

        public suspend fun tga() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_TGA)
        }

        public suspend fun dr1Loop() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_DR1_LOOP)
        }

        public suspend fun dr1ClimaxEp() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_DR1_CLIMAX_EP)
        }

        public suspend fun dr1Anagram() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_DR1_ANAGRAM)
        }

        public suspend fun dr1Nonstop() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_DR1_NONSTOP)
        }

        public suspend fun dr1RoomObject() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_DR1_ROOMOBJECT)
        }

        public suspend fun v3DataTable() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_V3_DATA_TABLE)
        }

        public suspend fun utf8() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_UTF8)
        }

        public suspend fun rawInt8(int8: Number) {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_RAW_INT8)
            out.write(int8.toInt() and 0xFF)
        }

        public suspend fun rawInt16(int16: Number) {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_RAW_INT16)
            out.writeInt16LE(int16)
        }

        public suspend fun rawInt32(int32: Number) {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_RAW_INT32)
            out.writeInt32LE(int32)
        }

        public suspend fun rawInt64(int64: Number) {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_RAW_INT64)
            out.writeInt64LE(int64)
        }
    }

    /** 0x00 */
    public suspend fun parseDataAs(init: suspend ParseDataAsBuilder.() -> Unit): Unit = ParseDataAsBuilder(out).init()
    /** 0x01 */
    public suspend fun addMagicNumber(init: suspend AddMagicNumberBuilder.() -> Unit): Unit = AddMagicNumberBuilder(out).init()
    /* 0x02 */
    public suspend fun iterateSubfiles(init: suspend BstBuilder.() -> Unit) {
        out.write(BstProcessor.OPCODE_ITERATE_SUBFILES)
        BstBuilder(out).init()
    }
    /* 0x03 */
    public suspend fun done() {
        out.write(BstProcessor.OPCODE_DONE)
    }
    /* 0x04 */
    public suspend fun breakOut() {
        out.write(BstProcessor.OPCODE_BREAK)
    }
    /* 0x05 */
    public suspend fun skip() {
        out.write(BstProcessor.OPCODE_SKIP)
    }
    /* 0x06 */
    public suspend fun flush() {
        out.write(BstProcessor.OPCODE_FLUSH)
    }
}

public suspend fun buildBinaryScriptTemplate(init: suspend BstBuilder.() -> Unit): ByteArray {
    val out = BinaryOutputFlow()
    val builder = BstBuilder(out)
    builder.init()
    return out.getData()
}