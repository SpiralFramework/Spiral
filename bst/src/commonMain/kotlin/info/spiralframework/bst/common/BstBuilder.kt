package info.spiralframework.bst.common

import info.spiralframework.base.binding.BinaryOutputFlow
import info.spiralframework.base.common.io.flow.OutputFlow

@ExperimentalUnsignedTypes
class BstBuilder(val out: OutputFlow) {
    class ParseDataAsBuilder(val out: OutputFlow) {
        suspend fun pak() {
            out.write(BstProcessor.OPCODE_PARSE_DATA)
            out.write(BstProcessor.FILE_TYPE_PAK)
        }
    }

    class AddMagicNumberBuilder(val out: OutputFlow) {
        suspend fun pak() {
            out.write(BstProcessor.OPCODE_ADD_MAGIC_NUMBER)
            out.write(BstProcessor.MAGIC_NUMBER_PAK)
        }
    }

    suspend fun parseDataAs(init: suspend ParseDataAsBuilder.() -> Unit) = ParseDataAsBuilder(out).init()
    suspend fun addMagicNumber(init: suspend AddMagicNumberBuilder.() -> Unit) = AddMagicNumberBuilder(out).init()
    suspend fun iterateSubfiles(init: suspend BstBuilder.() -> Unit) {
        out.write(BstProcessor.OPCODE_ITERATE_SUBFILES)
        BstBuilder(out).init()
    }
    suspend fun done() {
        out.write(BstProcessor.OPCODE_DONE)
    }
    suspend fun breakOut() {
        out.write(BstProcessor.OPCODE_BREAK)
    }
}

@ExperimentalUnsignedTypes
suspend fun buildBinaryScriptTemplate(init: suspend BstBuilder.() -> Unit): ByteArray {
    val out = BinaryOutputFlow()
    val builder = BstBuilder(out)
    builder.init()
    return out.getData()
}