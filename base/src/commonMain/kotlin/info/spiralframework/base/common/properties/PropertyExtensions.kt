package info.spiralframework.base.common.properties

import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

operator fun <R, P: KProperty0<R>> P.getValue(any: Any, property: KProperty<*>): R = get()