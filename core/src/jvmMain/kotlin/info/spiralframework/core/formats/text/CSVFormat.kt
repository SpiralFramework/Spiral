package info.spiralframework.core.formats.text

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.core.formats.FormatWriteContext
import info.spiralframework.core.formats.FormatWriteResponse
import info.spiralframework.core.formats.WritableSpiralFormat
import info.spiralframework.formats.common.data.DataTableStructure
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.PrintOutputFlow
import dev.brella.kornea.toolkit.common.printLine
import info.spiralframework.base.common.PrintOutputFlowWrapper

object CSVFormat: WritableSpiralFormat {
    /** A **RECOGNISABLE** name, not necessarily the full name. May commonly be the extension */
    override val name: String = "CSV"

    /**
     * The usual extension for this format. Some formats don't have a proper extension, so this can be nullable
     */
    override val extension: String = "csv"

    /**
     * Does this format support writing [data]?
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     *
     * @return If we are able to write [data] as this format
     */
    override fun supportsWriting(context: SpiralContext, writeContext: FormatWriteContext?, data: Any): Boolean = data is DataTableStructure

    /**
     * Writes [data] to [stream] in this format
     *
     * @param name Name of the data, if any
     * @param game Game relevant to this data
     * @param context Context that we retrieved this file in
     * @param data The data to wrote
     * @param stream The stream to write to
     *
     * @return An enum for the success of the operation
     */
    override suspend fun write(context: SpiralContext, writeContext: FormatWriteContext?, data: Any, flow: OutputFlow): FormatWriteResponse {
        val out = flow as? PrintOutputFlow ?: PrintOutputFlowWrapper(flow)
        when (data) {
            is DataTableStructure -> {
                data.variableDetails.forEach { header -> out.print("${header.variableName} ${header.variableType},") }
                out.printLine()
                data.entries.forEach { entry ->
                    entry.forEach { variable ->
                        when (variable) {
                            is DataTableStructure.DataVariable.UTF8 -> out.print(data.utf8Strings[variable.data])
                            is DataTableStructure.DataVariable.UTF16 -> out.print(data.utf16Strings[variable.data])
                            else -> out.print(variable.genericData.toString())
                        }
                        out.printLine(',')
                    }
                    out.printLine()
                }
            }
            else -> return FormatWriteResponse.WRONG_FORMAT
        }

        return FormatWriteResponse.SUCCESS
    }
}