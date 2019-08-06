package info.spiralframework.base.common.properties

import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object Observables {
    public inline fun <T> newValue(initialValue: T, crossinline onChange: (newValue: T) -> Unit):
            ReadWriteProperty<Any?, T> =
            object : ObservableProperty<T>(initialValue) {
                override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) = onChange(newValue)
            }
}