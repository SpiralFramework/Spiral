package org.abimon.osl

import org.abimon.spiral.core.objects.game.DRGame

data class ParserState(val silence: Boolean, val game: DRGame, val strictParsing: Boolean, val flags: Array<Map.Entry<String, Boolean>>, val data: Array<Map.Entry<String, Any>>, val labels: Array<Int>, val valueStackSnapshot: Any?)