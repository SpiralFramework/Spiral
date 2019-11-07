package info.spiralframework.osl.results

import info.spiralframework.formats.text.CustomSTX
import info.spiralframework.formats.text.STX
import info.spiralframework.formats.utils.removeEscapes
import info.spiralframework.osl.SpiralDrillBit
import info.spiralframework.osl.WordScriptString
import kotlin.reflect.KClass

class CustomSTXOSL: OSLCompilation<CustomSTX> {
    val stx = CustomSTX()
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
            is STX.Language -> stx.language = product
            is WordScriptString -> if (product.index == -1) unmapped.add(product.str.removeEscapes()) else stx[product.index] = product.str.removeEscapes()
        }
    }

    override fun produce(): CustomSTX {
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
