package info.spiralframework.base.binding

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.BinaryDataPool
import info.spiralframework.base.common.io.DataPool
import info.spiralframework.base.common.io.SpiralCacheProvider
import info.spiralframework.base.common.io.flow.InputFlow
import info.spiralframework.base.common.io.flow.OutputFlow
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalUnsignedTypes
actual class DefaultSpiralCacheProvider actual constructor() : SpiralCacheProvider {
    override fun supportsShortTermCaching(): Boolean = false

    override fun SpiralContext.cacheShortTerm(name: String): DataPool<out InputFlow, out OutputFlow> = BinaryDataPool()

    override fun supportsPersistentCaching(): Boolean = false

    override fun SpiralContext.cachePersistent(name: String): DataPool<out InputFlow, out OutputFlow> = BinaryDataPool()

    override fun supportsTimedCaching(): Boolean = false

    @ExperimentalTime
    override fun SpiralContext.cacheFor(name: String, duration: Duration): DataPool<out InputFlow, out OutputFlow> = BinaryDataPool()

    override fun prime(catalyst: SpiralContext) {}
}