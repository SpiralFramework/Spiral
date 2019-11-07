package info.spiralframework.base.jvm.properties

import kotlin.reflect.KProperty

operator fun <T> KProperty<T>.getValue(thisRef: Any, property: KProperty<*>): T = this.call()