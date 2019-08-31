package info.spiralframework.base.common.text

import info.spiralframework.base.common.SpiralContext
import kotlinx.coroutines.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

suspend fun <T> SpiralContext.arbitraryProgressBar(
        delay: Long = 100, limit: Int = 9,
        start: Char = '[', end: Char = ']',
        space: Char = ' ', indicator: Char = 'o',
        loadingText: String = "ascii.arbitrary.loading",
        loadedText: String = "ascii.arbitrary.loaded!",
        operation: () -> T
): T {
    val arbitrary = arbitraryProgressBar(delay, limit, start, end, space, indicator, loadingText, loadedText)
    try {
        return operation()
    } finally {
        arbitrary.cancelAndJoin()
    }
}

suspend fun <T> SpiralContext.arbitrarySuspendedProgressBar(
        delay: Long = 100, limit: Int = 9,
        start: Char = '[', end: Char = ']',
        space: Char = ' ', indicator: Char = 'o',
        loadingText: String = "ascii.arbitrary.loading",
        loadedText: String = "ascii.arbitrary.loaded!",
        operation: suspend () -> T
): T {
    val arbitrary = arbitraryProgressBar(delay, limit, start, end, space, indicator, loadingText, loadedText)
    try {
        return operation()
    } finally {
        arbitrary.cancelAndJoin()
    }
}

fun SpiralContext.arbitraryProgressBar(
        delay: Long = 100, limit: Int = 9,
        start: Char = '[', end: Char = ']',
        space: Char = ' ', indicator: Char = 'o',
        loadingText: String = "ascii.arbitrary.loading",
        loadedText: String = "ascii.arbitrary.loaded"
): Job = GlobalScope.launch {
    val localisedLoading = localise(loadingText).takeIf(String::isNotBlank)
    val localisedLoaded = localise(loadedText).takeIf(String::isNotBlank)

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
                    localisedLoading?.let(this::append)
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
    } finally {
        print(buildString {
            append('\r')
            for (i in 0 until limit)
                append(' ')
            append("    ")
            for (i in 0 until (localisedLoading?.length ?: 0))
                append(' ')
            append('\r')
        })

        localisedLoaded?.let(::println)
    }
}



open class ProgressTracker protected constructor(
        context: SpiralContext,
        val trackLength: Int = 10,
        val start: Char = '[', val end: Char = ']',
        val trackSpace: Char = ' ', val trackFilled: Char = '#',
        downloadingText: String = "ascii.progress.loading",
        downloadedText: String = "ascii.progress.loaded",
        val showPercentage: Boolean = true
) {
    companion object {
        val SILENT_TRACKER: ProgressTracker = object: ProgressTracker(SpiralContext.NoOp) {
            override fun finishedDownload() {}
            override fun trackDownload(downloaded: Long, total: Long) {}
        }

        operator fun invoke(context: SpiralContext,
                            trackLength: Int = 10,
                            start: Char = '[', end: Char = ']',
                            trackSpace: Char = ' ', trackFilled: Char = '#',
                            downloadingText: String = "ascii.progress.loading",
                            downloadedText: String = "ascii.progress.loaded",
                            showPercentage: Boolean = true): ProgressTracker {
            return ProgressTracker(context, trackLength, start, end, trackSpace, trackFilled, downloadingText, downloadedText, showPercentage)
        }
    }

    val downloadingText: String? = context.localise(downloadingText).takeIf(String::isNotBlank)
    val downloadedText: String? = context.localise(downloadedText).takeIf(String::isNotBlank)
    val percentPerTrackSpace = ceil(100.0 / trackLength.toDouble())
    val tracks = (0 until trackLength).map { filled ->
        buildString {
            append(start)

            for (i in 0 until filled)
                append(trackFilled)
            for (i in 0 until (trackLength - filled))
                append(trackSpace)

            append(end)
            append(' ')
            append(this@ProgressTracker.downloadingText)
        }
    }.toTypedArray()
    val blankTrack = buildString {
        append('\r')
        for (i in 0 until (trackLength + 12 + downloadingText.length))
            append(' ')
    }

    open fun trackDownload(downloaded: Long, total: Long) {
        val percent = (downloaded * 100.0) / total.toDouble()
        val filled = min(tracks.size - 1, floor(percent / percentPerTrackSpace).roundToInt())
        print(buildString {
            append('\r')
            if (showPercentage) {
                append(formatPercent(percent))
                append("% ")
            }
            append(tracks[filled])
        })
    }

    open fun finishedDownload() {
        print(buildString {
            append('\r')
            append(blankTrack)
            append('\r')
        })
        downloadedText?.let(::println)
    }
}

fun <T> SpiralContext.ProgressTracker(
        trackLength: Int = 20,
        start: Char = '[', end: Char = ']',
        trackSpace: Char = ' ', trackFilled: Char = '#',
        downloadingText: String = "ascii.progress.loading",
        downloadedText: String = "ascii.progress.loaded",
        showPercentage: Boolean = true,
        op: ProgressTracker.() -> T
): T {
    val tracker = ProgressTracker(this, trackLength, start, end, trackSpace, trackFilled, downloadingText, downloadedText, showPercentage)
    try {
        return tracker.op()
    } finally {
        tracker.finishedDownload()
    }
}