package info.spiralframework.base.common.properties

inline class SpiralProperties(val properties: MutableMap<String, Any>) {
    constructor() : this(HashMap())

    inline fun <T : Any> with(key: ISpiralProperty.PropertyKey<T>, value: T): SpiralProperties =
        apply { properties[key.name] = value }

    inline fun <T : Any> withOptional(key: ISpiralProperty.PropertyKey<T>, value: T?): SpiralProperties =
        if (value == null) this else apply { properties[key.name] = value }
}

inline operator fun <T> SpiralProperties?.get(key: ISpiralProperty.PropertyKey<T>): T? = this?.properties?.get(key.name)?.takeIf(key::isInstance)?.let(key::asInstance)
inline operator fun <T> SpiralProperties?.get(key: ISpiralProperty.Object<T>): T? = this?.get(key as ISpiralProperty.PropertyKey<T>)
inline operator fun <T> SpiralProperties?.get(property: ISpiralProperty<T>): T? = this?.get(property.key)

inline fun <T : Any> SpiralProperties.with(property: ISpiralProperty.Object<T>, value: T): SpiralProperties = with(property as ISpiralProperty.PropertyKey<T>, value)
inline fun <T : Any> SpiralProperties.with(property: ISpiralProperty<T>, value: T): SpiralProperties = with(property.key, value)

inline fun <T : Any> SpiralProperties.withOptional(property: ISpiralProperty.Object<T>, value: T?): SpiralProperties = withOptional(property as ISpiralProperty.PropertyKey<T>, value)
inline fun <T : Any> SpiralProperties.withOptional(property: ISpiralProperty<T>, value: T?): SpiralProperties = withOptional(property.key, value)

inline operator fun SpiralProperties?.contains(property: ISpiralProperty.PropertyKey<*>): Boolean = this?.properties?.contains(property.name) == true
inline operator fun SpiralProperties?.contains(property: ISpiralProperty.Object<*>): Boolean = this.contains(property as ISpiralProperty.PropertyKey<*>)
inline operator fun SpiralProperties?.contains(property: ISpiralProperty<*>): Boolean = this.contains(property.key)