package info.spiralframework.console.jvm.commands.mechanic

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.console.jvm.commands.CommandRegistrar
import info.spiralframework.console.jvm.pipeline.PipelineContext
import info.spiralframework.console.jvm.pipeline.PipelineUnion
import info.spiralframework.core.formats.WritableSpiralFormat
import info.spiralframework.core.formats.archives.*
import java.text.DecimalFormat

@Suppress("unused")
@ExperimentalUnsignedTypes
object GurrenMechanic : CommandRegistrar {
    val COMPILABLE_ARCHIVES = arrayOf<WritableSpiralFormat>(
            CpkArchiveFormat, PakArchiveFormat, SpcArchiveFormat,
            WadArchiveFormat, ZipFormat
    )

    val PERCENT_FORMAT = DecimalFormat("00.00")

    suspend fun SpiralContext.showEnvironment() {
        println(retrieveEnvironment().entries.joinToString("\n") { (k, v) -> "environment[$k]: \"$v\""})
    }

    override suspend fun register(spiralContext: SpiralContext, pipelineContext: PipelineContext) {
        pipelineContext.register("show_environment") {
            setFunction { spiralContext, _, _ -> spiralContext.showEnvironment(); null }
        }
    }
}