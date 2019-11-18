package info.spiralframework.base.common.io

@ExperimentalUnsignedTypes
typealias DataCloseableEventHandler = suspend (closeable: DataCloseable) -> Unit

@ExperimentalUnsignedTypes
interface DataCloseable {
    val closeHandlers: MutableList<DataCloseableEventHandler>
    val isClosed: Boolean
    suspend fun close() {
        if (!isClosed) {
            closeHandlers.forEach { closed -> closed(this) }
        }
    }
}

@ExperimentalUnsignedTypes
fun DataCloseable.addCloseHandler(handler: DataCloseableEventHandler) {
    closeHandlers.add(handler)
}

@ExperimentalUnsignedTypes
public suspend inline fun <T : DataCloseable?, R> T.use(noinline block: suspend (T) -> R): R {
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        this.closeFinally(exception)
    }
}

@ExperimentalUnsignedTypes
public suspend inline fun <T : DataCloseable?, R> T.useBlock(block: (T) -> R): R {
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        this.closeFinally(exception)
    }
}

//@ExperimentalContracts
@ExperimentalUnsignedTypes
public suspend inline fun <T : DataCloseable?, R> use(t: T, block: () -> R): R {
//    contract {
//        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
//    }

    var exception: Throwable? = null
    try {
        return block()
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        t.closeFinally(exception)
    }
}

@ExperimentalUnsignedTypes
@PublishedApi
internal suspend fun DataCloseable?.closeFinally(cause: Throwable?) = when {
    this == null -> {
    }
    cause == null -> close()
    else ->
        try {
            close()
        } catch (closeException: Throwable) {
            //cause.addSuppressed(closeException)
        }
}

suspend fun <T: DataCloseable> Array<T>.closeAll() = forEach { data -> data.close() }