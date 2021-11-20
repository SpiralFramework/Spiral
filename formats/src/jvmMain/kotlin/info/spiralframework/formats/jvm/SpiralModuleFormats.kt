package info.spiralframework.formats.jvm

import dev.brella.kornea.toolkit.common.SemanticVersion
import info.spiralframework.base.binding.DefaultSpiralCacheProvider
import info.spiralframework.base.binding.DefaultSpiralConfig
import info.spiralframework.base.binding.DefaultSpiralContext
import info.spiralframework.base.binding.DefaultSpiralEnvironment
import info.spiralframework.base.binding.DefaultSpiralEventBus
import info.spiralframework.base.binding.DefaultSpiralLocale
import info.spiralframework.base.binding.DefaultSpiralLogger
import info.spiralframework.base.binding.DefaultSpiralResourceLoader
import info.spiralframework.base.common.SPIRAL_VERSION
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.SpiralModuleProvider
import info.spiralframework.base.common.environment.registerAllModules
import info.spiralframework.base.common.locale.loadBundle
import info.spiralframework.base.common.serialisation.DefaultSpiralSerialisation

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
    val context = DefaultSpiralContext(DefaultSpiralLocale(), DefaultSpiralLogger("DefaultSpiral"), DefaultSpiralConfig(), DefaultSpiralEnvironment(), DefaultSpiralEventBus(), DefaultSpiralCacheProvider(), DefaultSpiralResourceLoader(), DefaultSpiralSerialisation())
    context.addModuleProvider(SpiralModuleFormats())
    context.registerAllModules(context)
    return context
}