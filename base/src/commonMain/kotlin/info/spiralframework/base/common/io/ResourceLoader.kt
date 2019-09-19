package info.spiralframework.base.common.io

import info.spiralframework.base.common.SpiralModuleBase
import kotlin.reflect.KClass

@ExperimentalUnsignedTypes
expect suspend fun loadResource(name: String, from: KClass<*> = SpiralModuleBase::class): DataSource<*>?