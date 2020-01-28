@file:Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")

package info.spiralframework.base.common.concurrent

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Performs the given [action] on each element.
 */
public suspend inline fun IntArray.suspendForEach(action: suspend (Int) -> Unit) {
    for (element in this) action(element)
}


/**
 * Performs the given [action] on each element.
 */
public suspend inline fun <T> Iterable<T>.suspendForEach(action: suspend (T) -> Unit) {
    for (element in this) action(element)
}

/**
 * Performs the given [action] on each element **in parallel.**
 */
public suspend inline fun <T> Iterable<T>.parallelForEach(crossinline action: suspend (T) -> Unit) {
    coroutineScope {
        for (element in this@parallelForEach) launch { action(element) }
    }
}