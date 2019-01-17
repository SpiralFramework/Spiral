package org.abimon.osl

object ComparisonOperations {
    fun equal(parser: OpenSpiralLanguageParser, first: String, second: String): Boolean =
            first == second

    fun notEqual(parser: OpenSpiralLanguageParser, variable: String, value: String): Boolean =
            !equal(parser, variable, value)

    fun nop(parser: OpenSpiralLanguageParser, variable: String, value: String): Boolean = false

    fun and(first: Boolean, second: Boolean): Boolean = first && second
    fun or(first: Boolean, second: Boolean): Boolean = first || second
    fun nand(first: Boolean, second: Boolean): Boolean = !and(first, second)
    fun nor(first: Boolean, second: Boolean): Boolean = !or(first, second)
    fun xor(first: Boolean, second: Boolean): Boolean = first xor second
}