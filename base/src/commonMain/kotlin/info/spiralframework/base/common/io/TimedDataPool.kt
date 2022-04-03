package info.spiralframework.base.common.io

import dev.brella.kornea.io.common.DataPool
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.OutputFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
public class TimedDataPool<I: InputFlow, O: OutputFlow>(private val backing: DataPool<I, O>, closeAfter: Duration, scope: CoroutineScope, context: CoroutineContext = EmptyCoroutineContext): DataPool<I, O> by backing {
    private var closingJob: Job = scope.launch(context) {
        delay(closeAfter.inWholeMilliseconds)
        close()
    }

    override suspend fun close() {
        if (!isClosed) {
            backing.close()
            closingJob.cancel()
        }
    }

    public fun extend(closeAfter: Duration, scope: CoroutineScope, context: CoroutineContext) {
        closingJob.cancel()
        closingJob = scope.launch(context) {
            delay(closeAfter.inWholeMilliseconds)
            close()
        }
    }
}