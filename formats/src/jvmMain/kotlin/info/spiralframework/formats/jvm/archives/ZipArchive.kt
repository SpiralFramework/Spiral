package info.spiralframework.formats.jvm.archives

import com.soywiz.krypto.sha256
import dev.brella.kornea.errors.common.doOnFailure
import dev.brella.kornea.errors.common.doOnSuccess
import dev.brella.kornea.errors.common.getOrElseRun
import dev.brella.kornea.errors.common.map
import dev.brella.kornea.errors.common.switchIfFailure
import dev.brella.kornea.io.common.BinaryDataPool
import dev.brella.kornea.io.common.BinaryDataSource
import dev.brella.kornea.toolkit.common.useAndFlatMap
import dev.brella.kornea.toolkit.common.useAndMap
import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.toHexString
import info.spiralframework.formats.common.archives.SpiralArchive
import info.spiralframework.formats.common.archives.SpiralArchiveSubfile
import info.spiralframework.formats.common.compression.decompressSpcData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.zip.ZipFile

class ZipArchive(val zipFile: ZipFile) : SpiralArchive {
    override val fileCount: Int
        get() = zipFile.entries().asSequence().count { entry -> !entry.isDirectory }

    override suspend fun SpiralContext.getSubfiles(): Flow<SpiralArchiveSubfile<*>> =
        zipFile.entries()
            .asIterator()
            .asFlow()
            .filter { entry -> !entry.isDirectory }
            .map { entry ->
                val data = withContext(Dispatchers.IO) { zipFile.getInputStream(entry).use(InputStream::readAllBytes) }
                val cache = cacheShortTerm(data.sha256().toHexString())

                SpiralArchiveSubfile(
                    entry.name,
                    cache.openOutputFlow()
                        .useAndMap { flow -> flow.write(data); cache }
                        .doOnFailure { cache.close() }
                        .getOrElseRun { BinaryDataSource(data) }
                )
            }
}