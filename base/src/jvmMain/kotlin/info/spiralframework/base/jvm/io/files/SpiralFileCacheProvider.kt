package info.spiralframework.base.jvm.io.files

import info.spiralframework.base.common.SpiralCatalyst
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.SpiralCacheProvider
import info.spiralframework.base.common.io.TimedDataPool
import org.abimon.kornea.annotations.ExperimentalKorneaIO
import org.abimon.kornea.io.common.DataPool
import org.abimon.kornea.io.common.flow.InputFlow
import org.abimon.kornea.io.common.flow.OutputFlow
import org.abimon.kornea.io.jvm.files.AsyncFileDataPool
import java.io.File
import kotlin.concurrent.thread
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalKorneaIO
@ExperimentalUnsignedTypes
class SpiralFileCacheProvider(): SpiralCacheProvider, SpiralCatalyst<SpiralContext> {
    private lateinit var shortTermDir: File
    private lateinit var persistentDir: File

    override fun supportsShortTermCaching(): Boolean = true
    override fun supportsPersistentCaching(): Boolean = true
    override fun supportsTimedCaching(): Boolean = true

    override suspend fun SpiralContext.isCachedShortTerm(name: String): Boolean = File(shortTermDir, name).exists()

    override suspend fun SpiralContext.cacheShortTerm(name: String, location: String?): DataPool<out InputFlow, out OutputFlow> =
            ShortTermFileDataPool(File(shortTermDir, name), location)

    override suspend fun SpiralContext.isCachedPersistent(name: String): Boolean = File(persistentDir, name).exists()

    override suspend fun SpiralContext.cachePersistent(name: String, location: String?): DataPool<out InputFlow, out OutputFlow> =
            AsyncFileDataPool(File(persistentDir, name))

    override suspend fun SpiralContext.isCachedTimed(name: String): Boolean = isCachedShortTerm(name)

    @ExperimentalTime
    override suspend fun SpiralContext.cacheFor(name: String, duration: Duration, location: String?): DataPool<out InputFlow, out OutputFlow> =
            TimedDataPool(ShortTermFileDataPool(File(shortTermDir, name)), duration)

    override fun prime(catalyst: SpiralContext) {
        val upperDir = with(catalyst) { File(getLocalDataDir("cache")) }
        shortTermDir = File(upperDir, "short term")
        persistentDir = File(upperDir, "persistent")

        shortTermDir.mkdirs()
        persistentDir.mkdirs()

        Runtime.getRuntime().addShutdownHook(thread(start = false, block = this::purgeShortTerm))
        purgeShortTerm()
    }

    fun purgeShortTerm() { shortTermDir.listFiles()?.forEach { file -> file.delete() } }
}