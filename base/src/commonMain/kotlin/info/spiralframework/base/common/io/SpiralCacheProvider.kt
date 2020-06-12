package info.spiralframework.base.common.io

import info.spiralframework.base.common.SpiralCatalyst
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.putBack
import org.abimon.kornea.erorrs.common.flatMap
import org.abimon.kornea.erorrs.common.getOrElse
import org.abimon.kornea.erorrs.common.map
import org.abimon.kornea.io.common.*
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
        override suspend fun SpiralContext.cacheShortTerm(name: String, location: String?): DataPool<out InputFlow, out OutputFlow> {
            val pool = memCaches.getOrPut(name) { newDataPool(location) }
            if (pool.isClosed) {
                val newPool = newDataPool(location)
                memCaches[name] = newPool
                return newPool
            }

            return pool
        }

        fun newDataPool(location: String?): DataPool<out InputFlow, out OutputFlow> = BinaryDataPool(location)
    }

    fun supportsShortTermCaching(): Boolean
    suspend fun SpiralContext.isCachedShortTerm(name: String): Boolean
    suspend fun SpiralContext.cacheShortTerm(name: String, location: String? = null): DataPool<out InputFlow, out OutputFlow>
}

@ExperimentalUnsignedTypes
interface SpiralPersistentCacheProvider {
    class Memory : SpiralPersistentCacheProvider {
        private val memCaches: MutableMap<String, DataPool<out InputFlow, out OutputFlow>> = HashMap()

        override fun supportsPersistentCaching(): Boolean = true
        override suspend fun SpiralContext.isCachedPersistent(name: String): Boolean = memCaches[name]?.isClosed == false
        override suspend fun SpiralContext.cachePersistent(name: String, location: String?): DataPool<out InputFlow, out OutputFlow> {
            val pool = memCaches.getOrPut(name) { newDataPool(location) }
            if (pool.isClosed) {
                val newPool = newDataPool(location)
                memCaches[name] = newPool
                return newPool
            }

            return pool
        }

        fun newDataPool(location: String?): DataPool<out InputFlow, out OutputFlow> = BinaryDataPool(location = location)
    }

    fun supportsPersistentCaching(): Boolean
    suspend fun SpiralContext.isCachedPersistent(name: String): Boolean
    suspend fun SpiralContext.cachePersistent(name: String, location: String? = null): DataPool<out InputFlow, out OutputFlow>
}

@ExperimentalUnsignedTypes
interface SpiralTimedCacheProvider {
    class Memory : SpiralTimedCacheProvider {
        private val memCaches: MutableMap<String, DataPool<out InputFlow, out OutputFlow>> = HashMap()

        override fun supportsTimedCaching(): Boolean = true
        override suspend fun SpiralContext.isCachedTimed(name: String): Boolean = memCaches[name]?.isClosed == false

        @ExperimentalTime
        override suspend fun SpiralContext.cacheFor(name: String, duration: Duration, location: String?): DataPool<out InputFlow, out OutputFlow> {
            val pool = memCaches[name] ?: memCaches.putBack(name, newDataPool(duration, location))
            if (pool.isClosed) {
                val newPool = newDataPool(duration, location)
                memCaches[name] = newPool
                return newPool
            }

            return pool
        }

        @ExperimentalTime
        fun newDataPool(duration: Duration, location: String?): DataPool<out InputFlow, out OutputFlow> = TimedDataPool(BinaryDataPool(location = location), duration)
    }

    fun supportsTimedCaching(): Boolean
    suspend fun SpiralContext.isCachedTimed(name: String): Boolean

    @ExperimentalTime
    suspend fun SpiralContext.cacheFor(name: String, duration: Duration, location: String? = null): DataPool<out InputFlow, out OutputFlow>
}

@ExperimentalUnsignedTypes
interface SpiralCacheProvider : SpiralShortTermCacheProvider, SpiralPersistentCacheProvider, SpiralTimedCacheProvider, SpiralCatalyst<SpiralContext> {
    class Memory() : SpiralCacheProvider, SpiralShortTermCacheProvider by SpiralShortTermCacheProvider.Memory(), SpiralPersistentCacheProvider by SpiralPersistentCacheProvider.Memory(), SpiralTimedCacheProvider by SpiralTimedCacheProvider.Memory() {
        override fun prime(catalyst: SpiralContext) {}
    }
}

@ExperimentalUnsignedTypes
suspend fun SpiralShortTermCacheProvider.cacheShortTerm(context: SpiralContext, name: String, location: String? = null): DataPool<out InputFlow, out OutputFlow> = context.cacheShortTerm(name, location)

@ExperimentalUnsignedTypes
suspend fun SpiralPersistentCacheProvider.cachePersistent(context: SpiralContext, name: String, location: String? = null): DataPool<out InputFlow, out OutputFlow> = context.cachePersistent(name, location)

@ExperimentalUnsignedTypes
@ExperimentalTime
suspend fun SpiralTimedCacheProvider.cacheFor(context: SpiralContext, name: String, duration: Duration, location: String? = null): DataPool<out InputFlow, out OutputFlow> = context.cacheFor(name, duration, location)

@Suppress("USELESS_CAST")
suspend fun <T: InputFlow> DataSource<T>.cache(context: SpiralContext): DataSource<*> {
    val cache = context.cacheShortTerm(context, this.location ?: this.toString(), this.location)

    return cache.openOutputFlow()
            .flatMap { out -> this.useInputFlow { flow -> flow.copyTo(out) } }
            .map { cache as DataSource<*> }
            .getOrElse(this)
}