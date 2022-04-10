package info.spiralframework.core.common.formats.text

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.success
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.toolkit.common.printLine
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.core.common.formats.WritableSpiralFormat
import info.spiralframework.core.common.formats.spiralWrongFormat
import info.spiralframework.formats.common.data.DataTableStructure

public object CSVFormat : WritableSpiralFormat<Unit> {
    override val name: String = "CSV"
    override val extension: String = "csv"

    override fun supportsWriting(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean =
        data is DataTableStructure

    override suspend fun write(
        context: SpiralContext,
        writeContext: SpiralProperties?,
        data: Any,
        flow: OutputFlow
    ): KorneaResult<Unit> {
        when (data) {
            is DataTableStructure -> {
                data.variableDetails.forEach { header -> flow.print("${header.variableName} ${header.variableType},") }
                flow.printLine()

                data.entries.forEach { entry ->
                    entry.forEach { variable ->
                        when (variable) {
                            is DataTableStructure.DataVariable.UTF8 -> flow.print(data.utf8Strings[variable.data])
                            is DataTableStructure.DataVariable.UTF16 -> flow.print(data.utf16Strings[variable.data])
                            else -> flow.print(variable.genericData.toString())
                        }
                        flow.printLine(',')
                    }

                    flow.printLine()
                }
            }
            else -> return KorneaResult.spiralWrongFormat()
        }

        return KorneaResult.success()
    }
}