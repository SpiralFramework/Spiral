package info.spiralframework.osb.common

abstract class SpiralFunction<T>(val name: String, val parameterNames: Array<out String>) {
    abstract suspend fun suspendInvoke(parameters: Map<String, Any?>): T
//    abstract fun synchronousInvoke(parameters: Map<String, Any?>)
}

object SpiralSynchronous {
    abstract class Function<T>(name: String, parameterNames: Array<out String>): SpiralFunction<T>(name, parameterNames) {
        abstract operator fun invoke(parameters: Map<String, Any?>): T
        override suspend fun suspendInvoke(parameters: Map<String, Any?>) = invoke(parameters)
    }

    class Func0<T>(name: String, val func: () -> T): Function<T>(name, emptyArray()) {
        override operator fun invoke(parameters: Map<String, Any?>) = func()
    }
    class Func1<T>(name: String, private val paramName: String, val func: (Any?) -> T): Function<T>(name, arrayOf(paramName)) {
        override operator fun invoke(parameters: Map<String, Any?>) = func(parameters[paramName])
    }
    class Func2<T>(name: String, vararg parameterNames: String, val func: (Any?, Any?) -> T): Function<T>(name, parameterNames) {
        override operator fun invoke(parameters: Map<String, Any?>) = func(parameters[this, 0], parameters[this, 1])
    }
    class Func3<T>(name: String, vararg parameterNames: String, val func: (Any?, Any?, Any?) -> T): Function<T>(name, parameterNames) {
        override operator fun invoke(parameters: Map<String, Any?>) = func(parameters[this, 0], parameters[this, 1], parameters[this, 2])
    }

    class FuncX<T>(name: String, vararg parameterNames: String, val func: (parameters: Map<String, Any?>) -> T): Function<T>(name, parameterNames) {
        override operator fun invoke(parameters: Map<String, Any?>) = func(parameters)
    }
}

object SpiralSuspending {
    abstract class Function<T>(name: String, parameterNames: Array<out String>): SpiralFunction<T>(name, parameterNames) {
        suspend operator fun invoke(parameters: Map<String, Any?>) = suspendInvoke(parameters)
    }

    class Func0<T>(name: String, val func: suspend () -> T): Function<T>(name, emptyArray()) {
        override suspend fun suspendInvoke(parameters: Map<String, Any?>) = func()
    }
    class Func1<T>(name: String, private val paramName: String, val func: suspend (Any?) -> T): Function<T>(name, arrayOf(paramName)) {
        override suspend fun suspendInvoke(parameters: Map<String, Any?>) = func(parameters[paramName])
    }
    class Func2<T>(name: String, vararg parameterNames: String, val func: suspend (Any?, Any?) -> T): Function<T>(name, parameterNames) {
        override suspend fun suspendInvoke(parameters: Map<String, Any?>) = func(parameters[this, 0], parameters[this, 1])
    }
    class Func3<T>(name: String, vararg parameterNames: String, val func: suspend (Any?, Any?, Any?) -> T): Function<T>(name, parameterNames) {
        override suspend fun suspendInvoke(parameters: Map<String, Any?>) = func(parameters[this, 0], parameters[this, 1], parameters[this, 2])
    }
    class Func4<T>(name: String, vararg parameterNames: String, val func: suspend (Any?, Any?, Any?, Any?) -> T): Function<T>(name, parameterNames) {
        override suspend fun suspendInvoke(parameters: Map<String, Any?>) = func(parameters[this, 0], parameters[this, 1], parameters[this, 2], parameters[this, 3])
    }

    class FuncX<T>(name: String, vararg parameterNames: String, val func: suspend (parameters: Map<String, Any?>) -> T): Function<T>(name, parameterNames) {
        override suspend fun suspendInvoke(parameters: Map<String, Any?>) = func(parameters)
    }
}

operator fun Map<String, Any?>.get(func: SpiralFunction<*>, index: Int) = this[func.parameterNames[index]]