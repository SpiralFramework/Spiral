package info.spiralframework.gui.jvm.pipeline

import info.spiralframework.bst.common.BstProcessor.FILE_TYPE_DR1_ANAGRAM
import info.spiralframework.bst.common.BstProcessor.FILE_TYPE_DR1_CLIMAX_EP
import info.spiralframework.bst.common.BstProcessor.FILE_TYPE_DR1_LOOP
import info.spiralframework.bst.common.BstProcessor.FILE_TYPE_DR1_NONSTOP
import info.spiralframework.bst.common.BstProcessor.FILE_TYPE_PAK
import info.spiralframework.bst.common.BstProcessor.FILE_TYPE_DR1_ROOMOBJECT
import info.spiralframework.bst.common.BstProcessor.FILE_TYPE_SPC
import info.spiralframework.bst.common.BstProcessor.FILE_TYPE_SRD
import info.spiralframework.bst.common.BstProcessor.FILE_TYPE_SRDI
import info.spiralframework.bst.common.BstProcessor.FILE_TYPE_SRDV
import info.spiralframework.bst.common.BstProcessor.FILE_TYPE_V3_DATA_TABLE
import info.spiralframework.bst.common.BstProcessor.FILE_TYPE_WRD
import info.spiralframework.bst.common.BstProcessor.MAGIC_NUMBER_DR1_ANAGRAM
import info.spiralframework.bst.common.BstProcessor.MAGIC_NUMBER_DR1_CLIMAX_EP
import info.spiralframework.bst.common.BstProcessor.MAGIC_NUMBER_DR1_LIN
import info.spiralframework.bst.common.BstProcessor.MAGIC_NUMBER_DR1_LOOP
import info.spiralframework.bst.common.BstProcessor.MAGIC_NUMBER_DR1_NONSTOP
import info.spiralframework.bst.common.BstProcessor.MAGIC_NUMBER_DR1_ROOMOBJECT
import info.spiralframework.bst.common.BstProcessor.MAGIC_NUMBER_DR2_LIN
import info.spiralframework.bst.common.BstProcessor.MAGIC_NUMBER_PAK
import info.spiralframework.bst.common.BstProcessor.MAGIC_NUMBER_SRD
import info.spiralframework.bst.common.BstProcessor.MAGIC_NUMBER_SRDI
import info.spiralframework.bst.common.BstProcessor.MAGIC_NUMBER_SRDV
import info.spiralframework.bst.common.BstProcessor.MAGIC_NUMBER_TGA
import info.spiralframework.bst.common.BstProcessor.MAGIC_NUMBER_UDG_LIN
import info.spiralframework.bst.common.BstProcessor.MAGIC_NUMBER_UNK_LIN
import info.spiralframework.bst.common.BstProcessor.MAGIC_NUMBER_UTF8
import info.spiralframework.bst.common.BstProcessor.MAGIC_NUMBER_V3_DATA_TABLE
import info.spiralframework.bst.common.BstProcessor.MAGIC_NUMBER_WRD
import info.spiralframework.bst.common.BstProcessor.OPCODE_ADD_MAGIC_NUMBER
import info.spiralframework.bst.common.BstProcessor.OPCODE_BREAK
import info.spiralframework.bst.common.BstProcessor.OPCODE_DONE
import info.spiralframework.bst.common.BstProcessor.OPCODE_FLUSH
import info.spiralframework.bst.common.BstProcessor.OPCODE_ITERATE_SUBFILES
import info.spiralframework.bst.common.BstProcessor.OPCODE_PARSE_DATA
import info.spiralframework.bst.common.BstProcessor.OPCODE_SKIP
import org.abimon.kornea.io.common.flow.BinaryOutputFlow

private typealias intType = PipelineUnion.VariableValue.IntegerType

class BstContext : PipelineContext(null) {
    val out = BinaryOutputFlow()

