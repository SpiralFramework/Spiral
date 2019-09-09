@file:Suppress("NOTHING_TO_INLINE")

package info.spiralframework.base.common.io.flow

import info.spiralframework.base.common.io.DataCloseable

@ExperimentalUnsignedTypes
typealias InputFlowEventHandler = suspend (flow: InputFlow) -> Unit

@ExperimentalUnsignedTypes
interface InputFlow : DataCloseable {
    companion object {
        const val FROM_BEGINNING = 0
        const val FROM_END = 1
        const val FROM_POSITION = 2
    }

    var onClose: InputFlowEventHandler?

    suspend fun read(): Int?
    suspend fun read(b: ByteArray): Int? = read(b, 0, b.size)
    suspend fun read(b: ByteArray, off: Int, len: Int): Int?
    suspend fun skip(n: ULong): ULong?
    suspend fun seek(pos: Long, mode: Int): ULong? = throw IllegalStateException("This flow is not seekable")
    suspend fun position(): ULong

    suspend fun available(): ULong?
    suspend fun remaining(): ULong?
    suspend fun size(): ULong?

    override suspend fun close() {
        onClose?.invoke(this)
    }
}

@ExperimentalUnsignedTypes
suspend inline fun InputFlow.skip(number: Number): ULong? = skip(number.toLong().toULong())

@ExperimentalUnsignedTypes
fun InputFlow.setCloseHandler(handler: InputFlowEventHandler) {
    this.onClose = handler
}

fun readResultIsValid(byte: Int): Boolean = byte != -1