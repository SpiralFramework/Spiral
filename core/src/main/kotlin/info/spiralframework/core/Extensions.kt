package info.spiralframework.core

import java.io.Closeable

/**
 * Executes the given [block] function on this resource and then closes it down correctly whether an exception
 * is thrown or not.
 *
 * @param block a function to process this [Closeable] resource.
 * @return the result of [block] function invoked on this resource.
 */
public inline fun <T : Closeable?, R> (() -> T).use(block: (T) -> R): R {
    var exception: Throwable? = null
    val stream = this()
    try {
        return block(stream)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        when {
            stream == null -> {}
            exception == null -> stream.close()
            else ->
                try {
                    stream.close()
                } catch (closeException: Throwable) {
                    exception.addSuppressed(closeException)
                }
        }
    }
}