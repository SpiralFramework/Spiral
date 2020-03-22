package info.spiralframework.base.common.io

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.abimon.kornea.io.common.addCloseHandler
import org.abimon.kornea.io.common.flow.OutputFlow
import java.io.BufferedOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.zip.ZipOutputStream

class FlowOutputStream private constructor(val flow: OutputFlow, val closeFlow: Boolean) : OutputStream() {
    companion object {
        operator fun invoke(scope: CoroutineScope, flow: OutputFlow, closeFlow: Boolean): FlowOutputStream {
            val stream = FlowOutputStream(flow, closeFlow)
            stream.init(scope)
            return stream
        }
    }

    val channel = Channel<ByteArray>(Channel.UNLIMITED)
    lateinit var job: Job

    override fun write(b: Int) {
        channel.offer(byteArrayOf(b.toByte()))
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        channel.offer(b.copyOfRange(off, off + len))
    }

    override fun close() {
        super.close()
        channel.close()
        if (closeFlow)
            runBlocking { flow.close() }
    }

    fun init(scope: CoroutineScope) {
        job = scope.launch {
            while (isActive && !channel.isClosedForReceive) {
                flow.write(channel.receive())
                delay(50)
            }
        }
    }
}

fun CoroutineScope.FlowOutputStream(flow: OutputFlow, closeFlow: Boolean) = FlowOutputStream(this, flow, closeFlow)
suspend inline fun <T> CoroutineScope.asOutputStream(flow: OutputFlow, closeFlow: Boolean, block: (OutputStream) -> T): T {
    val stream = FlowOutputStream(this, flow, closeFlow)
    val output = BufferedOutputStream(stream).use(block)
    stream.job.join()
    return output
}