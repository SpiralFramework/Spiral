package info.spiralframework.base.binding

import info.spiralframework.base.common.properties.PrimedLazy

public expect fun <T, P> primedLazy(initializer: (P) -> T): PrimedLazy<T, P>