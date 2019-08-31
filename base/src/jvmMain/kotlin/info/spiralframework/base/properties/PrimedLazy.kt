package info.spiralframework.base.properties

import info.spiralframework.base.common.properties.*

/**
 * Creates a new instance of the [PrimedLazy] that uses the specified initialization function [initializer]
 * and the default thread-safety mode [PrimedLazyThreadSafetyMode.SYNCHRONIZED].
 *
 * If the initialization of a value throws an exception, it will attempt to reinitialize the value at next access.
 *
 * Note that the returned instance uses itself to synchronize on. Do not synchronize from external code on
 * the returned instance as it may cause accidental deadlock. Also this behavior can be changed in the future.
 */
//public actual fun <T, P> primedLazy(initializer: (P) -> T): PrimedLazy<T, P> = SynchronizedPrimedLazyImpl(initializer)

/**
 * Creates a new instance of the [PrimedLazy] that uses the specified initialization function [initializer]
 * and thread-safety [mode].
 *
 * If the initialization of a value throws an exception, it will attempt to reinitialize the value at next access.
 *
 * Note that when the [PrimedLazyThreadSafetyMode.SYNCHRONIZED] mode is specified the returned instance uses itself
 * to synchronize on. Do not synchronize from external code on the returned instance as it may cause accidental deadlock.
 * Also this behavior can be changed in the future.
 */
public fun <T, P> primedLazy(mode: PrimedLazyThreadSafetyMode, initializer: (P) -> T): PrimedLazy<T, P> =
        when (mode) {
            PrimedLazyThreadSafetyMode.SYNCHRONIZED -> SynchronizedPrimedLazyImpl(initializer)
            PrimedLazyThreadSafetyMode.PUBLICATION -> SafePublicationPrimedLazyImpl(initializer)
            PrimedLazyThreadSafetyMode.NONE -> UnsafePrimedLazyImpl(initializer)
        }

/**
 * Creates a new instance of the [PrimedLazy] that uses the specified initialization function [initializer]
 * and the default thread-safety mode [PrimedLazyThreadSafetyMode.SYNCHRONIZED].
 *
 * If the initialization of a value throws an exception, it will attempt to reinitialize the value at next access.
 *
 * The returned instance uses the specified [lock] object to synchronize on.
 * When the [lock] is not specified the instance uses itself to synchronize on,
 * in this case do not synchronize from external code on the returned instance as it may cause accidental deadlock.
 * Also this behavior can be changed in the future.
 */
public fun <T, P> primedLazy(lock: Any?, initializer: (P) -> T): PrimedLazy<T, P> = SynchronizedPrimedLazyImpl(initializer, lock)

internal class SynchronizedPrimedLazyImpl<out T, P>(initializer: (P) -> T, lock: Any? = null) : PrimedLazy<T, P> {
    private var initializer: ((P) -> T)? = initializer
    @Volatile
    private var _value: Any? = UNINITIALIZED_VALUE
    @Volatile
    private var _priming: Any? = UNINITIALIZED_VALUE
    // final field is required to enable safe publication of constructed instance
    private val lock = lock ?: this

    override val value: T
        get() {
            val _v1 = _value
            if (_v1 !== UNINITIALIZED_VALUE) {
                @Suppress("UNCHECKED_CAST")
                return _v1 as T
            }

            return synchronized(lock) {
                val _v2 = _value
                if (_v2 !== UNINITIALIZED_VALUE) {
                    @Suppress("UNCHECKED_CAST") (_v2 as T)
                } else {
                    if (_priming === UNINITIALIZED_VALUE) {
                        throw IllegalStateException("Lazy not primed")
                    }

                    @Suppress("UNCHECKED_CAST")
                    val typedValue = initializer!!(_priming as P)
                    _value = typedValue
                    initializer = null
                    typedValue
                }
            }
        }

    override fun isInitialized(): Boolean = _value !== UNINITIALIZED_VALUE

    override fun isPrimed(): Boolean = _priming !== UNINITIALIZED_VALUE

    override fun prime(payload: P) {
        if (!isPrimed()) {
            _priming = payload
        }
    }

    override fun toString(): String = if (isInitialized()) value.toString() else "PrimedLazy value not initialized yet."

    private fun writeReplace(): Any = InitializedPrimedLazyImpl<T, P>(value)
}


private class SafePublicationPrimedLazyImpl<out T, P>(initializer: (P) -> T) : PrimedLazy<T, P> {
    @Volatile
    private var initializer: ((P) -> T)? = initializer
    @Volatile
    private var _value: Any? = UNINITIALIZED_VALUE
    @Volatile
    private var _priming: Any? = UNINITIALIZED_VALUE
    // this final field is required to enable safe publication of constructed instance
    private val final: Any = UNINITIALIZED_VALUE

    override val value: T
        get() {
            val value = _value
            if (value !== UNINITIALIZED_VALUE) {
                @Suppress("UNCHECKED_CAST")
                return value as T
            }

            val initializerValue = initializer
            // if we see null in initializer here, it means that the value is already set by another thread
            if (initializerValue != null) {
                if (_priming === UNINITIALIZED_VALUE) {
                    throw IllegalStateException("Lazy not primed")
                }

                @Suppress("UNCHECKED_CAST")
                val newValue = initializerValue(_priming as P)
                if (valueUpdater.compareAndSet(this, UNINITIALIZED_VALUE, newValue)) {
                    initializer = null
                    return newValue
                }
            }
            @Suppress("UNCHECKED_CAST")
            return _value as T
        }

    override fun isInitialized(): Boolean = _value !== UNINITIALIZED_VALUE

    override fun isPrimed(): Boolean = _priming !== UNINITIALIZED_VALUE

    override fun prime(payload: P) {
        if (!isPrimed()) {
            _priming = payload
        }
    }

    override fun toString(): String = if (isInitialized()) value.toString() else "PrimedLazy value not initialized yet."

    private fun writeReplace(): Any = InitializedPrimedLazyImpl<T, P>(value)

    companion object {
        private val valueUpdater = java.util.concurrent.atomic.AtomicReferenceFieldUpdater.newUpdater(
                SafePublicationPrimedLazyImpl::class.java,
                Any::class.java,
                "_value"
        )
    }
}