package info.spiralframework.core

import java.io.InputStream

typealias DataSource = () -> InputStream
typealias DataContext = (String) -> DataSource?

data class FormatChance(val isFormat: Boolean, val confidence: Double) {
    companion object {
        val ACCEPTABLE_RANGE = -0.0000001 .. 1.0000001
    }

    init {
        check(confidence in ACCEPTABLE_RANGE)
    }
}

val BLANK_DATA_CONTEXT: DataContext = { null }