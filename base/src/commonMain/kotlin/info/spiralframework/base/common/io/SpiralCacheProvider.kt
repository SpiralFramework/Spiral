package info.spiralframework.base.common.io

import dev.brella.kornea.annotations.ExperimentalKorneaToolkit
import dev.brella.kornea.errors.common.flatMap
import dev.brella.kornea.errors.common.getOrDefault
import dev.brella.kornea.errors.common.map
import dev.brella.kornea.io.common.*
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.extensions.copyTo
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.putBack
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

public interface SpiralShortTermCacheProvider {
    public class Memory : SpiralShortTermCacheProvider {
        private val memCaches: MutableMap<String, DataPool<InputFlow, OutputFlow>> = HashMap()

        override fun supportsShortTermCaching(): Boolean = true
        override suspend fun SpiralContext.isCachedShortTerm(name: String): Boolean = memCaches[name]?.isClosed == false
        override suspend fun SpiralContext.cacheShortTerm(name: String, location: String?): DataPool<InputFlow, OutputFlow> {
            val pool = memCaches.getOrPut(name) { newDataPool(location) }
            if (pool.isClosed) {
                val newPool = newDataPool(location)
                memCaches[name] = newPool
                return newPool
            }

            return pool
        }

        @OptIn(ExperimentalKorneaToolkit::class)
        public suspend fun newDataPool(location: String?): DataPool<InputFlow, OutputFlow> = BinaryDataPool(location, null, 8)
    }

    public fun supportsShortTermCaching(): Boolean
    public suspend fun SpiralContext.isCachedShortTerm(name: String): Boolean
    public suspend fun SpiralContext.cacheShortTerm(name: String, location: String? = "CacheShortTerm[$name]"): DataPool<InputFlow, OutputFlow>
}

public interface SpiralPersistentCacheProvider {
    public class Memory : SpiralPersistentCacheProvider {
        private val memCaches: MutableMap<String, DataPool<InputFlow, OutputFlow>> = HashMap()

        override fun supportsPersistentCaching(): Boolean = true
        override suspend fun SpiralContext.isCachedPersistent(name: String): Boolean = memCaches[name]?.isClosed == false
        override suspend fun SpiralContext.cachePersistent(name: String, location: String?): DataPool<InputFlow, OutputFlow> {
            val pool = memCaches.getOrPut(name) { newDataPool(location) }
            if (pool.isClosed) {
                val newPool = newDataPool(location)
                memCaches[name] = newPool
                return newPool
            }

            return pool
        }

        @OptIn(ExperimentalKorneaToolkit::class)
        public suspend fun newDataPool(location: String?): DataPool<InputFlow, OutputFlow> = BinaryDataPool(location = location)
    }

    public fun supportsPersistentCaching(): Boolean
    public suspend fun SpiralContext.isCachedPersistent(name: String): Boolean
    public suspend fun SpiralContext.cachePersistent(name: String, location: String? = "CachePersist[$name]"): DataPool<InputFlow, OutputFlow>
}

public interface SpiralTimedCacheProvider {
    public class Memory : SpiralTimedCacheProvider {
        private val memCaches: MutableMap<String, DataPool<InputFlow, OutputFlow>> = HashMap()

        override fun supportsTimedCaching(): Boolean = true
        override suspend fun SpiralContext.isCachedTimed(name: String): Boolean = memCaches[name]?.isClosed == false

        @ExperimentalTime
        override suspend fun SpiralContext.cacheFor(name: String, duration: Duration, scope: CoroutineScope, location: String?): DataPool<InputFlow, OutputFlow> {
            val pool = memCaches[name] ?: memCaches.putBack(name, newDataPool(duration, scope, location))
            if (pool.isClosed) {
                val newPool = newDataPool(duration, scope, location)
                memCaches[name] = newPool
                return newPool
            }

            return pool
        }

        @OptIn(ExperimentalKorneaToolkit::class)
        @ExperimentalTime
        public suspend fun newDataPool(duration: Duration, scope: CoroutineScope, location: String?): DataPool<InputFlow, OutputFlow> = TimedDataPool(BinaryDataPool(location = location), duration, scope)
    }

    public fun supportsTimedCaching(): Boolean
    public suspend fun SpiralContext.isCachedTimed(name: String): Boolean

    @ExperimentalTime
    public suspend fun SpiralContext.cacheFor(name: String, duration: Duration, scope: CoroutineScope = this, location: String? = "CacheTimed[$name,$duration]"): DataPool<InputFlow, OutputFlow>
}

public interface SpiralCacheProvider : SpiralShortTermCacheProvider, SpiralPersistentCacheProvider, SpiralTimedCacheProvider {
    public class Memory : SpiralCacheProvider, SpiralShortTermCacheProvider by SpiralShortTermCacheProvider.Memory(), SpiralPersistentCacheProvider by SpiralPersistentCacheProvider.Memory(), SpiralTimedCacheProvider by SpiralTimedCacheProvider.Memory()
}

public suspend fun SpiralShortTermCacheProvider.cacheShortTerm(context: SpiralContext, name: String, location: String? = "CacheShortTerm[$name]"): DataPool<InputFlow, OutputFlow> = context.cacheShortTerm(name, location)
public suspend fun SpiralPersistentCacheProvider.cachePersistent(context: SpiralContext, name: String, location: String? = "CachePersist[$name]"): DataPool<InputFlow, OutputFlow> = context.cachePersistent(name, location)

@ExperimentalTime
public suspend fun SpiralTimedCacheProvider.cacheFor(context: SpiralContext, name: String, duration: Duration, scope: CoroutineScope, location: String? = "CacheTimed[$name,$duration]"): DataPool<InputFlow, OutputFlow> = context.cacheFor(name, duration, scope, location)

@Suppress("USELESS_CAST")
public suspend fun <T: InputFlow> DataSource<T>.cache(context: SpiralContext): DataSource<*> {
    val cache = context.cacheShortTerm(context, this.location ?: this.toString(), this.location)

    return cache.openOutputFlow()
            .flatMap { out -> this.useInputFlow { flow -> flow.copyTo(out) } }
            .map { cache as DataSource<*> }
            .getOrDefault(this)
}