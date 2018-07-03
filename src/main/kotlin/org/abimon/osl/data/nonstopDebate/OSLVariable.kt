package org.abimon.osl.data.nonstopDebate

import kotlin.reflect.KClass

data class OSLVariable<T : Any>(val key: String, val value: T, val klass: KClass<T>) {
    companion object {
        inline operator fun <reified T: Any> invoke(key: String, value: T): OSLVariable<T> = OSLVariable(key, value, T::class)
    }

    object KEYS {
        val NONSTOP_TIMELIMIT = "Nonstop-TimeLimit"

        val COMPILE_AS = "Compile-As"
    }
}