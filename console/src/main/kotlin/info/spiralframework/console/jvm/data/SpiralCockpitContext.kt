package info.spiralframework.console.jvm.data

import info.spiralframework.base.common.config.SpiralConfig
import info.spiralframework.base.common.environment.SpiralEnvironment
import info.spiralframework.base.common.events.SpiralEventBus
import info.spiralframework.base.common.io.SpiralCacheProvider
import info.spiralframework.base.common.io.SpiralResourceLoader
import info.spiralframework.base.common.locale.SpiralLocale
import info.spiralframework.base.common.logging.SpiralLogger
import info.spiralframework.core.SpiralCoreConfig
import info.spiralframework.core.SpiralCoreContext
import info.spiralframework.core.SpiralHttp
import info.spiralframework.core.plugins.SpiralPluginRegistry
import info.spiralframework.core.security.SpiralSignatures
import info.spiralframework.core.serialisation.SpiralSerialisation

@ExperimentalUnsignedTypes
interface SpiralCockpitContext: SpiralCoreContext {
    val args: GurrenArgs

    suspend fun copy(newArgs: GurrenArgs? = null, newCore: SpiralCoreConfig? = null, newLocale: SpiralLocale? = null, newLogger: SpiralLogger? = null, newConfig: SpiralConfig? = null, newEnvironment: SpiralEnvironment? = null, newEventBus: SpiralEventBus? = null, newCacheProvider: SpiralCacheProvider? = null, newResourceLoader: SpiralResourceLoader? = null, newSignatures: SpiralSignatures? = null, newPluginRegistry: SpiralPluginRegistry? = null, newSerialisation: SpiralSerialisation? = null, newHttp: SpiralHttp? = null): SpiralCockpitContext
}