package info.spiralframework.formats.common.data

import kotlin.jvm.JvmInline
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

public interface IntArrayDataStructure {
    public val data: IntArray

    public fun intIndex(index: Int): IntDataAccessor = IntDataAccessor(index)
    public fun boolIndex(index: Int, trueValue: Int = 1, falseValue: Int = 0): BooleanDataAccessor =
        BooleanDataAccessor(data, index, trueValue, falseValue)
}

@JvmInline
public value class IntDataAccessor(public val index: Int) : ReadWriteProperty<IntArrayDataStructure, Int> {
    override fun getValue(thisRef: IntArrayDataStructure, property: KProperty<*>): Int = thisRef.data[index]
    override fun setValue(thisRef: IntArrayDataStructure, property: KProperty<*>, value: Int) {
        thisRef.data[index] = value
    }
}

public class BooleanDataAccessor(
    public val array: IntArray,
    public val index: Int,
    public val trueValue: Int,
    public val falseValue: Int
) : ReadWriteProperty<Any, Boolean> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean = array[index] == trueValue
    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        array[index] = if (value) trueValue else falseValue
    }
}