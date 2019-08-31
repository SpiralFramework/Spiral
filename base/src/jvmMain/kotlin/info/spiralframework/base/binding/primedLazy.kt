package info.spiralframework.base.binding

import info.spiralframework.base.common.properties.PrimedLazy
import info.spiralframework.base.properties.SynchronizedPrimedLazyImpl

public actual fun <T, P> primedLazy(initializer: (P) -> T): PrimedLazy<T, P> = SynchronizedPrimedLazyImpl(initializer)