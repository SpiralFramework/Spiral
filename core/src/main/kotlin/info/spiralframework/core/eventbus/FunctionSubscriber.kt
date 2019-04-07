package info.spiralframework.core.eventbus

import org.greenrobot.eventbus.Subscribe
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.reflect.full.isSuperclassOf

class FunctionSubscriber<T: Any>(val klass: KClass<T>, val function: (T) -> Unit) {
    companion object {
        inline operator fun <reified T: Any> invoke(noinline function: (T) -> Unit): FunctionSubscriber<T> = FunctionSubscriber(T::class, function)
    }
    @Subscribe
    operator fun invoke(param: Any) {
        if (klass.isSuperclassOf(param::class)) {
            function(klass.cast(param))
        }
    }
}