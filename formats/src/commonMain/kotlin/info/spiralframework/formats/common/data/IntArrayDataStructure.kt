package info.spiralframework.formats.common.data

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface IntArrayDataStructure {
    val data: IntArray

    fun intIndex(index: Int): IntDataAccessor = IntDataAccessor(data, index)
    fun boolIndex(index: Int, trueValue: Int = 1, falseValue: Int = 0): BooleanDataAccessor = BooleanDataAccessor(data, index, trueValue, falseValue)
}

class IntDataAccessor(val array: IntArray, val index: Int): ReadWriteProperty<Any, Int> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Int = array[index]
    override fun setValue(thisRef: Any, property: KProperty<*>, value: Int) {
        array[index] = value
    }
}

class BooleanDataAccessor(val array: IntArray, val index: Int, val trueValue: Int, val falseValue: Int): ReadWriteProperty<Any, Boolean> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean = array[index] == trueValue
    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        array[index] = if (value) trueValue else falseValue
    }
}