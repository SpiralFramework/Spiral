package info.spiralframework.core.formats

import dev.brella.kornea.errors.common.KorneaResult
import dev.brella.kornea.toolkit.common.DataCloseable
import info.spiralframework.base.common.concurrent.suspendForEach
import java.util.*
import kotlin.collections.ArrayList

data class FormatResult<out T>(val obj: T, val format: ReadableSpiralFormat<T>, val confidence: Double)