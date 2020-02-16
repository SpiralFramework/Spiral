package info.spiralframework.osb.common

abstract class SpiralFunction(val name: String, val parameterNames: Array<out String>) {
    abstract suspend fun suspendInvoke(parameters: Map<String, Any?>)
//    abstract fun synchronousInvoke(parameters: Map<String, Any?>)
}

object SpiralSynchronous {
    abstract class Function(name: String, parameterNames: Array<out String>): SpiralFunction(name, parameterNames) {
        abstract operator fun invoke(parameters: Map<String, Any?>)
        override suspend fun suspendInvoke(parameters: Map<String, Any?>) = invoke(parameters)
    }

    class Func0(name: String, val func: () -> Unit): Function(name, emptyArray()) {
        override operator fun invoke(parameters: Map<String, Any?>) = func()
    }
    class Func1(name: String, private val paramName: String, val func: (Any?) -> Unit): Function(name, arrayOf(paramName)) {
        override operator fun invoke(parameters: Map<String, Any?>) = func(parameters[paramName])
    }
    class Func2(name: String, vararg parameterNames: String, val func: (Any?, Any?) -> Unit): Function(name, parameterNames) {
        override operator fun invoke(parameters: Map<String, Any?>) = func(parameters[this, 0], parameters[this, 1])
    }
    class Func3(name: String, vararg parameterNames: String, val func: (Any?, Any?, Any?) -> Unit): Function(name, parameterNames) {
        override operator fun invoke(parameters: Map<String, Any?>) = func(parameters[this, 0], parameters[this, 1], parameters[this, 2])
    }

    class FuncX(name: String, vararg parameterNames: String, val func: (parameters: Map<String, Any?>) -> Unit): Function(name, parameterNames) {
        override operator fun invoke(parameters: Map<String, Any?>) = func(parameters)
    }
}

object SpiralSuspending {
    abstract class Function(name: String, parameterNames: Array<out String>): SpiralFunction(name, parameterNames) {
        suspend operator fun invoke(parameters: Map<String, Any?>) = suspendInvoke(parameters)
    }

    class Func0(name: String, val func: suspend () -> Unit): Function(name, emptyArray()) {
        override suspend fun suspendInvoke(parameters: Map<String, Any?>) = func()
    }
    class Func1(name: String, private val paramName: String, val func: suspend (Any?) -> Unit): Function(name, arrayOf(paramName)) {
        override suspend fun suspendInvoke(parameters: Map<String, Any?>) = func(parameters[paramName])
    }
    class Func2(name: String, vararg parameterNames: String, val func: suspend (Any?, Any?) -> Unit): Function(name, parameterNames) {
        override suspend fun suspendInvoke(parameters: Map<String, Any?>) = func(parameters[this, 0], parameters[this, 1])
    }
    class Func3(name: String, vararg parameterNames: String, val func: suspend (Any?, Any?, Any?) -> Unit): Function(name, parameterNames) {
        override suspend fun suspendInvoke(parameters: Map<String, Any?>) = func(parameters[this, 0], parameters[this, 1], parameters[this, 2])
    }
    class Func4(name: String, vararg parameterNames: String, val func: suspend (Any?, Any?, Any?, Any?) -> Unit): Function(name, parameterNames) {
        override suspend fun suspendInvoke(parameters: Map<String, Any?>) = func(parameters[this, 0], parameters[this, 1], parameters[this, 2], parameters[this, 3])
    }

    class FuncX(name: String, vararg parameterNames: String, val func: suspend (parameters: Map<String, Any?>) -> Unit): Function(name, parameterNames) {
        override suspend fun suspendInvoke(parameters: Map<String, Any?>) = func(parameters)
    }
}

operator fun Map<String, Any?>.get(func: SpiralFunction, index: Int) = this[func.parameterNames[index]]