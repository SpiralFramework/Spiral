package info.spiralframework.base.util

/**
 * A class used for debugging. Measure the time between several points and determine the slowdown
 */
class Stopwatch {
    var time = System.nanoTime()

    val laps: MutableList<Pair<String, Long>> = ArrayList()

    inline fun lap(name: String? = null): Pair<String, Long> {
        val old = time
        time = System.nanoTime()
        val lap = (name ?: Thread.currentThread().stackTrace[1].toString()) to (time - old)
        laps.add(lap)
        return lap
    }

    inline fun printLap(name: String? = null) {
        val (loc, time) = lap(name)
        println("$loc - $time ns")
    }
}