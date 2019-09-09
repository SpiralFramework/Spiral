package info.spiralframework.base.common.collections

public fun <T, V> Array<T>.iterator(map: (T) -> V): Iterator<V> = MappingIterator(this.iterator(), map)
public fun <T, V> Iterable<T>.iterator(map: (T) -> V): Iterator<V> = MappingIterator(this.iterator(), map)
public fun <T, V> Iterator<T>.map(map: (T) -> V): Iterator<V> = MappingIterator(this, map)
//public fun <T, V> Enumeration<T>.iterator(map: (T) -> V): Iterator<V> = MappingIterator(this.iterator(), map)