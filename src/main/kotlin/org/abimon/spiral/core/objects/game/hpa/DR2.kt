package org.abimon.spiral.core.objects.game.hpa

import org.abimon.spiral.core.utils.DataMapper

object DR2 : HopesPeakDRGame {
    override val pakNames: Map<String, Array<String>> =
            DataMapper.readMapFromStream(DR1::class.java.classLoader.getResourceAsStream("pak/dr2.json"))?.mapValues { (_, value) ->
                ((value as? List<*>)?.asIterable() ?: (value as? Array<*>)?.asIterable())?.mapNotNull { str -> str as? String }?.toTypedArray() ?: emptyArray()
            } ?: emptyMap()
}