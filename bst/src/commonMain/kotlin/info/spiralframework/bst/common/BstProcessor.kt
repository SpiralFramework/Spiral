package info.spiralframework.bst.common

import info.spiralframework.base.binding.BinaryOutputFlow
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.*
import info.spiralframework.base.common.io.flow.InputFlow
import info.spiralframework.base.common.io.flow.OutputFlow
import info.spiralframework.formats.common.archives.CustomPakArchive
import info.spiralframework.formats.common.archives.PakArchive

object BstProcessor {
    const val OPCODE_PARSE_DATA = 0x00
    const val OPCODE_ADD_MAGIC_NUMBER = 0x01
    const val OPCODE_ITERATE_SUBFILES = 0x02
    const val OPCODE_DONE = 0x03
    const val OPCODE_BREAK = 0x04

    const val FILE_TYPE_PAK = 0x00
    const val FILE_TYPE_RAW = 0xFF

    const val MAGIC_NUMBER_PAK = 0x00
    const val MAGIC_NUMBER_RAW_INT8 = 0xFC
    const val MAGIC_NUMBER_RAW_INT16 = 0xFD
    const val MAGIC_NUMBER_RAW_INT32 = 0xFE
    const val MAGIC_NUMBER_RAW_INT64 = 0xFF

    @ExperimentalUnsignedTypes
    suspend fun SpiralContext.process(source: DataSource<*>, bst: InputFlow, output: OutputFlow): Boolean {
        var scriptData: Any? = null

        return requireNotNull(source.openInputFlow()).use { input ->
            loop@ while (true) {
                val op = bst.read() ?: break@loop
                when (op) {
                    OPCODE_PARSE_DATA -> scriptData = processParseFileAs(input, source, bst, output, scriptData)
                            ?: scriptData
                    OPCODE_ADD_MAGIC_NUMBER -> processAddMagicNumber(input, source, bst, output, scriptData)
                    OPCODE_ITERATE_SUBFILES -> processIterateSubfiles(input, source, bst, output, scriptData)
                    OPCODE_DONE -> {
                        processFlush(input, source, bst, output, scriptData)
                        return@use false
                    }
                    OPCODE_BREAK -> {
                        processFlush(input, source, bst, output, scriptData)
                        return@use true
                    }
                }
            }

            return@use false
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
            else -> return null
        }
    }

    @ExperimentalUnsignedTypes
    suspend fun SpiralContext.processAddMagicNumber(input: InputFlow, source: DataSource<*>, bst: InputFlow, output: OutputFlow, scriptData: Any?) {
        when (bst.read() ?: return) {
            MAGIC_NUMBER_PAK -> output.writeInt32LE(PakArchive.MAGIC_NUMBER)

            MAGIC_NUMBER_RAW_INT8 -> output.write(bst.read() ?: return)
            MAGIC_NUMBER_RAW_INT16 -> output.writeInt16LE(bst.readInt16LE() ?: return)
            MAGIC_NUMBER_RAW_INT32 -> output.writeInt32LE(bst.readInt32LE() ?: return)
            MAGIC_NUMBER_RAW_INT64 -> output.writeInt64LE(bst.readInt64LE() ?: return)
        }
    }

    @ExperimentalUnsignedTypes
    suspend fun SpiralContext.processIterateSubfiles(input: InputFlow, source: DataSource<*>, bst: InputFlow, output: OutputFlow, scriptData: Any?) {
        when (scriptData) {
            is CustomPakArchive -> {
                for ((index, subSource) in scriptData.files) {
                    val customOutput = BinaryOutputFlow()
                    val shouldBreak = process(subSource, bst, customOutput)
                    scriptData[index] = BinaryDataSource(customOutput.getData())
                    if (shouldBreak)
                        break
                }
            }
        }
    }

    @ExperimentalUnsignedTypes
    suspend fun SpiralContext.processFlush(input: InputFlow, source: DataSource<*>, bst: InputFlow, output: OutputFlow, scriptData: Any?) {
        when (scriptData) {
            is CustomPakArchive -> scriptData.compile(output)
            else -> input.copyToOutputFlow(output)
        }
    }
}

@ExperimentalUnsignedTypes
suspend fun BstProcessor.process(context: SpiralContext, source: DataSource<*>, bst: InputFlow, output: OutputFlow): Boolean = context.process(source, bst, output)
