package info.spiralframework.base

class MappingIterator<T, V>(val iterator: Iterator<T>, val map: (T) -> V): Iterator<V> {
    /**
     * Returns `true` if the iteration has more elements.
     */
    override fun hasNext(): Boolean = iterator.hasNext()

    /**
     * Returns the next element in the iteration.
     */
    override fun next(): V  = map(iterator.next())
}