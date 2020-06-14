package info.spiralframework.formats.jvm

import info.spiralframework.base.binding.*
import info.spiralframework.base.common.SPIRAL_VERSION
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.SpiralModuleProvider
import info.spiralframework.base.common.environment.registerAllModules
import info.spiralframework.base.common.locale.loadBundle
import org.kornea.toolkit.common.SemanticVersion

@ExperimentalUnsignedTypes
class SpiralModuleFormats: SpiralModuleProvider {
    override val moduleName: String = "spiral-formats"
    override val moduleVersion: SemanticVersion = SPIRAL_VERSION

    override suspend fun register(context: SpiralContext) {
        context.loadBundle<SpiralModuleFormats>(context, "SpiralFormats")
    }
}

@ExperimentalUnsignedTypes
suspend fun defaultSpiralContextWithFormats(): SpiralContext {
    val context = DefaultSpiralContext(DefaultSpiralLocale(), DefaultSpiralLogger("DefaultSpiral"), DefaultSpiralConfig(), DefaultSpiralEnvironment(), DefaultSpiralEventBus(), DefaultSpiralCacheProvider(), DefaultSpiralResourceLoader())
    context.addModuleProvider(SpiralModuleFormats())
    context.registerAllModules(context)
    return context
}