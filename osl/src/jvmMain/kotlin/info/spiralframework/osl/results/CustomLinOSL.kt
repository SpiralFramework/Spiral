package info.spiralframework.osl.results

import info.spiralframework.formats.scripting.CustomLin
import info.spiralframework.formats.common.scripting.lin.LinEntry
import info.spiralframework.osl.SpiralDrillBit
import kotlin.reflect.KClass

open class CustomLinOSL: OSLCompilation<CustomLin> {
    companion object {
        val LIN_SCRIPT = LinEntry::class.java
        val ARRAY_LIN_SCRIPT = Array<LinEntry>::class
    }
    val lin = CustomLin()

    override fun <T : Any> handle(drill: SpiralDrillBit, product: T, klass: KClass<out T>) {
        when (product) {
            is LinEntry -> lin.add(product)
            is Array<*> -> if (klass == ARRAY_LIN_SCRIPT) lin.addAll(product.filterIsInstance(LIN_SCRIPT))
        }
    }

    override fun produce(): CustomLin = lin
}
