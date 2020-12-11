package info.spiralframework.base.jvm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.io.common.*
import dev.brella.kornea.io.common.DataSource.Companion.korneaSourceClosed
import dev.brella.kornea.io.common.DataSource.Companion.korneaSourceUnknown
import dev.brella.kornea.io.common.DataSource.Companion.korneaTooManySourcesOpen
import dev.brella.kornea.io.jvm.JVMDataSource
import dev.brella.kornea.io.jvm.JVMInputFlow
import dev.brella.kornea.io.jvm.files.fromUri
import dev.brella.kornea.toolkit.common.closeAll
import java.net.URL
import kotlin.math.max

class URLDataSource(
    val url: URL,
    override val maximumInstanceCount: Int? = null,
    override val location: String? = url.toExternalForm()
) : LimitedInstanceDataSource.Typed<JVMInputFlow, URLDataSource>(withBareOpener(this::openBareInputFlow)) {
    companion object {
        public suspend fun openBareInputFlow(self: URLDataSource, location: String?): JVMInputFlow =
            withContext(Dispatchers.IO) { JVMInputFlow(self.url.openStream(), location ?: self.location) }
    }

    override val dataSize: ULong by lazy { url.openConnection().contentLengthLong.toULong() }

    override val reproducibility: DataSourceReproducibility =
        DataSourceReproducibility(isUnreliable = true)

    override fun locationAsUri(): KorneaResult<Uri> = KorneaResult.success(Uri.fromUri(url.toURI()), null)

    override suspend fun whenClosed() {
        super.whenClosed()

        openInstances.closeAll()
        openInstances.clear()
    }
}