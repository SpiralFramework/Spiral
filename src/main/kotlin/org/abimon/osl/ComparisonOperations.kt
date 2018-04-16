package org.abimon.osl

object ComparisonOperations {
    fun equal(parser: OpenSpiralLanguageParser, variable: String, value: String): Boolean {
        if(variable == "GAME")
            return value in parser.game.names

        return parser[variable].toString() == value
    }

    fun notEqual(parser: OpenSpiralLanguageParser, variable: String, value: String): Boolean = !equal(parser, variable, value)

    fun nop(parser: OpenSpiralLanguageParser, variable: String, value: String): Boolean = false
}