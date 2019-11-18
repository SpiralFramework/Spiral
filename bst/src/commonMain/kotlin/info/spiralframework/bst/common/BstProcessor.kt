package info.spiralframework.bst.common

import info.spiralframework.base.binding.BinaryOutputFlow
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.byteArrayOfHex
import info.spiralframework.base.common.io.*
import info.spiralframework.base.common.io.flow.InputFlow
import info.spiralframework.base.common.io.flow.OutputFlow
import info.spiralframework.formats.common.archives.*

@ExperimentalStdlibApi
object BstProcessor {
    const val STATE_OK = 0
    const val STATE_BREAK = 1
    const val STATE_SKIP = 2

    const val OPCODE_PARSE_DATA = 0x00
    const val OPCODE_ADD_MAGIC_NUMBER = 0x01
    const val OPCODE_ITERATE_SUBFILES = 0x02
    const val OPCODE_DONE = 0x03
    const val OPCODE_BREAK = 0x04
    const val OPCODE_SKIP = 0x05
    const val OPCODE_FLUSH = 0x06

    const val FILE_TYPE_PAK = 0x00
    const val FILE_TYPE_SPC = 0x01
    const val FILE_TYPE_LIN = 0x02
    const val FILE_TYPE_WRD = 0x03
    const val FILE_TYPE_SRD = 0x04
    const val FILE_TYPE_SRDI = 0x05
    const val FILE_TYPE_SRDV = 0x06

    const val FILE_TYPE_DR1_LOOP = 0x10
    const val FILE_TYPE_DR1_CLIMAX_EP = 0x11
    const val FILE_TYPE_DR1_ANAGRAM = 0x12
    const val FILE_TYPE_DR1_NONSTOP = 0x13
    const val FILE_TYPE_ROOMOBJECT = 0x14

    const val FILE_TYPE_V3_DATA_TABLE = 0x30

    const val FILE_TYPE_RAW = 0xFF

    const val MAGIC_NUMBER_PAK = 0x00 //4B 41 50 2E
    const val MAGIC_NUMBER_LIN = 0x01 //4E 49 4C 2E
    const val MAGIC_NUMBER_WRD = 0x02 //44 52 57 2E
    const val MAGIC_NUMBER_SRD = 0x03 //44 52 53 2E
    const val MAGIC_NUMBER_SRDI = 0x04 //49 44 52 53
    const val MAGIC_NUMBER_SRDV = 0x05 //56 44 52 53
    const val MAGIC_NUMBER_TGA = 0x06   // 00 00 00 00 00 00 00 00
    // 54 52 55 45 56 49 53 49
    // 4F 4E 2D 58 46 49 4C 45
    // 2E 00

    const val MAGIC_NUMBER_DR1_LOOP = 0x10 //31 50 4C 2E
    const val MAGIC_NUMBER_DR1_CLIMAX_EP = 0x11 //31 45 43 2E
    const val MAGIC_NUMBER_DR1_ANAGRAM = 0x12 //31 47 48 2E
    const val MAGIC_NUMBER_DR1_NONSTOP = 0x13 //31 44 4E 2E
    const val MAGIC_NUMBER_DR1_ROOMOBJECT = 0x14 //31 4F 52 2E

    const val MAGIC_NUMBER_V3_DATA_TABLE = 0x30 //33 54 44 2E

    const val MAGIC_NUMBER_RAW_INT8 = 0xFC
    const val MAGIC_NUMBER_RAW_INT16 = 0xFD
    const val MAGIC_NUMBER_RAW_INT32 = 0xFE
    const val MAGIC_NUMBER_RAW_INT64 = 0xFF

    @Suppress("UNREACHABLE_CODE")
    @ExperimentalUnsignedTypes
    suspend fun SpiralContext.process(source: DataSource<*>, bst: InputFlow, output: OutputFlow, startingData: Any? = null): Int {
        var scriptData: Any? = startingData

        return requireNotNull(source.openInputFlow()).use { input ->
            loop@ while (true) {
                val op = bst.read() ?: return@use STATE_BREAK
                trace("0x{0}", op.toString(16).padStart(2, '0'))
                when (op) {
                    OPCODE_PARSE_DATA -> scriptData = processParseFileAs(input, source, bst, output, scriptData)
                            ?: scriptData
                    OPCODE_ADD_MAGIC_NUMBER -> processAddMagicNumber(input, source, bst, output, scriptData)
                    OPCODE_ITERATE_SUBFILES -> processIterateSubfiles(input, source, bst, output, scriptData)
                    OPCODE_DONE -> {
                        processFlush(input, source, bst, output, scriptData)
                        return@use STATE_OK
                    }
                    OPCODE_BREAK -> {
//                        processFlush(input, source, bst, output, scriptData)
                        return@use STATE_BREAK
                    }
                    OPCODE_SKIP -> return@use STATE_SKIP
                    OPCODE_FLUSH -> processFlush(input, source, bst, output, scriptData)
                }
            }

            return@use STATE_OK
        }
    }

    @ExperimentalUnsignedTypes
    suspend fun SpiralContext.processParseFileAs(input: InputFlow, source: DataSource<*>, bst: InputFlow, output: OutputFlow, scriptData: Any?): Any? {
        when (bst.read() ?: return null) {
            FILE_TYPE_PAK -> {
                val basePak = PakArchive(this, source) ?: return null
                val customPak = CustomPakArchive()
                basePak.files.forEach { entry -> customPak[entry.index] = basePak.openSource(entry) }
                return customPak
            }
            FILE_TYPE_SPC -> {
                val baseSpc = SpcArchive(this, source) ?: return null
                val customSpc = CustomPatchSpcArchive(baseSpc)
                customSpc.addAllBaseFiles(this)
                return customSpc
            }
            else -> return null
        }
    }

