package org.abimon.osl

import org.abimon.spiral.core.objects.game.DRGame

data class ParserState(val silence: Boolean, val game: DRGame, val strictParsing: Boolean, val customIdentifiers: Array<Map.Entry<String, Int>>, val customFlagNames: Array<Map.Entry<String, Int>>, val customLabelNames: Array<Map.Entry<String, Int>>, val flags: Array<Map.Entry<String, Boolean>>, val data: Array<Map.Entry<String, Any>>, val valueStackSnapshot: Any?)