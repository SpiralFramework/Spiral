@file:Suppress("NOTHING_TO_INLINE")

package info.spiralframework.base.common.properties

import kotlin.jvm.JvmInline

@JvmInline
public value class SpiralProperties(public val properties: Map<ISpiralProperty.PropertyKey<*>, Any>) {
    public constructor() : this(HashMap())

    public inline fun keys(): Set<ISpiralProperty.PropertyKey<*>> = properties.keys
    public inline fun values(): Collection<Any> = properties.values
    public inline fun entries(): Set<Map.Entry<ISpiralProperty.PropertyKey<*>, Any>> = properties.entries

    public inline fun <T : Any> with(key: ISpiralProperty.PropertyKey<T>, value: T): SpiralProperties =
        SpiralProperties(properties.plus(key to value))

    public inline fun <T : Any> withOptional(key: ISpiralProperty.PropertyKey<T>, value: T?): SpiralProperties =
        if (value == null) this else SpiralProperties(properties.plus(key, value))

    public inline fun without(key: ISpiralProperty.PropertyKey<*>): SpiralProperties =
        SpiralProperties(properties - key)

    public inline fun without(keys: Iterable<ISpiralProperty.PropertyKey<*>>): SpiralProperties =
        SpiralProperties(properties - keys.toSet())
}

public inline operator fun <T> SpiralProperties?.get(key: ISpiralProperty.PropertyKey<T>): T? = this?.properties?.get(key)?.takeIf(key::isInstance)?.let(key::asInstance)
public inline operator fun <T> SpiralProperties?.get(key: ISpiralProperty.Object<T>): T? = this?.get(key as ISpiralProperty.PropertyKey<T>)
public inline operator fun <T> SpiralProperties?.get(property: ISpiralProperty<T>): T? = this?.get(property.key)

//inline fun <T : Any> SpiralProperties.with(property: ISpiralProperty.Object<T>, value: T): SpiralProperties = with(property as ISpiralProperty.PropertyKey<T>, value)
public inline fun <T : Any> SpiralProperties.with(property: ISpiralProperty<T>, value: T): SpiralProperties = with(property.key, value)

//inline fun <T : Any> SpiralProperties.withOptional(property: ISpiralProperty.Object<T>, value: T?): SpiralProperties = withOptional(property as ISpiralProperty.PropertyKey<T>, value)
public inline fun <T : Any> SpiralProperties.withOptional(property: ISpiralProperty<T>, value: T?): SpiralProperties = withOptional(property.key, value)

//inline fun SpiralProperties.without(property: ISpiralProperty.Object<*>): SpiralProperties = without(property as ISpiralProperty.PropertyKey<*>)
public inline fun SpiralProperties.without(property: ISpiralProperty<*>): SpiralProperties = without(property.key)

public inline fun SpiralProperties.without(properties: Iterable<*>): SpiralProperties = without(properties.mapNotNull { value ->
    when (value) {
        is ISpiralProperty.Object<*> -> value
        is ISpiralProperty.PropertyKey<*> -> value
        is ISpiralProperty<*> -> value.key
        else -> null
    }
})

public inline operator fun SpiralProperties?.contains(property: ISpiralProperty.PropertyKey<*>): Boolean = this?.properties?.contains(property) == true
public inline operator fun SpiralProperties?.contains(property: ISpiralProperty.Object<*>): Boolean = this.contains(property as ISpiralProperty.PropertyKey<*>)
public inline operator fun SpiralProperties?.contains(property: ISpiralProperty<*>): Boolean = this.contains(property.key)


/**
 * Creates a new read-only map by replacing or adding an entry to this map from a given ([key],[value]).
 *
 * The returned map preserves the entry iteration order of the original map.
 * The [key] is iterated in the end if it is unique.
 */
public fun <K, V> Map<out K, V>.plus(key: K, value: V): Map<K, V> =
    if (this.isEmpty()) mapOf(key to value) else LinkedHashMap(this).apply { put(key, value) }