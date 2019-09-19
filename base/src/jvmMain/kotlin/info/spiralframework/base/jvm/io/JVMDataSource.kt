package info.spiralframework.base.jvm.io

import info.spiralframework.base.common.io.DataSource
import info.spiralframework.base.common.io.DataSourceReproducibility
import java.io.InputStream

@ExperimentalUnsignedTypes
class JVMDataSource(val func: () -> InputStream): DataSource<JVMInputFlow> {
    override val dataSize: ULong? = null
    /**
     * The reproducibility traits of this data source.
     *
     * These traits *may* change between invocations, so a fresh instance should be obtained each time
     */
    override val reproducibility: DataSourceReproducibility
        get() = DataSourceReproducibility(
                isUnreliable = true
        )

    override suspend fun openInputFlow(): JVMInputFlow = JVMInputFlow(func())

    override fun canOpenInputFlow(): Boolean = true

    override suspend fun close() {}
}