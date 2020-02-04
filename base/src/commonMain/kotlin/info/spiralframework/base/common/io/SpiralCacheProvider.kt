package info.spiralframework.base.common.io

import info.spiralframework.base.common.SpiralCatalyst
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.putBack
import org.abimon.kornea.io.common.BinaryDataPool
import org.abimon.kornea.io.common.DataPool
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.OutputFlow
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalUnsignedTypes
interface SpiralShortTermCacheProvider {
    class Memory : SpiralShortTermCacheProvider {
        private val memCaches: MutableMap<String, DataPool<out InputFlow, out OutputFlow>> = HashMap()

        override fun supportsShortTermCaching(): Boolean = true
        override suspend fun SpiralContext.isCachedShortTerm(name: String): Boolean = memCaches[name]?.isClosed == false
        override suspend fun SpiralContext.cacheShortTerm(name: String): DataPool<out InputFlow, out OutputFlow> {
            val pool = memCaches.getOrPut(name, this@Memory::newDataPool)
            if (pool.isClosed) {
                val newPool = newDataPool()
                memCaches[name] = newPool
                return newPool
            }

            return pool
        }

        fun newDataPool(): DataPool<out InputFlow, out OutputFlow> = BinaryDataPool()
    }

    fun supportsShortTermCaching(): Boolean
    suspend fun SpiralContext.isCachedShortTerm(name: String): Boolean
    suspend fun SpiralContext.cacheShortTerm(name: String): DataPool<out InputFlow, out OutputFlow>
}

@ExperimentalUnsignedTypes
interface SpiralPersistentCacheProvider {
    class Memory : SpiralPersistentCacheProvider {
        private val memCaches: MutableMap<String, DataPool<out InputFlow, out OutputFlow>> = HashMap()

        override fun supportsPersistentCaching(): Boolean = true
        override suspend fun SpiralContext.isCachedPersistent(name: String): Boolean = memCaches[name]?.isClosed == false
        override suspend fun SpiralContext.cachePersistent(name: String): DataPool<out InputFlow, out OutputFlow> {
            val pool = memCaches.getOrPut(name, this@Memory::newDataPool)
            if (pool.isClosed) {
                val newPool = newDataPool()
                memCaches[name] = newPool
                return newPool
            }

            return pool
        }

        fun newDataPool(): DataPool<out InputFlow, out OutputFlow> = BinaryDataPool()
    }

    fun supportsPersistentCaching(): Boolean
    suspend fun SpiralContext.isCachedPersistent(name: String): Boolean
    suspend fun SpiralContext.cachePersistent(name: String): DataPool<out InputFlow, out OutputFlow>
}

@ExperimentalUnsignedTypes
interface SpiralTimedCacheProvider {
    class Memory : SpiralTimedCacheProvider {
        private val memCaches: MutableMap<String, DataPool<out InputFlow, out OutputFlow>> = HashMap()

        override fun supportsTimedCaching(): Boolean = true
        override suspend fun SpiralContext.isCachedTimed(name: String): Boolean = memCaches[name]?.isClosed == false
        @ExperimentalTime
        override suspend fun SpiralContext.cacheFor(name: String, duration: Duration): DataPool<out InputFlow, out OutputFlow> {
            val pool = memCaches[name] ?: memCaches.putBack(name, newDataPool(duration))
            if (pool.isClosed) {
                val newPool = newDataPool(duration)
                memCaches[name] = newPool
                return newPool
            }

            return pool
        }

        @ExperimentalTime
        fun newDataPool(duration: Duration): DataPool<out InputFlow, out OutputFlow> = TimedDataPool(BinaryDataPool(), duration)
    }

    fun supportsTimedCaching(): Boolean
    suspend fun SpiralContext.isCachedTimed(name: String): Boolean
    @ExperimentalTime
    suspend fun SpiralContext.cacheFor(name: String, duration: Duration): DataPool<out InputFlow, out OutputFlow>
}

@ExperimentalUnsignedTypes
interface SpiralCacheProvider : SpiralShortTermCacheProvider, SpiralPersistentCacheProvider, SpiralTimedCacheProvider, SpiralCatalyst<SpiralContext> {
    class Memory() : SpiralCacheProvider, SpiralShortTermCacheProvider by SpiralShortTermCacheProvider.Memory(), SpiralPersistentCacheProvider by SpiralPersistentCacheProvider.Memory(), SpiralTimedCacheProvider by SpiralTimedCacheProvider.Memory() {
        override fun prime(catalyst: SpiralContext) {}
    }
}

@ExperimentalUnsignedTypes
suspend fun SpiralShortTermCacheProvider.cacheShortTerm(context: SpiralContext, name: String): DataPool<out InputFlow, out OutputFlow> = context.cacheShortTerm(name)
@ExperimentalUnsignedTypes
suspend fun SpiralPersistentCacheProvider.cachePersistent(context: SpiralContext, name: String): DataPool<out InputFlow, out OutputFlow> = context.cachePersistent(name)
@ExperimentalUnsignedTypes
@ExperimentalTime
suspend fun SpiralTimedCacheProvider.cacheFor(context: SpiralContext, name: String, duration: Duration): DataPool<out InputFlow, out OutputFlow> = context.cacheFor(name, duration)