    init {
        this["FILE_TYPE_PAK"] = intType(FILE_TYPE_PAK)
        this["FILE_TYPE_SPC"] = intType(FILE_TYPE_SPC)
        this["FILE_TYPE_WRD"] = intType(FILE_TYPE_WRD)
        this["FILE_TYPE_SRD"] = intType(FILE_TYPE_SRD)
        this["FILE_TYPE_SRDI"] = intType(FILE_TYPE_SRDI)
        this["FILE_TYPE_SRDV"] = intType(FILE_TYPE_SRDV)

        this["FILE_TYPE_DR1_LOOP"] = intType(FILE_TYPE_DR1_LOOP)
        this["FILE_TYPE_DR1_CLIMAX_EP"] = intType(FILE_TYPE_DR1_CLIMAX_EP)
        this["FILE_TYPE_DR1_ANAGRAM"] = intType(FILE_TYPE_DR1_ANAGRAM)
        this["FILE_TYPE_DR1_NONSTOP"] = intType(FILE_TYPE_DR1_NONSTOP)
        this["FILE_TYPE_ROOMOBJECT"] = intType(FILE_TYPE_DR1_ROOMOBJECT)

        this["FILE_TYPE_V3_DATA_TABLE"] = intType(FILE_TYPE_V3_DATA_TABLE)

        this["MAGIC_NUMBER_PAK"] = intType(MAGIC_NUMBER_PAK)
        this["MAGIC_NUMBER_UNK_LIN"] = intType(MAGIC_NUMBER_UNK_LIN)
        this["MAGIC_NUMBER_WRD"] = intType(MAGIC_NUMBER_WRD)
        this["MAGIC_NUMBER_SRD"] = intType(MAGIC_NUMBER_SRD)
        this["MAGIC_NUMBER_SRDI"] = intType(MAGIC_NUMBER_SRDI)
        this["MAGIC_NUMBER_SRDV"] = intType(MAGIC_NUMBER_SRDV)
        this["MAGIC_NUMBER_TGA"] = intType(MAGIC_NUMBER_TGA)

        this["MAGIC_NUMBER_DR1_LOOP"] = intType(MAGIC_NUMBER_DR1_LOOP)
        this["MAGIC_NUMBER_DR1_CLIMAX_EP"] = intType(MAGIC_NUMBER_DR1_CLIMAX_EP)
        this["MAGIC_NUMBER_DR1_ANAGRAM"] = intType(MAGIC_NUMBER_DR1_ANAGRAM)
        this["MAGIC_NUMBER_DR1_NONSTOP"] = intType(MAGIC_NUMBER_DR1_NONSTOP)
        this["MAGIC_NUMBER_DR1_ROOMOBJECT"] = intType(MAGIC_NUMBER_DR1_ROOMOBJECT)
        this["MAGIC_NUMBER_DR1_LIN"] = intType(MAGIC_NUMBER_DR1_LIN)
        this["MAGIC_NUMBER_DR1_LIN"] = intType(MAGIC_NUMBER_DR2_LIN)

        this["MAGIC_NUMBER_V3_DATA_TABLE"] = intType(MAGIC_NUMBER_V3_DATA_TABLE)

        this["MAGIC_NUMBER_DR1_LIN"] = intType(MAGIC_NUMBER_UDG_LIN)

        this["MAGIC_NUMBER_UTF8"] = intType(MAGIC_NUMBER_UTF8)

        register("parse_data_as") {
            addParameter("file_type")

            setFunction { spiralContext, pipelineContext, parameters ->
                out.write(OPCODE_PARSE_DATA)
                out.write(parameters.getValue("FILETYPE").asNumber(spiralContext, pipelineContext).toInt())
                null
            }
        }

        register("add_magic_number") {
            addParameter("magic_number")

            setFunction { spiralContext, pipelineContext, parameters ->
                out.write(OPCODE_ADD_MAGIC_NUMBER)
                out.write(parameters.getValue("MAGICNUMBER").asNumber(spiralContext, pipelineContext).toInt())
                null
            }
        }

        register("iterate_subfiles") {
            setFunction { _, _, _ ->
                out.write(OPCODE_ITERATE_SUBFILES)
                null
            }
        }

        register("done") {
            setFunction { _, _, _ ->
                out.write(OPCODE_DONE)
                null
            }
        }

        register("break") {
            setFunction { _, _, _ ->
                out.write(OPCODE_BREAK)
                null
            }
        }

        register("skip") {
            setFunction { _, _, _ ->
                out.write(OPCODE_SKIP)
                null
            }
        }

        register("flush") {
            setFunction { _, _, _ ->
                out.write(OPCODE_FLUSH)
                null
            }
        }
    }
}