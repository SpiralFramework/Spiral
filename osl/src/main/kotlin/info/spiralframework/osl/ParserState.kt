package info.spiralframework.osl

data class ParserState(val silence: Boolean, val game: info.spiralframework.osl.GameContext?, val strictParsing: Boolean, val flags: Array<Pair<String, Boolean>>, val data: Array<Pair<String, Any>>, val labels: Array<Int>, val valueStackSnapshot: Any?)
