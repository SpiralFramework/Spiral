package info.spiralframework.base.common

abstract class SpiralFunction<T>(val name: String, val parameterNames: Array<out String>) {
    abstract suspend fun suspendInvoke(context: SpiralContext, parameters: Map<String, Any?>): T
//    abstract fun synchronousInvoke(parameters: Map<String, Any?>)
}

object SpiralSynchronous {
    abstract class Function<T>(name: String, parameterNames: Array<out String>): SpiralFunction<T>(name, parameterNames) {
        abstract operator fun invoke(context: SpiralContext, parameters: Map<String, Any?>): T
        override suspend fun suspendInvoke(context: SpiralContext, parameters: Map<String, Any?>) = invoke(context, parameters)
    }

    class Func0<T>(name: String, val func: (context: SpiralContext) -> T): Function<T>(name, emptyArray()) {
        override operator fun invoke(context: SpiralContext, parameters: Map<String, Any?>) = func(context)
    }
    class Func1<T>(name: String, private val paramName: String, val func: (context: SpiralContext, Any?) -> T): Function<T>(name, arrayOf(paramName)) {
        override operator fun invoke(context: SpiralContext, parameters: Map<String, Any?>) = func(context, parameters[paramName])
    }
    class Func2<T>(name: String, vararg parameterNames: String, val func: (context: SpiralContext, Any?, Any?) -> T): Function<T>(name, parameterNames) {
        override operator fun invoke(context: SpiralContext, parameters: Map<String, Any?>) = func(context, parameters[this, 0], parameters[this, 1])
    }
    class Func3<T>(name: String, vararg parameterNames: String, val func: (context: SpiralContext, Any?, Any?, Any?) -> T): Function<T>(name, parameterNames) {
        override operator fun invoke(context: SpiralContext, parameters: Map<String, Any?>) = func(context, parameters[this, 0], parameters[this, 1], parameters[this, 2])
    }

    class FuncX<T>(name: String, vararg parameterNames: String, val func: (context: SpiralContext, parameters: Map<String, Any?>) -> T): Function<T>(name, parameterNames) {
        override operator fun invoke(context: SpiralContext, parameters: Map<String, Any?>) = func(context, parameters)
    }
}

object SpiralSuspending {
    abstract class Function<T>(name: String, parameterNames: Array<out String>): SpiralFunction<T>(name, parameterNames) {
        suspend operator fun invoke(context: SpiralContext, parameters: Map<String, Any?>) = suspendInvoke(context, parameters)
    }

    class Func0<T>(name: String, val func: suspend (context: SpiralContext) -> T): Function<T>(name, emptyArray()) {
        override suspend fun suspendInvoke(context: SpiralContext, parameters: Map<String, Any?>) = func(context)
    }
    class Func1<T>(name: String, private val paramName: String, val func: suspend (context: SpiralContext, Any?) -> T): Function<T>(name, arrayOf(paramName)) {
        override suspend fun suspendInvoke(context: SpiralContext, parameters: Map<String, Any?>) = func(context, parameters[paramName])
    }
    class Func2<T>(name: String, vararg parameterNames: String, val func: suspend (context: SpiralContext, Any?, Any?) -> T): Function<T>(name, parameterNames) {
        override suspend fun suspendInvoke(context: SpiralContext, parameters: Map<String, Any?>) = func(context, parameters[this, 0], parameters[this, 1])
    }
    class Func3<T>(name: String, vararg parameterNames: String, val func: suspend (context: SpiralContext, Any?, Any?, Any?) -> T): Function<T>(name, parameterNames) {
        override suspend fun suspendInvoke(context: SpiralContext, parameters: Map<String, Any?>) = func(context, parameters[this, 0], parameters[this, 1], parameters[this, 2])
    }
    class Func4<T>(name: String, vararg parameterNames: String, val func: suspend (context: SpiralContext, Any?, Any?, Any?, Any?) -> T): Function<T>(name, parameterNames) {
        override suspend fun suspendInvoke(context: SpiralContext, parameters: Map<String, Any?>) = func(context, parameters[this, 0], parameters[this, 1], parameters[this, 2], parameters[this, 3])
    }
    class Func5<T>(name: String, vararg parameterNames: String, val func: suspend (context: SpiralContext, Any?, Any?, Any?, Any?, Any?) -> T): Function<T>(name, parameterNames) {
        override suspend fun suspendInvoke(context: SpiralContext, parameters: Map<String, Any?>) = func(context, parameters[this, 0], parameters[this, 1], parameters[this, 2], parameters[this, 3], parameters[this, 4])
    }

    class FuncX<T>(name: String, vararg parameterNames: String, val variadicSupported: Boolean = false, val func: suspend (context: SpiralContext, parameters: Map<String, Any?>) -> T): Function<T>(name, parameterNames) {
        override suspend fun suspendInvoke(context: SpiralContext, parameters: Map<String, Any?>) = func(context, parameters)
    }
}

operator fun Map<String, Any?>.get(func: SpiralFunction<*>, index: Int) = this[func.parameterNames[index]]