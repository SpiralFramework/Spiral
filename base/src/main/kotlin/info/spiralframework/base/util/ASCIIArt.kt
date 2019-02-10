package info.spiralframework.base.util

import info.spiralframework.base.SpiralLocale
import kotlinx.coroutines.*
import java.text.DecimalFormat
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

fun <T> arbitraryProgressBar(
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
        runBlocking { arbitrary.cancelAndJoin() } //This feels messy; is there a better way?
    }
}

fun arbitraryProgressBar(
        delay: Long = 100, limit: Int = 9,
        start: Char = '[', end: Char = ']',
        space: Char = ' ', indicator: Char = 'o',
        loadingText: String = "ascii.arbitrary.loading",
        loadedText: String = "ascii.arbitrary.loaded"
): Job = GlobalScope.launch {
    val localisedLoading = loadingText.takeIf(String::isNotBlank)?.let(SpiralLocale::localiseString)
    val localisedLoaded = loadedText.takeIf(String::isNotBlank)?.let(SpiralLocale::localiseString)

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

val PERCENT_FORMAT = DecimalFormat("00.00")

class ProgressTracker(
        val trackLength: Int = 10,
        val start: Char = '[', val end: Char = ']',
        val trackSpace: Char = ' ', val trackFilled: Char = '#',
        downloadingText: String = "ascii.progress.loading",
        downloadedText: String = "ascii.progress.loaded",
        val showPercentage: Boolean = true
) {
    val downloadingText: String? = downloadingText.takeIf(String::isNotBlank)?.let(SpiralLocale::localiseString)
    val downloadedText: String? = downloadedText.takeIf(String::isNotBlank)?.let(SpiralLocale::localiseString)
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
        for (i in 0 until (trackLength + 10 + downloadingText.length))
            append(' ')
    }

    fun trackDownload(downloaded: Long, total: Long) {
        val percent = (downloaded * 100.0) / total.toDouble()
        val filled = floor(percent / percentPerTrackSpace).roundToInt()
        print(buildString {
            append('\r')
            if (showPercentage) {
                append(PERCENT_FORMAT.format(percent))
                append("% ")
            }
            append(tracks[filled])
        })
    }
}

fun <T> ProgressTracker(
        trackLength: Int = 20,
        start: Char = '[', end: Char = ']',
        trackSpace: Char = ' ', trackFilled: Char = '#',
        downloadingText: String = "ascii.progress.loading",
        downloadedText: String = "ascii.progress.loaded",
        showPercentage: Boolean = true,
        op: ProgressTracker.() -> T
): T {
    val tracker = ProgressTracker(trackLength, start, end, trackSpace, trackFilled, downloadingText, downloadedText, showPercentage)
    try {
        return tracker.op()
    } finally {
        print(buildString {
            append('\r')
            append(tracker.blankTrack)
            append('\r')
        })
        tracker.downloadedText?.let(::println)
    }
}