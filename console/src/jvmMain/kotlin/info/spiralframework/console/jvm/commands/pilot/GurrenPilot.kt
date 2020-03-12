package info.spiralframework.console.jvm.commands.pilot

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.console.jvm.commands.CommandRegistrar
import info.spiralframework.console.jvm.pipeline.PipelineContext
import java.text.DecimalFormat
import java.util.concurrent.atomic.AtomicBoolean

@ExperimentalUnsignedTypes
object GurrenPilot : CommandRegistrar {
    /** Helper Variables */
    var keepLooping = AtomicBoolean(true)

    val PERCENT_FORMAT = DecimalFormat("00.00")

    suspend fun SpiralContext.showEnvironment() {
        println(
                retrieveEnvironment().entries
                        .groupBy { (k) -> k.substringBeforeLast('.') }
                        .entries
                        .sortedBy(Map.Entry<String, *>::key)
                        .flatMap { (_, v) -> v.sortedBy(Map.Entry<String, String>::key) }
                        .joinToString("\n") { (k, v) ->
                            "environment[$k]: \"${v.replace("\r", "\\r").replace("\n", "\\n")}\""
                        }
        )
    }

    override suspend fun register(spiralContext: SpiralContext, pipelineContext: PipelineContext) {
        pipelineContext.register("show_environment") {
            setFunction { spiralContext, _, _ -> spiralContext.showEnvironment(); null }
        }
    }
}