package info.spiralframework.base.common.io

import info.spiralframework.base.common.SpiralContext
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalUnsignedTypes
interface SpiralShortTermCacheProvider {
    object Memory: SpiralShortTermCacheProvider {
        override fun supportsShortTermCaching(): Boolean = true
        override fun SpiralContext.cacheShortTerm(name: String): DataPool<out InputFlow, out OutputFlow> = BinaryDataPool()
    }

    fun supportsShortTermCaching(): Boolean
    fun SpiralContext.cacheShortTerm(name: String): DataPool<out InputFlow, out OutputFlow>
}

@ExperimentalUnsignedTypes
interface SpiralPersistentCacheProvider {
    object Memory: SpiralPersistentCacheProvider {
        override fun supportsPersistentCaching(): Boolean = true
        override fun SpiralContext.cachePersistent(name: String): DataPool<out InputFlow, out OutputFlow> = BinaryDataPool()
    }

    fun supportsPersistentCaching(): Boolean
    fun SpiralContext.cachePersistent(name: String): DataPool<out InputFlow, out OutputFlow>
}

@ExperimentalUnsignedTypes
interface SpiralTimedCacheProvider {
    object Memory: SpiralTimedCacheProvider {
        override fun supportsTimedCaching(): Boolean = true
        @ExperimentalTime
        override fun SpiralContext.cacheFor(name: String, duration: Duration): DataPool<out InputFlow, out OutputFlow> = TimedDataPool(BinaryDataPool(), duration)
    }

    fun supportsTimedCaching(): Boolean
    @ExperimentalTime
    fun SpiralContext.cacheFor(name: String, duration: Duration): DataPool<out InputFlow, out OutputFlow>
}

@ExperimentalUnsignedTypes
interface SpiralCacheProvider: SpiralShortTermCacheProvider, SpiralPersistentCacheProvider, SpiralTimedCacheProvider