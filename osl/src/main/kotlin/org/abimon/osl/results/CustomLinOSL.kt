package org.abimon.osl.results

import org.abimon.osl.SpiralDrillBit
import org.abimon.spiral.core.objects.scripting.CustomLin
import org.abimon.spiral.core.objects.scripting.lin.LinScript
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