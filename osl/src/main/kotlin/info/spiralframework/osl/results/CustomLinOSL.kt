package info.spiralframework.osl.results

import info.spiralframework.formats.scripting.CustomLin
import info.spiralframework.formats.scripting.lin.LinScript
import info.spiralframework.osl.SpiralDrillBit
import kotlin.reflect.KClass

open class CustomLinOSL: OSLCompilation<CustomLin> {
    companion object {
        val LIN_SCRIPT = LinScript::class.java
        val ARRAY_LIN_SCRIPT = Array<LinScript>::class
    }
    val lin = CustomLin()

    override fun <T : Any> handle(drill: SpiralDrillBit, product: T, klass: KClass<out T>) {
        when (product) {
            is LinScript -> lin.add(product)
            is Array<*> -> if (klass == ARRAY_LIN_SCRIPT) lin.addAll(product.filterIsInstance(LIN_SCRIPT))
        }
    }

    override fun produce(): CustomLin = lin
}
