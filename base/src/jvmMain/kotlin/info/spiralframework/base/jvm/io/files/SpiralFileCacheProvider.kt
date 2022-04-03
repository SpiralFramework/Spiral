package info.spiralframework.base.jvm.io.files

import dev.brella.kornea.io.common.DataPool
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.jvm.files.AsyncFileDataPool
import info.spiralframework.base.common.SpiralCatalyst
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.SpiralCacheProvider
import info.spiralframework.base.common.io.TimedDataPool
import kotlinx.coroutines.CoroutineScope
import java.io.File
import kotlin.concurrent.thread
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

public class SpiralFileCacheProvider : SpiralCacheProvider, SpiralCatalyst<SpiralContext> {
    private lateinit var shortTermDir: File
    private lateinit var persistentDir: File

    override val klass: KClass<SpiralContext> = SpiralContext::class
    private var primed: Boolean = false

    override fun supportsShortTermCaching(): Boolean = true
    override fun supportsPersistentCaching(): Boolean = true
    override fun supportsTimedCaching(): Boolean = true

    override suspend fun SpiralContext.isCachedShortTerm(name: String): Boolean = File(shortTermDir, name).exists()

    override suspend fun SpiralContext.cacheShortTerm(name: String, location: String?): DataPool<InputFlow, OutputFlow> =
            ShortTermFileDataPool(File(shortTermDir, name), location)

    override suspend fun SpiralContext.isCachedPersistent(name: String): Boolean = File(persistentDir, name).exists()

    override suspend fun SpiralContext.cachePersistent(name: String, location: String?): DataPool<InputFlow, OutputFlow> =
            AsyncFileDataPool(File(persistentDir, name))

    override suspend fun SpiralContext.isCachedTimed(name: String): Boolean = isCachedShortTerm(name)

    @ExperimentalTime
    override suspend fun SpiralContext.cacheFor(name: String, duration: Duration, scope: CoroutineScope, location: String?): DataPool<InputFlow, OutputFlow> =
            TimedDataPool(ShortTermFileDataPool(File(shortTermDir, name)), duration, scope)

    override suspend fun prime(catalyst: SpiralContext) {
        if (!primed) {
            val upperDir = with(catalyst) { File(getLocalDataDir("cache")) }
            shortTermDir = File(upperDir, "short term")
            persistentDir = File(upperDir, "persistent")

            shortTermDir.mkdirs()
            persistentDir.mkdirs()

            Runtime.getRuntime().addShutdownHook(thread(start = false, block = this::purgeShortTerm))
            purgeShortTerm()

            primed = true
        }
    }

    public fun purgeShortTerm() { shortTermDir.listFiles()?.forEach { file -> file.delete() } }
}