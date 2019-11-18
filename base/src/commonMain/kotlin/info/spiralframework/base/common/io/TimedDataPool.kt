package info.spiralframework.base.common.io

import info.spiralframework.base.common.io.flow.InputFlow
import info.spiralframework.base.common.io.flow.OutputFlow
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalUnsignedTypes
class TimedDataPool<I: InputFlow, O: OutputFlow>(val backing: DataPool<I, O>, closeAfter: Duration, scope: CoroutineScope = GlobalScope, context: CoroutineContext = EmptyCoroutineContext): DataPool<I, O> by backing {
    private var closingJob: Job = scope.launch(context) {
        delay(closeAfter.toLongMilliseconds())
        close()
    }

    override suspend fun close() {
        super.close()

        closingJob.cancel()
    }

    fun extend(closeAfter: Duration, scope: CoroutineScope = GlobalScope, context: CoroutineContext) {
        closingJob.cancel()
        closingJob = scope.launch(context) {
            delay(closeAfter.toLongMilliseconds())
            close()
        }
    }
}