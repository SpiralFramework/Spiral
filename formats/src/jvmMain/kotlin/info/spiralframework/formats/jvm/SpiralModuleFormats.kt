package info.spiralframework.formats.jvm

import dev.brella.kornea.toolkit.common.SemanticVersion
import info.spiralframework.base.binding.*
import info.spiralframework.base.common.SPIRAL_VERSION
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.SpiralModuleProvider
import info.spiralframework.base.common.environment.registerAllModules
import info.spiralframework.base.common.locale.loadBundle
import info.spiralframework.base.common.serialisation.DefaultSpiralSerialisation
import kotlin.coroutines.CoroutineContext

public class SpiralModuleFormats : SpiralModuleProvider {
    override val moduleName: String = "spiral-formats"
    override val moduleVersion: SemanticVersion = SPIRAL_VERSION

    override suspend fun register(context: SpiralContext) {
        context.loadBundle<SpiralModuleFormats>(context, "SpiralFormats")
    }
}

public suspend fun defaultSpiralContextWithFormats(parentCoroutineContext: CoroutineContext? = null): SpiralContext {
    val context = DefaultSpiralContext(
        DefaultSpiralLocale(),
        DefaultSpiralLogger("DefaultSpiral"),
        DefaultSpiralConfig(),
        DefaultSpiralEnvironment(),
        DefaultSpiralEventBus(),
        DefaultSpiralCacheProvider(),
        DefaultSpiralResourceLoader(),
        DefaultSpiralSerialisation(),
        parentCoroutineContext
    )

    context.addModuleProvider(SpiralModuleFormats())
    context.registerAllModules(context)
    return context
}