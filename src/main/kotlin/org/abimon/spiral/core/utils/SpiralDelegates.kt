package org.abimon.spiral.core.utils

//private object UNINITIALIZED_VALUE
//
//class Late<T>(lock: Any? = null) : Lazy<T> {
//    @Volatile private var _value: Any? = UNINITIALIZED_VALUE
//    private val lock = lock ?: this
//
//    override var value: T
//        get() {
//            val _v1 = _value
//            if (_v1 !== UNINITIALIZED_VALUE) {
//                @Suppress("UNCHECKED_CAST")
//                return _v1 as T
//            }
//
//            return synchronized(lock) {
//                val _v2 = _value
//                if (_v2 !== UNINITIALIZED_VALUE) {
//                    @Suppress("UNCHECKED_CAST") (_v2 as T)
//                } else {
//                    val typedValue = initializer!!()
//                    _value = typedValue
//                    initializer = null
//                    typedValue
//                }
//            }
//        }
//        set() {
//
//        }
//
//    override fun isInitialized(): Boolean = _value !== UNINITIALIZED_VALUE
//
//    override fun toString(): String = if (isInitialized()) value.toString() else "Late value not initialized yet."
//}