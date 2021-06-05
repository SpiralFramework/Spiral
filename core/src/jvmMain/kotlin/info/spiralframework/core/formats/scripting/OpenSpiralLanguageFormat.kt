package info.spiralframework.core.formats.scripting

import dev.brella.kornea.base.common.Optional
import dev.brella.kornea.base.common.empty
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.errors.common.useAndFlatMap
import dev.brella.kornea.io.common.BinaryDataSource
import dev.brella.kornea.io.common.DataSource
import dev.brella.kornea.io.common.flow.BinaryOutputFlow
import dev.brella.kornea.io.common.flow.FlowReader
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.PrintOutputFlow
import dev.brella.kornea.io.common.flow.extensions.readAsciiString
import dev.brella.kornea.io.common.flow.useEachLine
import dev.brella.kornea.io.common.useInputFlowForResult
import info.spiralframework.antlr.osl.OpenSpiralLexer
import info.spiralframework.antlr.osl.OpenSpiralParser
import info.spiralframework.base.common.PrintOutputFlowWrapper
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.properties.ISpiralProperty
import info.spiralframework.base.common.properties.SpiralProperties
import info.spiralframework.base.common.properties.get
import info.spiralframework.core.common.formats.FormatWriteResponse
import info.spiralframework.core.common.formats.ReadableSpiralFormat
import info.spiralframework.core.common.formats.WritableSpiralFormat
import info.spiralframework.formats.common.games.DrGame
import info.spiralframework.formats.common.scripting.lin.LinScript
import info.spiralframework.formats.common.scripting.osl.LinTranspiler
import info.spiralframework.osb.common.OpenSpiralBitcodeBuilder
import info.spiralframework.osb.common.OpenSpiralBitcodeWrapper
import info.spiralframework.osl.OSLVisitor
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.IntStream

object OpenSpiralLanguageFormat : ReadableSpiralFormat<OpenSpiralBitcodeWrapper>, WritableSpiralFormat {
    override val name: String = "OpenSpiralLanguage"
    override val extension: String = "osl"

    val REQUIRED_PROPERTIES = listOf(DrGame.LinScriptable)

    override suspend fun identify(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): KorneaResult<Optional<OpenSpiralBitcodeWrapper>> =
        source.openInputFlow()
            .useAndFlatMap { flow ->
                if (flow.readAsciiString(10)?.equals("OSL Script", true) == true)
                    KorneaResult.success(Optional.empty<OpenSpiralBitcodeWrapper>())
                else
                    KorneaResult.empty()
            }.buildFormatResult(0.9)

    override suspend fun read(context: SpiralContext, readContext: SpiralProperties?, source: DataSource<*>): KorneaResult<OpenSpiralBitcodeWrapper> =
            source.useInputFlowForResult { flow ->
                val input = CharStreams.fromString(StringBuilder().apply {
                    FlowReader(flow).useEachLine(this::appendLine)
                }.toString(), readContext[ISpiralProperty.FileName] ?: IntStream.UNKNOWN_SOURCE_NAME)

                val lexer = OpenSpiralLexer(input)
                val tokens = CommonTokenStream(lexer)
                val parser = OpenSpiralParser(tokens)
                val tree = parser.script()
                val osb = BinaryOutputFlow()

                val visitor = OSLVisitor()
                val script = visitor.visitScript(tree)
                val builder = OpenSpiralBitcodeBuilder(osb)
                script.writeToBuilder(builder)

                OpenSpiralBitcodeWrapper(context, BinaryDataSource(osb.getData()))
            }


    override fun supportsWriting(context: SpiralContext, writeContext: SpiralProperties?, data: Any): Boolean = data is LinScript
    override fun requiredPropertiesForWrite(context: SpiralContext, writeContext: SpiralProperties?, data: Any): List<ISpiralProperty.PropertyKey<*>> = REQUIRED_PROPERTIES

    override suspend fun write(context: SpiralContext, writeContext: SpiralProperties?, data: Any, flow: OutputFlow): FormatWriteResponse {
        when (data) {
            is LinScript -> {
                LinTranspiler(
                    data,
                    writeContext[DrGame.LinScriptable]
                    ?: data.game?.takeUnless { it == DrGame.LinScriptable.Unknown }
                    ?: return FormatWriteResponse.MISSING_PROPERTY(DrGame.LinScriptable)
                ).transpile(flow as? PrintOutputFlow ?: PrintOutputFlowWrapper(flow))

                return FormatWriteResponse.SUCCESS
            }
            else -> return FormatWriteResponse.WRONG_FORMAT
        }
    }
}