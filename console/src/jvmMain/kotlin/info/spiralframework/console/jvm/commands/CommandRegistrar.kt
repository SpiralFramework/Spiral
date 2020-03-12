package info.spiralframework.console.jvm.commands

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.console.jvm.pipeline.PipelineContext

interface CommandRegistrar {
    suspend fun register(spiralContext: SpiralContext, pipelineContext: PipelineContext)
}