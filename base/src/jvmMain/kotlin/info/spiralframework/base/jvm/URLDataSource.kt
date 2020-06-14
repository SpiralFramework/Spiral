package info.spiralframework.base.jvm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.abimon.kornea.errors.common.KorneaResult
import org.abimon.kornea.io.common.*
import org.abimon.kornea.io.common.DataSource.Companion.korneaSourceClosed
import org.abimon.kornea.io.common.DataSource.Companion.korneaSourceUnknown
import org.abimon.kornea.io.common.DataSource.Companion.korneaTooManySourcesOpen
import org.abimon.kornea.io.jvm.JVMDataSource
import org.abimon.kornea.io.jvm.JVMInputFlow
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

    override suspend fun whenClosed() {
        super.whenClosed()

        openInstances.closeAll()
        openInstances.clear()
    }
}