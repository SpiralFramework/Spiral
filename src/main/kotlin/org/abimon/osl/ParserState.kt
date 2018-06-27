package org.abimon.osl

data class ParserState(val silence: Boolean, val game: GameContext?, val strictParsing: Boolean, val flags: Array<Map.Entry<String, Boolean>>, val data: Array<Map.Entry<String, Any>>, val labels: Array<Int>, val valueStackSnapshot: Any?)