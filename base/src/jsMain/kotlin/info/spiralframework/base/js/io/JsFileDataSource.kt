package info.spiralframework.base.js.io

import info.spiralframework.base.common.io.DataSource
import info.spiralframework.base.common.io.DataSourceReproducibility
import info.spiralframework.base.common.io.closeAll
import info.spiralframework.base.common.io.flow.BinaryInputFlow
import info.spiralframework.base.common.io.flow.InputFlow
import info.spiralframework.base.common.io.flow.setCloseHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.js.Promise
import kotlin.math.max

@ExperimentalUnsignedTypes
class JsFileDataSource private constructor(val file: File, val maxInstanceCount: Int = -1) : DataSource<BinaryInputFlow> {
    companion object {
        suspend operator fun invoke(file: File): JsFileDataSource {
            val ajax = JsFileDataSource(file)
            ajax.init()
            return ajax
        }

        fun async(file: File): Promise<JsFileDataSource> = GlobalScope.promise { invoke(file) }
    }

    private var data: ByteArray? = null
    override val dataSize: ULong?
        get() = data?.size?.toULong()
    override val reproducibility: DataSourceReproducibility = DataSourceReproducibility(isStatic = true, isRandomAccess = true)

    private val openInstances: MutableList<BinaryInputFlow> = ArrayList(max(maxInstanceCount, 0))
    private var closed: Boolean = false

    override suspend fun openInputFlow(): BinaryInputFlow? {
        if (canOpenInputFlow()) {
            val stream = BinaryInputFlow(data!!)
            stream.setCloseHandler(this::instanceClosed)
            openInstances.add(stream)
            return stream
        } else {
            return null
        }
    }
    override fun canOpenInputFlow(): Boolean = !closed && data != null && (maxInstanceCount == -1 || openInstances.size < maxInstanceCount)

    private suspend fun instanceClosed(flow: InputFlow) {
        openInstances.remove(flow)
    }

    override suspend fun close() {
        if (!closed) {
            closed = true
            openInstances.toTypedArray().closeAll()
            openInstances.clear()
        }
    }

    private suspend fun init() {
        val buffer = Promise { resolve: (ArrayBuffer?) -> Unit, _: (Throwable) -> Unit ->
            val reader = FileReader()
            reader.onloadend = { _ -> resolve(reader.result as? ArrayBuffer) }
            reader.readAsArrayBuffer(file)
        }.await()

        if (buffer == null) {
            close()
        } else {
            data = Int8Array(buffer).asDynamic() as ByteArray
        }
    }
}