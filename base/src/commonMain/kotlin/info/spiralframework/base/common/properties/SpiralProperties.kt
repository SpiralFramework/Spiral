package info.spiralframework.base.common.properties

inline class SpiralProperties(val properties: Map<ISpiralProperty.PropertyKey<*>, Any>) {
    constructor() : this(HashMap())

    inline fun keys() = properties.keys
    inline fun values() = properties.values
    inline fun entries() = properties.entries

    inline fun <T : Any> with(key: ISpiralProperty.PropertyKey<T>, value: T): SpiralProperties =
        SpiralProperties(properties.plus(key to value))

    inline fun <T : Any> withOptional(key: ISpiralProperty.PropertyKey<T>, value: T?): SpiralProperties =
        if (value == null) this else SpiralProperties(properties.plus(key, value))

    inline fun without(key: ISpiralProperty.PropertyKey<*>): SpiralProperties =
        SpiralProperties(properties.minus(key))

    inline fun without(keys: Iterable<ISpiralProperty.PropertyKey<*>>): SpiralProperties =
        SpiralProperties(properties.minus(keys))
}

inline operator fun <T> SpiralProperties?.get(key: ISpiralProperty.PropertyKey<T>): T? = this?.properties?.get(key)?.takeIf(key::isInstance)?.let(key::asInstance)
inline operator fun <T> SpiralProperties?.get(key: ISpiralProperty.Object<T>): T? = this?.get(key as ISpiralProperty.PropertyKey<T>)
inline operator fun <T> SpiralProperties?.get(property: ISpiralProperty<T>): T? = this?.get(property.key)

inline fun <T : Any> SpiralProperties.with(property: ISpiralProperty.Object<T>, value: T): SpiralProperties = with(property as ISpiralProperty.PropertyKey<T>, value)
inline fun <T : Any> SpiralProperties.with(property: ISpiralProperty<T>, value: T): SpiralProperties = with(property.key, value)

inline fun <T : Any> SpiralProperties.withOptional(property: ISpiralProperty.Object<T>, value: T?): SpiralProperties = withOptional(property as ISpiralProperty.PropertyKey<T>, value)
inline fun <T : Any> SpiralProperties.withOptional(property: ISpiralProperty<T>, value: T?): SpiralProperties = withOptional(property.key, value)

inline fun SpiralProperties.without(property: ISpiralProperty.Object<*>): SpiralProperties = without(property as ISpiralProperty.PropertyKey<*>)
inline fun SpiralProperties.without(property: ISpiralProperty<*>): SpiralProperties = without(property.key)

inline fun SpiralProperties.without(properties: Iterable<*>): SpiralProperties = without(properties.mapNotNull { value ->
    when (value) {
        is ISpiralProperty.Object<*> -> value as ISpiralProperty.PropertyKey<*>
        is ISpiralProperty.PropertyKey<*> -> value
        is ISpiralProperty<*> -> value.key
        else -> null
    }
})

inline operator fun SpiralProperties?.contains(property: ISpiralProperty.PropertyKey<*>): Boolean = this?.properties?.contains(property) == true
inline operator fun SpiralProperties?.contains(property: ISpiralProperty.Object<*>): Boolean = this.contains(property as ISpiralProperty.PropertyKey<*>)
inline operator fun SpiralProperties?.contains(property: ISpiralProperty<*>): Boolean = this.contains(property.key)


/**
 * Creates a new read-only map by replacing or adding an entry to this map from a given key-value [pair].
 *
 * The returned map preserves the entry iteration order of the original map.
 * The [pair] is iterated in the end if it has a unique key.
 */
public fun <K, V> Map<out K, V>.plus(key: K, value: V): Map<K, V> =
    if (this.isEmpty()) mapOf(key to value) else LinkedHashMap(this).apply { put(key, value) }