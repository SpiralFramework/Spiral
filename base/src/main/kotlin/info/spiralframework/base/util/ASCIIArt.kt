package info.spiralframework.base.util

import kotlinx.coroutines.*

fun <T> arbitraryProgressBar(
        delay: Long = 100, limit: Int = 9,
        start: Char = '[', end: Char = ']',
        space: Char = ' ', indicator: Char = 'o',
        loadingText: String = "LOADING",
        loadedText: String = "Loaded!",
        operation: () -> T
): T {
    val arbitrary = arbitraryProgressBar(delay, limit, start, end, space, indicator, loadingText, loadedText)
    try {
        return operation()
    } finally {
        arbitrary.cancel()
    }
}

fun arbitraryProgressBar(
        delay: Long = 100, limit: Int = 9,
        start: Char = '[', end: Char = ']',
        space: Char = ' ', indicator: Char = 'o',
        loadingText: String = "LOADING",
        loadedText: String = "Loaded!"
): Job = GlobalScope.launch {
    try {
        while (isActive) {
            var progress: Int = 0
            var goingRight: Boolean = true
            while (true) {
                print(buildString {
                    append('\r')
                    append(start)
                    for (i in 0 until progress)
                        append(space)
                    append(indicator)
                    for (i in 0 until (limit - progress))
                        append(space)
                    append(end)
                    append(' ')
                    append(loadingText)
                })

                if (goingRight)
                    progress++
                else
                    progress--

                if (progress == limit || progress == 0)
                    goingRight = !goingRight

                delay(delay)
            }
        }
    } catch (e: CancellationException) {
        print(buildString {
            append('\r')
            for (i in 0 until limit)
                append(' ')
            append("    ")
            for (i in 0 until loadingText.length)
                append(' ')
            append('\r')
        })
        println(loadedText)
    }
}