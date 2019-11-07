package info.spiralframework.base.common.properties

import kotlin.reflect.KProperty

class OneTimeMutable<T> {
    private var _value: Any? = UNINITIALIZED_VALUE
    @Suppress("UNCHECKED_CAST")
    var value: T
        get() = _value as? T ?: throw IllegalStateException("Value not initialised")
        set(value) {
            if (_value === UNINITIALIZED_VALUE) {
                _value = value
            } else {
                println("Value was already set") //Escalate?
            }
        }
}

public inline operator fun <T> OneTimeMutable<T>.getValue(thisRef: Any?, property: KProperty<*>): T = value
public inline operator fun <T> OneTimeMutable<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    this.value = value
}

public inline fun <reified T> oneTimeMutable(): OneTimeMutable<T> = OneTimeMutable()