package info.spiralframework.base.binding

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.io.BinaryDataPool
import info.spiralframework.base.common.io.DataPool
import info.spiralframework.base.common.io.SpiralCacheProvider
import info.spiralframework.base.common.io.flow.InputFlow
import info.spiralframework.base.common.io.flow.OutputFlow
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalUnsignedTypes
actual typealias DefaultSpiralCacheProvider = SpiralCacheProvider.Memory