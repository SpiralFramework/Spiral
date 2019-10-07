package info.spiralframework.base.js.io

import info.spiralframework.base.common.io.DataSource
import info.spiralframework.base.common.io.DataSourceReproducibility
import info.spiralframework.base.common.io.flow.BinaryInputFlow

@ExperimentalUnsignedTypes
class AjaxDataSource: DataSource<BinaryInputFlow> {
    override val dataSize: ULong?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val reproducibility: DataSourceReproducibility
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override suspend fun openInputFlow(): BinaryInputFlow? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun canOpenInputFlow(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun close() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}