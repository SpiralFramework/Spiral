package info.spiralframework.osl

import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.reflect.full.safeCast

/**
 * Returns a *typed* array containing all of the elements of this collection.
 *
 * Allocates an array of runtime type `T` having its size equal to the size of this collection
 * and populates the array with the elements of this collection.
 * @sample samples.collections.Collections.Collections.collectionToTypedArray
 */
@Suppress("UNCHECKED_CAST")
public fun <T: Any> Collection<T>.toTypedArray(klass: KClass<T>): Array<T> {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    val thisCollection = this as java.util.Collection<T>
    return thisCollection.toArray(java.lang.reflect.Array.newInstance(klass.java, size) as Array<T>) as Array<T>
}

public fun <T: Any, V: T> List<T>.firstOfInstance(klass: KClass<V>): V = klass.cast(first(klass::isInstance))
public inline fun <T: Any, reified V: T> List<T>.firstOfInstance(): V = this.firstOfInstance(V::class)

public fun <T: Any, V: T> List<T>.firstOfInstanceOrNull(klass: KClass<V>): V? = firstOrNull(klass::isInstance)?.let { t -> klass.safeCast(t) }
public inline fun <T: Any, reified V: T> List<T>.firstOfInstanceOrNull(): V? = this.firstOfInstanceOrNull(V::class)
