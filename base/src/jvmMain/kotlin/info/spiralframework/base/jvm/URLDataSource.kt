package info.spiralframework.base.jvm

import dev.brella.kornea.base.common.closeAll
import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.io.common.DataSourceReproducibility
import dev.brella.kornea.io.common.LimitedInstanceDataSource
import dev.brella.kornea.io.common.Uri
import dev.brella.kornea.io.jvm.JVMInputFlow
import dev.brella.kornea.io.jvm.files.fromUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

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