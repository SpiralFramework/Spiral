package info.spiralframework.base.common

public abstract class SpiralFunction<T>(public val name: String, public val parameterNames: Array<out String>) {
    public abstract suspend fun suspendInvoke(context: SpiralContext, parameters: Map<String, Any?>): T
//    abstract fun synchronousInvoke(parameters: Map<String, Any?>)
}

public object SpiralSynchronous {
    public abstract class Function<T>(name: String, parameterNames: Array<out String>): SpiralFunction<T>(name, parameterNames) {
        public abstract operator fun invoke(context: SpiralContext, parameters: Map<String, Any?>): T
        override suspend fun suspendInvoke(context: SpiralContext, parameters: Map<String, Any?>): T = invoke(context, parameters)
    }

    public class Func0<T>(name: String, public val func: (context: SpiralContext) -> T): Function<T>(name, emptyArray()) {
        override operator fun invoke(context: SpiralContext, parameters: Map<String, Any?>): T = func(context)
    }
    public class Func1<T>(name: String, private val paramName: String, public val func: (context: SpiralContext, Any?) -> T): Function<T>(name, arrayOf(paramName)) {
        override operator fun invoke(context: SpiralContext, parameters: Map<String, Any?>): T = func(context, parameters[paramName])
    }
    public class Func2<T>(name: String, vararg parameterNames: String, public val func: (context: SpiralContext, Any?, Any?) -> T): Function<T>(name, parameterNames) {
        override operator fun invoke(context: SpiralContext, parameters: Map<String, Any?>): T = func(context, parameters[this, 0], parameters[this, 1])
    }
    public class Func3<T>(name: String, vararg parameterNames: String, public val func: (context: SpiralContext, Any?, Any?, Any?) -> T): Function<T>(name, parameterNames) {
        override operator fun invoke(context: SpiralContext, parameters: Map<String, Any?>): T = func(context, parameters[this, 0], parameters[this, 1], parameters[this, 2])
    }

    public class FuncX<T>(name: String, vararg parameterNames: String, public val func: (context: SpiralContext, parameters: Map<String, Any?>) -> T): Function<T>(name, parameterNames) {
        override operator fun invoke(context: SpiralContext, parameters: Map<String, Any?>): T = func(context, parameters)
    }
}

public object SpiralSuspending {
    public abstract class Function<T>(name: String, parameterNames: Array<out String>): SpiralFunction<T>(name, parameterNames) {
        public suspend operator fun invoke(context: SpiralContext, parameters: Map<String, Any?>): T = suspendInvoke(context, parameters)
    }

    public class Func0<T>(name: String, public val func: suspend (context: SpiralContext) -> T): Function<T>(name, emptyArray()) {
        override suspend fun suspendInvoke(context: SpiralContext, parameters: Map<String, Any?>): T = func(context)
    }
    public class Func1<T>(name: String, private val paramName: String, public val func: suspend (context: SpiralContext, Any?) -> T): Function<T>(name, arrayOf(paramName)) {
        override suspend fun suspendInvoke(context: SpiralContext, parameters: Map<String, Any?>): T = func(context, parameters[paramName])
    }
    public class Func2<T>(name: String, vararg parameterNames: String, public val func: suspend (context: SpiralContext, Any?, Any?) -> T): Function<T>(name, parameterNames) {
        override suspend fun suspendInvoke(context: SpiralContext, parameters: Map<String, Any?>): T = func(context, parameters[this, 0], parameters[this, 1])
    }
    public class Func3<T>(name: String, vararg parameterNames: String, public val func: suspend (context: SpiralContext, Any?, Any?, Any?) -> T): Function<T>(name, parameterNames) {
        override suspend fun suspendInvoke(context: SpiralContext, parameters: Map<String, Any?>): T = func(context, parameters[this, 0], parameters[this, 1], parameters[this, 2])
    }
    public class Func4<T>(name: String, vararg parameterNames: String, public val func: suspend (context: SpiralContext, Any?, Any?, Any?, Any?) -> T): Function<T>(name, parameterNames) {
        override suspend fun suspendInvoke(context: SpiralContext, parameters: Map<String, Any?>): T = func(context, parameters[this, 0], parameters[this, 1], parameters[this, 2], parameters[this, 3])
    }
    public class Func5<T>(name: String, vararg parameterNames: String, public val func: suspend (context: SpiralContext, Any?, Any?, Any?, Any?, Any?) -> T): Function<T>(name, parameterNames) {
        override suspend fun suspendInvoke(context: SpiralContext, parameters: Map<String, Any?>): T = func(context, parameters[this, 0], parameters[this, 1], parameters[this, 2], parameters[this, 3], parameters[this, 4])
    }

    public class FuncX<T>(name: String, vararg parameterNames: String, public val variadicSupported: Boolean = false, public val func: suspend (context: SpiralContext, parameters: Map<String, Any?>) -> T): Function<T>(name, parameterNames) {
        override suspend fun suspendInvoke(context: SpiralContext, parameters: Map<String, Any?>): T = func(context, parameters)
    }
}

public operator fun Map<String, Any?>.get(func: SpiralFunction<*>, index: Int): Any? = this[func.parameterNames[index]]