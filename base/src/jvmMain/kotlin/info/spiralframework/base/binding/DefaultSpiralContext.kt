package info.spiralframework.base.binding

import info.spiralframework.base.SpiralModuleProvider
import info.spiralframework.base.common.SemanticVersion
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.config.SpiralConfig
import info.spiralframework.base.common.environment.SpiralEnvironment
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.logging.SpiralLogger
import java.util.*

actual data class DefaultSpiralContext actual constructor(
        val locale: SpiralLocale,
        val logger: SpiralLogger,
        val config: SpiralConfig,
        val environment: SpiralEnvironment
) : SpiralContext,
        SpiralLocale by locale,
        SpiralLogger by logger,
        SpiralConfig by config,
        SpiralEnvironment by environment {
    override fun subcontext(module: String): SpiralContext = this
    override fun copy(newLocale: SpiralLocale?, newLogger: SpiralLogger?, newConfig: SpiralConfig?, newEnvironment: SpiralEnvironment?): SpiralContext =
            DefaultSpiralContext(
                    newLocale ?: locale,
                    newLogger ?: logger,
                    newConfig ?: config,
                    newEnvironment ?: environment
            )

    val moduleLoader: ServiceLoader<SpiralModuleProvider> = ServiceLoader.load(SpiralModuleProvider::class.java)
    override val loadedModules: Map<String, SemanticVersion> = moduleLoader.iterator()
            .asSequence()
            .map { module -> Pair(module.moduleName, module.moduleVersion) }
            .toMap()

    init {
        moduleLoader.iterator().forEach { module -> module.register(this) }
        config.prime(this)
    }
}