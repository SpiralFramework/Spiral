package info.spiralframework.base.binding

import dev.brella.kornea.toolkit.common.SemanticVersion
import dev.brella.kornea.toolkit.common.SuspendInit0
import info.spiralframework.base.common.SpiralCatalyst
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.SpiralModuleBase
import info.spiralframework.base.common.config.SpiralConfig
import info.spiralframework.base.common.environment.SpiralEnvironment
import info.spiralframework.base.common.events.SpiralEventBus
import info.spiralframework.base.common.io.SpiralCacheProvider
import info.spiralframework.base.common.io.SpiralResourceLoader
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.logging.SpiralLogger
import info.spiralframework.base.common.serialisation.SpiralSerialisation
import info.spiralframework.base.common.tryPrime
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

public actual data class DefaultSpiralContext actual constructor(
    val locale: SpiralLocale,
    val logger: SpiralLogger,
    val config: SpiralConfig,
    val environment: SpiralEnvironment,
    val eventBus: SpiralEventBus,
    val cacheProvider: SpiralCacheProvider,
    val resourceLoader: SpiralResourceLoader,
    val serialisation: SpiralSerialisation,
    val parentCoroutineContext: CoroutineContext?,
) : SpiralContext, SuspendInit0, SpiralCatalyst<SpiralContext>,
    SpiralLocale by locale,
    SpiralLogger by logger,
    SpiralConfig by config,
    SpiralEnvironment by environment,
    SpiralEventBus by eventBus,
    SpiralCacheProvider by cacheProvider,
    SpiralResourceLoader by resourceLoader,
    SpiralSerialisation by serialisation {
    public actual companion object {
        public actual suspend operator fun invoke(
            locale: SpiralLocale,
            logger: SpiralLogger,
            config: SpiralConfig,
            environment: SpiralEnvironment,
            eventBus: SpiralEventBus,
            cacheProvider: SpiralCacheProvider,
            resourceLoader: SpiralResourceLoader,
            serialisation: SpiralSerialisation,
            parentCoroutineContext: CoroutineContext?
        ): DefaultSpiralContext {
            val context = DefaultSpiralContext(
                locale,
                logger,
                config,
                environment,
                eventBus,
                cacheProvider,
                resourceLoader,
                serialisation,
                parentCoroutineContext
            )
            context.init()
            return context
        }
    }

    override val coroutineContext: CoroutineContext = parentCoroutineContext?.plus(SupervisorJob()) ?: SupervisorJob()
    override val loadedModules: Map<String, SemanticVersion> = emptyMap()
    override val klass: KClass<SpiralContext> = SpiralContext::class
    private var primed: Boolean = false

    override fun subcontext(module: String): SpiralContext = this
    override suspend fun copy(
        newLocale: SpiralLocale?,
        newLogger: SpiralLogger?,
        newConfig: SpiralConfig?,
        newEnvironment: SpiralEnvironment?,
        newEventBus: SpiralEventBus?,
        newCacheProvider: SpiralCacheProvider?,
        newResourceLoader: SpiralResourceLoader?,
        newSerialisation: SpiralSerialisation?,
        newParentCoroutineContext: CoroutineContext?
    ): SpiralContext {
        val context =
            DefaultSpiralContext(
                newLocale ?: locale,
                newLogger ?: logger,
                newConfig ?: config,
                newEnvironment ?: environment,
                newEventBus ?: eventBus,
                newCacheProvider ?: cacheProvider,
                newResourceLoader ?: resourceLoader,
                newSerialisation ?: serialisation,
                newParentCoroutineContext ?: parentCoroutineContext
            )
        context.init()
        return context
    }

    override suspend fun prime(catalyst: SpiralContext) {
        if (!primed) {
            tryPrime(locale)
            tryPrime(logger)
            tryPrime(config)
            tryPrime(environment)
            tryPrime(eventBus)
            tryPrime(cacheProvider)
            tryPrime(resourceLoader)
            tryPrime(serialisation)

            primed = false
        }
    }

    actual override suspend fun init() {
        addModuleProvider(SpiralModuleBase())
        registerAllModules()
        //moduleLoader.iterator().forEach { module -> module.register(this) }

        prime(this)
    }
}