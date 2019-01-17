package info.spiralframework.osl

object ComparisonOperations {
    fun equal(parser: info.spiralframework.osl.OpenSpiralLanguageParser, first: String, second: String): Boolean =
            first == second

    fun notEqual(parser: info.spiralframework.osl.OpenSpiralLanguageParser, variable: String, value: String): Boolean =
            !info.spiralframework.osl.ComparisonOperations.equal(parser, variable, value)

    fun nop(parser: info.spiralframework.osl.OpenSpiralLanguageParser, variable: String, value: String): Boolean = false

    fun and(first: Boolean, second: Boolean): Boolean = first && second
    fun or(first: Boolean, second: Boolean): Boolean = first || second
    fun nand(first: Boolean, second: Boolean): Boolean = !info.spiralframework.osl.ComparisonOperations.and(first, second)
    fun nor(first: Boolean, second: Boolean): Boolean = !info.spiralframework.osl.ComparisonOperations.or(first, second)
    fun xor(first: Boolean, second: Boolean): Boolean = first xor second
}
