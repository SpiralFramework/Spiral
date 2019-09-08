package info.spiralframework.base.common.io

import info.spiralframework.base.common.SpiralCatalyst
import info.spiralframework.base.common.SpiralContext
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalUnsignedTypes
interface SpiralShortTermCacheProvider {
    object Memory : SpiralShortTermCacheProvider {
        override fun supportsShortTermCaching(): Boolean = true
        override fun SpiralContext.cacheShortTerm(name: String): DataPool<out InputFlow, out OutputFlow> = BinaryDataPool()
    }

    fun supportsShortTermCaching(): Boolean
    fun SpiralContext.cacheShortTerm(name: String): DataPool<out InputFlow, out OutputFlow>
}

@ExperimentalUnsignedTypes
interface SpiralPersistentCacheProvider {
    object Memory : SpiralPersistentCacheProvider {
        override fun supportsPersistentCaching(): Boolean = true
        override fun SpiralContext.cachePersistent(name: String): DataPool<out InputFlow, out OutputFlow> = BinaryDataPool()
    }

    fun supportsPersistentCaching(): Boolean
    fun SpiralContext.cachePersistent(name: String): DataPool<out InputFlow, out OutputFlow>
}

@ExperimentalUnsignedTypes
interface SpiralTimedCacheProvider {
    object Memory : SpiralTimedCacheProvider {
        override fun supportsTimedCaching(): Boolean = true
        @ExperimentalTime
        override fun SpiralContext.cacheFor(name: String, duration: Duration): DataPool<out InputFlow, out OutputFlow> = TimedDataPool(BinaryDataPool(), duration)
    }

    fun supportsTimedCaching(): Boolean
    @ExperimentalTime
    fun SpiralContext.cacheFor(name: String, duration: Duration): DataPool<out InputFlow, out OutputFlow>
}

@ExperimentalUnsignedTypes
interface SpiralCacheProvider : SpiralShortTermCacheProvider, SpiralPersistentCacheProvider, SpiralTimedCacheProvider, SpiralCatalyst<SpiralContext> {
    object Memory : SpiralCacheProvider, SpiralShortTermCacheProvider by SpiralShortTermCacheProvider.Memory, SpiralPersistentCacheProvider by SpiralPersistentCacheProvider.Memory, SpiralTimedCacheProvider by SpiralTimedCacheProvider.Memory {
        override fun prime(catalyst: SpiralContext) {}
    }
}

@ExperimentalUnsignedTypes
fun SpiralShortTermCacheProvider.cacheShortTerm(context: SpiralContext, name: String): DataPool<out InputFlow, out OutputFlow> = context.cacheShortTerm(name)
@ExperimentalUnsignedTypes
fun SpiralPersistentCacheProvider.cachePersistent(context: SpiralContext, name: String): DataPool<out InputFlow, out OutputFlow> = context.cachePersistent(name)
@ExperimentalUnsignedTypes
@ExperimentalTime
fun SpiralTimedCacheProvider.cacheFor(context: SpiralContext, name: String, duration: Duration): DataPool<out InputFlow, out OutputFlow> = context.cacheFor(name, duration)