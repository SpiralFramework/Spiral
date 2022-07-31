package info.spiralframework.gui.javafx

/**
 * Replaces the first occurrence of [oldValue] in [this] with [newValue]
 * Returns the index [oldValue] was found at, or -1 if it was not found
 */
public fun <T> MutableList<in T>.replaceFirst(oldValue: T, newValue: T): Int {
    val index = indexOf(oldValue)
    if (index == -1) return -1

    set(index, newValue)
    return index
}

/**
 * Replaces the first occurrence of [oldValue] in [this] with [newValue], or adds it to the end of the list if not found.
 * Returns the index [oldValue] was found at, or the new index of [newValue]
 */
public fun <T> MutableList<in T>.replaceFirstOrAdd(oldValue: T, newValue: T): Int {
    val index = indexOf(oldValue)
    if (index == -1) {
        add(newValue)
        return size - 1
    }

    set(index, newValue)
    return index
}

/**
 * Replaces the first occurrence of [oldValue] in [this] with [newValue], or adds it to the end of the list if not found.
 * Returns the index [oldValue] was found at, or the new index of [newValue]
 */
public fun <T> MutableList<T>.replaceFirstOrAdd(newValue: T, filter: (listValue: T) -> Boolean): Int {
    val index = indexOfFirst(filter)
    if (index == -1) {
        add(newValue)
        return size - 1
    }

    set(index, newValue)
    return index
}

/**
 * Replaces the first occurrence of [oldValue] in [this] with [newValue], or adds it to the end of the list if not found.
 * Returns the index [oldValue] was found at, or the new index of [newValue]
 */
public fun <T> MutableList<T>.replaceFirstOrAdd(oldValue: T, newValue: T, equalityCheck: (listValue: T, oldValue: T) -> Boolean): Int {
    val index = indexOfFirst { listValue -> equalityCheck(listValue, oldValue) }
    if (index == -1) {
        add(newValue)
        return size - 1
    }

    set(index, newValue)
    return index
}

/**
 * Replaces the first occurrence of each value in [oldValues] in [this] with the corresponding value of [newValues], or adds it to the end of the list if not found.
 * Returns the index each value was either found at, or added at
 */
public fun <T> MutableList<in T>.replaceAllFirstOrAdd(oldValues: List<T>, newValues: List<T>): IntArray =
    IntArray(minOf(oldValues.size, newValues.size)) { i ->
        val index = indexOf(oldValues[i])
        if (index == -1) {
            add(newValues[i])
            return@IntArray size - 1
        }

        set(index, newValues[i])
        return@IntArray index
    }

/**
 * Replaces the first occurrence of each value in [oldValues] in [this] with the corresponding value of [newValues], or adds it to the end of the list if not found.
 * Returns the index each value was either found at, or added at
 */
public fun <T> MutableList<in T>.replaceAllFirstOrAdd(values: List<Pair<T, T>>): IntArray =
    IntArray(values.size) { i ->
        val (old, new) = values[i]
        val index = indexOf(old)
        if (index == -1) {
            add(new)
            return@IntArray size - 1
        }

        set(index, new)
        return@IntArray index
    }

/**
 * Replaces the first occurrence of each value in [oldValues] in [this] with the corresponding value of [newValues], or adds it to the end of the list if not found.
 * Returns the index each value was either found at, or added at
 */
public fun <T> MutableList<T>.replaceAllFirstOrAdd(oldValues: List<T>, newValues: List<T>, equalityCheck: (listValue: T, oldValue: T) -> Boolean): IntArray =
    IntArray(minOf(oldValues.size, newValues.size)) { i ->
        val oldValue = oldValues[i]
        val index = indexOfFirst { listValue -> equalityCheck(listValue, oldValue) }
        if (index == -1) {
            add(newValues[i])
            return@IntArray size - 1
        }

        set(index, newValues[i])
        return@IntArray index
    }