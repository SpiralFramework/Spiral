package info.spiralframework.osl.results

import info.spiralframework.osl.SpiralDrillBit
import kotlin.reflect.KClass

interface OSLCompilation<R> {
    fun <T: Any> handle(drill: SpiralDrillBit, product: T, klass: KClass<out T>)
    fun produce(): R
}
