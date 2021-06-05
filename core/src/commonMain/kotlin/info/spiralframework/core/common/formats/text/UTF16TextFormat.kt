package info.spiralframework.core.common.formats.text

import dev.brella.kornea.base.common.Optional
import dev.brella.kornea.base.common.empty
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.cast
import dev.brella.kornea.errors.common.flatMap
import dev.brella.kornea.errors.common.getOrBreak
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.decodeToUTF16String
import dev.brella.kornea.io.common.encodeToUTF16LEByteArray
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.extensions.readInt16LE
import dev.brella.kornea.io.common.flow.readBytes
import dev.brella.kornea.io.common.useInputFlow
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.base.common.text.toHexString
import info.spiralframework.core.common.formats.FormatWriteResponse
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.WritableSpiralFormat
import info.spiralframework.core.common.formats.buildFormatResult

object UTF16TextFormat : ReadableSpiralFormat<String>, WritableSpiralFormat {
    override val name: String = "UTF-16 Text"
    override val extension: String = "txt"

    override suspend fun identify(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): KorneaResult<Optional<String>> =
        source.openInputFlow().flatMap { flow ->
            val bom = flow.readInt16LE()
            if (bom == 0xFFFE || bom == 0xFEFF) KorneaResult.success(Optional.empty<String>()) else KorneaResult.errorAsIllegalArgument(-1, "Invalid magic number $bom")
        }.buildFormatResult(1.0)

    override suspend fun read(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): KorneaResult<String> {
        val data = source.useInputFlow { flow -> flow.readBytes() }
            .getOrBreak { return it.cast() }

        val hasBom = (data[0] == 0xFF.toByte() && data[1] == 0xFE.toByte()) || (data[0] == 0xFE.toByte() && data[1] == 0xFF.toByte())
        val hasNullTerminator = data[data.size - 1] == 0x00.toByte() && data[data.size - 2] == 0x00.toByte()

        if (!hasBom)
            return KorneaResult.errorAsIllegalArgument(-1, "Invalid byte order marker ${data[0].toHexString()} ${data[1].toHexString()}")
        return buildFormatResult(data.decodeToUTF16String().trimEnd('\u0000'), 1.0)
    }

    override fun supportsWriting(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean = true

    override suspend fun write(context: SpiralContext, writeContext: SpiralProperties?, data: Any, flow: OutputFlow): FormatWriteResponse {
        flow.write(data.toString().encodeToUTF16LEByteArray())
        return FormatWriteResponse.SUCCESS
    }
}