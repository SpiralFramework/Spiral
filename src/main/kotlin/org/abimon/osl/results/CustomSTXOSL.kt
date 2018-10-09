package org.abimon.osl.results

import org.abimon.osl.SpiralDrillBit
import org.abimon.osl.WordScriptString
import org.abimon.spiral.core.objects.text.CustomSTXT
import org.abimon.spiral.core.objects.text.STXT
import org.abimon.spiral.core.utils.removeEscapes
import kotlin.reflect.KClass

class CustomSTXOSL: OSLCompilation<CustomSTXT> {
    val stx = CustomSTXT()
    val unmapped: MutableList<String> = ArrayList()

    override fun <T : Any> handle(drill: SpiralDrillBit, product: T, klass: KClass<out T>) {
        /**
         * STXT.Language::class -> language = products as STXT.Language
        WordScriptString::class -> {
        val str = products as WordScriptString

        if (str.index == -1)
        unmapped.add(str.str.removeEscapes())
        else
        this[str.index] = str.str.removeEscapes()
        }
         */

        when (product) {
            is STXT.Language -> stx.language = product
            is WordScriptString -> if (product.index == -1) unmapped.add(product.str.removeEscapes()) else stx[product.index] = product.str.removeEscapes()
        }
    }

    override fun produce(): CustomSTXT {
        val localKeys = stx.strings.keys.sorted().toMutableList()

        unmapped.forEach { str ->
            val index = localKeys.let { strs -> (0..localKeys.size + 1).first { i -> i !in strs } }
            localKeys.add(index)
            localKeys.sort()

            stx[index] = str
        }
        unmapped.clear()

        return stx
    }
}