    @ExperimentalUnsignedTypes
    suspend fun SpiralContext.processAddMagicNumber(input: InputFlow, source: DataSource<*>, bst: InputFlow, output: OutputFlow, scriptData: Any?) {
        when (bst.read() ?: return) {
            MAGIC_NUMBER_PAK -> output.writeInt32LE(PakArchive.MAGIC_NUMBER_LE)
            MAGIC_NUMBER_LIN -> output.writeInt32LE(0x2E4C494E)
            MAGIC_NUMBER_WRD -> output.writeInt32LE(0x2E575244)
            MAGIC_NUMBER_SRD -> output.writeInt32LE(0x2E535244)
            MAGIC_NUMBER_SRDI -> output.writeInt32LE(0x53524449)
            MAGIC_NUMBER_SRDV -> output.writeInt32LE(0x53524456)
            MAGIC_NUMBER_TGA -> output.write(byteArrayOfHex(
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x54, 0x52, 0x55, 0x45, 0x56, 0x49, 0x53, 0x49,
                    0x4F, 0x4E, 0x2D, 0x58, 0x46, 0x49, 0x4C, 0x45, 0x2E, 0x00
            ))

            MAGIC_NUMBER_DR1_LOOP -> output.writeInt32LE(0x2E4C5031)
            MAGIC_NUMBER_DR1_CLIMAX_EP -> output.writeInt32LE(0x2E434531)
            MAGIC_NUMBER_DR1_ANAGRAM -> output.writeInt32LE(0x2E484731)
            MAGIC_NUMBER_DR1_ROOMOBJECT -> output.writeInt32LE(0x2E524F31)

            MAGIC_NUMBER_V3_DATA_TABLE -> output.writeInt32LE(0x2E445433)

            MAGIC_NUMBER_RAW_INT8 -> output.write(bst.read() ?: return)
            MAGIC_NUMBER_RAW_INT16 -> output.writeInt16LE(bst.readInt16LE() ?: return)
            MAGIC_NUMBER_RAW_INT32 -> output.writeInt32LE(bst.readInt32LE() ?: return)
            MAGIC_NUMBER_RAW_INT64 -> output.writeInt64LE(bst.readInt64LE() ?: return)
        }
    }

    @ExperimentalUnsignedTypes
    //TODO: Look at streaming this; remember what Cap said
    suspend fun SpiralContext.processIterateSubfiles(input: InputFlow, source: DataSource<*>, bst: InputFlow, output: OutputFlow, scriptData: Any?) {
        when (scriptData) {
            is CustomPakArchive -> {
                loop@ for ((index, subSource) in scriptData.files) {
                    val customOutput = BinaryOutputFlow()
                    when (process(subSource, bst, customOutput)) {
                        STATE_OK -> {
                            subSource.close()
                            scriptData[index] = BinaryDataSource(customOutput.getData())
                        }
                        STATE_BREAK -> break@loop
                        STATE_SKIP -> continue@loop
                    }
                }
            }
            is CustomPatchSpcArchive -> {
                loop@ for ((name, entry) in scriptData.files) {
                    val customOutput = BinaryOutputFlow()
                    val subSource = scriptData.baseSpc.openDecompressedSource(this, requireNotNull(scriptData.baseSpc[name]))
                            ?: entry.dataSource
                    try {
                        when (process(subSource, bst, customOutput, entry)) {
                            STATE_OK -> {
                                val raw = customOutput.getData()
                                val compressed = raw //compressSpcData()
                                val compressionFlag = 0

                                entry.dataSource.close()
                                scriptData[name, compressionFlag, compressed.size.toLong(), raw.size.toLong()] = BinaryDataSource(compressed)
                            }
                            STATE_BREAK -> break@loop
                            STATE_SKIP -> continue@loop
                        }
                    } finally {
                        if (subSource !== entry.dataSource)
                            subSource.close()
                    }
                }
            }
            is CustomSpcArchive -> {
                loop@ for ((name, entry) in scriptData.files) {
                    val customOutput = BinaryOutputFlow()
                    when (process(entry.dataSource, bst, customOutput, entry)) {
                        STATE_OK -> {
                            val raw = customOutput.getData()
                            val compressed = raw //compressSpcData()
                            val compressionFlag = 0

                            entry.dataSource.close()
                            scriptData[name, compressionFlag, compressed.size.toLong(), raw.size.toLong()] = BinaryDataSource(compressed)
                        }
                        STATE_BREAK -> break@loop
                        STATE_SKIP -> continue@loop
                    }
                }
            }
        }
    }

    @ExperimentalUnsignedTypes
    suspend fun SpiralContext.processFlush(input: InputFlow, source: DataSource<*>, bst: InputFlow, output: OutputFlow, scriptData: Any?) {
        when (scriptData) {
            is CustomPakArchive -> scriptData.compile(output)
            is CustomSpcArchive -> scriptData.compile(output)
            else -> input.copyToOutputFlow(output)
        }
    }
}

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
suspend fun BstProcessor.process(context: SpiralContext, source: DataSource<*>, bst: InputFlow, output: OutputFlow, startingData: Any? = null) = context.process(source, bst, output, startingData)
