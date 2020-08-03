package info.spiralframework.base.binding

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.SpiralModuleBase
import info.spiralframework.base.common.SpiralModuleProvider
import info.spiralframework.base.common.config.SpiralConfig
import info.spiralframework.base.common.environment.SpiralEnvironment
import info.spiralframework.base.common.events.SpiralEventBus
import info.spiralframework.base.common.io.SpiralCacheProvider
import info.spiralframework.base.common.io.SpiralResourceLoader
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.logging.SpiralLogger
import dev.brella.kornea.toolkit.common.SemanticVersion
import java.util.*

@ExperimentalUnsignedTypes
actual data class DefaultSpiralContext private actual constructor(
        val locale: SpiralLocale,
        val logger: SpiralLogger,
        val config: SpiralConfig,
        val environment: SpiralEnvironment,
        val eventBus: SpiralEventBus,
        val cacheProvider: SpiralCacheProvider,
        val resourceLoader: SpiralResourceLoader
) : SpiralContext,
        SpiralLocale by locale,
        SpiralLogger by logger,
        SpiralConfig by config,
        SpiralEnvironment by environment,
        SpiralEventBus by eventBus,
        SpiralCacheProvider by cacheProvider,
        SpiralResourceLoader by resourceLoader {
    actual companion object {
        actual suspend operator fun invoke(locale: SpiralLocale, logger: SpiralLogger, config: SpiralConfig, environment: SpiralEnvironment, eventBus: SpiralEventBus, cacheProvider: SpiralCacheProvider, resourceLoader: SpiralResourceLoader): DefaultSpiralContext {
            val context = DefaultSpiralContext(locale, logger, config, environment, eventBus, cacheProvider, resourceLoader)
            context.init()
            return context
        }
    }

    override fun subcontext(module: String): SpiralContext = this
    override suspend fun copy(newLocale: SpiralLocale?, newLogger: SpiralLogger?, newConfig: SpiralConfig?, newEnvironment: SpiralEnvironment?, newEventBus: SpiralEventBus?, newCacheProvider: SpiralCacheProvider?, newResourceLoader: SpiralResourceLoader?): SpiralContext {
        val context =
                DefaultSpiralContext(
                        newLocale ?: locale,
                        newLogger ?: logger,
                        newConfig ?: config,
                        newEnvironment ?: environment,
                        newEventBus ?: eventBus,
                        newCacheProvider ?: cacheProvider,
                        newResourceLoader ?: resourceLoader
                )
        context.init()
        return context
    }

    val moduleLoader: ServiceLoader<SpiralModuleProvider> = ServiceLoader.load(SpiralModuleProvider::class.java)
    override val loadedModules: Map<String, SemanticVersion> = moduleLoader.iterator()
            .asSequence()
            .map { module -> Pair(module.moduleName, module.moduleVersion) }
            .toMap()

    override fun prime(catalyst: SpiralContext) {
        config.prime(catalyst)
        cacheProvider.prime(this)
    }

    actual suspend fun init() {
        addModuleProvider(SpiralModuleBase())
        registerAllModules()
        //moduleLoader.iterator().forEach { module -> module.register(this) }

        prime(this)
    }
}