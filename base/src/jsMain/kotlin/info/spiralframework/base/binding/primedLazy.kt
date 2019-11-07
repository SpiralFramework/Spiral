package info.spiralframework.base.binding

import info.spiralframework.base.common.properties.PrimedLazy
import info.spiralframework.base.common.properties.UnsafePrimedLazyImpl

actual fun <T, P> primedLazy(initializer: (P) -> T): PrimedLazy<T, P> = UnsafePrimedLazyImpl(initializer)