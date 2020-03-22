package info.spiralframework.base.common.collections

public fun <T, V> Array<T>.iterator(map: (T) -> V): Iterator<V> = MappingIterator(this.iterator(), map)
public fun <T, V> Iterable<T>.iterator(map: (T) -> V): Iterator<V> = MappingIterator(this.iterator(), map)
public fun <T, V> Iterator<T>.map(map: (T) -> V): Iterator<V> = MappingIterator(this, map)
//public fun <T, V> Enumeration<T>.iterator(map: (T) -> V): Iterator<V> = MappingIterator(this.iterator(), map)

public inline fun <T, R> Iterable<T>.mapFirst(map: (T) -> R?): R {
    for (element in this) return map(element) ?: continue

    throw NoSuchElementException("Collection contains no element matching the predicate.")
}

public inline fun <T, R> Iterable<T>.mapFirstOrNull(map: (T) -> R?): R? {
    for (element in this) return map(element) ?: continue

    return null
}