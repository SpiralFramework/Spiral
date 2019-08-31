package info.spiralframework.base.common.properties

import kotlin.reflect.KProperty

/**
 * Represents a value with primed lazy initialization.
 *
 * To create an instance of [PrimedLazy] use the [primedLazy] function.
 */
public interface PrimedLazy<out T, P> {
    /**
     * Gets the lazily initialized value of the current PrimedLazy instance.
     * Once the value was initialized it must not change during the rest of lifetime of this PrimedLazy instance.
     */
    public val value: T

    /**
     * Returns `true` if a value for this PrimedLazy instance has been already initialized, and `false` otherwise.
     * Once this function has returned `true` it stays `true` for the rest of lifetime of this PrimedLazy instance.
     */
    public fun isInitialized(): Boolean

    /**
     * Primes this property to be calculable.
     */
    public fun prime(payload: P)

    /**
     * Returns `true` if a value for this PrimedLazy instance has been already primed, and `false` otherwise.
     * Once this function has returned `true` it stays `true` for the rest of lifetime of this PrimedLazy instance.
     */
    public fun isPrimed(): Boolean

}

/**
 * Creates a new instance of the [PrimedLazy] that is already initialized with the specified [value].
 */
public fun <T> primedLazyOf(value: T): PrimedLazy<T, Any> = InitializedPrimedLazyImpl(value)

/**
 * An extension to delegate a read-only property of type [T] to an instance of [PrimedLazy].
 *
 * This extension allows to use instances of PrimedLazy for property delegation:
 * `val property: String by primedLazy { initializer }`
 */
public inline operator fun <T> PrimedLazy<T, *>.getValue(thisRef: Any?, property: KProperty<*>): T = value

/**
 * Specifies how a [PrimedLazy] instance synchronizes initialization among multiple threads.
 */
public enum class PrimedLazyThreadSafetyMode {

    /**
     * Locks are used to ensure that only a single thread can initialize the [PrimedLazy] instance.
     */
    SYNCHRONIZED,

    /**
     * Initializer function can be called several times on concurrent access to uninitialized [PrimedLazy] instance value,
     * but only the first returned value will be used as the value of [PrimedLazy] instance.
     */
    PUBLICATION,

    /**
     * No locks are used to synchronize an access to the [PrimedLazy] instance value; if the instance is accessed from multiple threads, its behavior is undefined.
     *
     * This mode should not be used unless the [PrimedLazy] instance is guaranteed never to be initialized from more than one thread.
     */
    NONE,
}


internal object UNINITIALIZED_VALUE

// internal to be called from primedLazy in JS
internal class UnsafePrimedLazyImpl<out T, P>(initializer: (P) -> T) : PrimedLazy<T, P> {
    private var initializer: ((P) -> T)? = initializer
    private var _value: Any? = UNINITIALIZED_VALUE
    private var _priming: Any? = UNINITIALIZED_VALUE

    override val value: T
        get() {
            if (_value === UNINITIALIZED_VALUE) {
                if (_priming === UNINITIALIZED_VALUE) {
                    throw IllegalStateException("Lazy not primed")
                }

                @Suppress("UNCHECKED_CAST")
                _value = initializer!!(_priming as P)
                initializer = null
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
}

internal class InitializedPrimedLazyImpl<out T, P>(override val value: T) : PrimedLazy<T, P> {
    override fun isInitialized(): Boolean = true
    override fun isPrimed(): Boolean = true
    override fun prime(payload: P) {}

    override fun toString(): String = value.toString()

}
