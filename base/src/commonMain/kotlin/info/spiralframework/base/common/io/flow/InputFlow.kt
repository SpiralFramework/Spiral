@file:Suppress("NOTHING_TO_INLINE")

package info.spiralframework.base.common.io.flow

import info.spiralframework.base.binding.BinaryOutputFlow
import info.spiralframework.base.common.io.DataCloseable
import info.spiralframework.base.common.io.DataSource
import info.spiralframework.base.common.io.copyTo
import info.spiralframework.base.common.io.use
import info.spiralframework.base.common.takeIf

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
suspend fun InputFlow.readBytes(bufferSize: Int = 8192): ByteArray {
    val buffer = BinaryOutputFlow()
    copyTo(buffer, bufferSize)
    return buffer.getData()
}

@ExperimentalUnsignedTypes
suspend fun InputFlow.readExact(buffer: ByteArray): ByteArray? = readExact(buffer, 0, buffer.size)

@ExperimentalUnsignedTypes
suspend fun InputFlow.readExact(buffer: ByteArray, offset: Int, length: Int): ByteArray? {
    var currentOffset: Int = offset
    var remainingLength: Int = length

    var read: Int

    while (remainingLength > 0 && (currentOffset + remainingLength) <= buffer.size) {
        read = read(buffer, currentOffset, remainingLength) ?: break
        currentOffset += read
        remainingLength -= read
    }

    return buffer.takeIf(remainingLength == 0)
}

@ExperimentalUnsignedTypes
suspend fun InputFlow.readAndClose(bufferSize: Int = 8192): ByteArray {
    use(this) {
        val buffer = BinaryOutputFlow()
        copyTo(buffer, bufferSize)
        return buffer.getData()
    }
}

@ExperimentalUnsignedTypes
fun InputFlow.setCloseHandler(handler: InputFlowEventHandler) {
    this.onClose = handler
}

@ExperimentalUnsignedTypes
suspend inline fun <T> InputFlow.fauxSeekFromStart(offset: ULong, dataSource: DataSource<*>, noinline block: suspend (InputFlow) -> T): T? {
    val bookmark = position()
    return if (seek(offset.toLong(), InputFlow.FROM_BEGINNING) == null) {
        val flow = dataSource.openInputFlow() ?: return null
        use(flow) {
            flow.skip(offset)
            block(flow)
        }
    } else {
        val result = block(this)
        seek(bookmark.toLong(), InputFlow.FROM_BEGINNING)
        result
    }
}

fun readResultIsValid(byte: Int): Boolean = byte != -1

@ExperimentalUnsignedTypes
public suspend inline fun <T : InputFlow, R> bookmark(t: T, block: () -> R): R {
//    contract {
//        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
//    }

    val position = t.position()
    try {
        return block()
    } finally {
        t.seek(position.toLong(), InputFlow.FROM_BEGINNING)
    }
}

@ExperimentalUnsignedTypes
suspend fun InputFlow.globalOffset(): ULong = if (this is OffsetInputFlow) offset + backing.globalOffset() else if (this is WindowedInputFlow) offset + window.globalOffset() else 0u
@ExperimentalUnsignedTypes
suspend fun InputFlow.offsetPosition(): ULong = globalOffset() + position()